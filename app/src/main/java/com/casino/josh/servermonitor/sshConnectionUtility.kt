package com.casino.josh.servermonitor;

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.util.*

class SshConnectionUtility() :  Runnable {

    private var username : String = ""
    private var hostname : String = ""
    private var password : String = ""
    private var port : Int = 0
    private var outputData : String = ""

    fun initConnectionParams(user : String, host : String, pass : String, port : Int = 22) {
        username = user
        hostname = host
        password = pass
        this.port = port
    }

    fun getData() : String {
        return outputData
    }

    fun isStopped(): Boolean {
        return Thread.currentThread().isInterrupted() // Version 2
    }

    fun shutdown() {
        Thread.currentThread().interrupt(); // Version 2
    }

    override fun run() {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sshChannel = session.openChannel("exec") as ChannelExec
        val outputStream = ByteArrayOutputStream()
        sshChannel.outputStream = outputStream

        // Execute command.
        // Command is a grep and replace on pm2 status information
        // for custom pm2 process.
        sshChannel.setCommand("pm2 status | grep API | tr -d '\\200-\\377'")
        sshChannel.connect()

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(1_000)
        sshChannel.disconnect()

        session.disconnect()

        outputData = outputStream.toString()
    }
}