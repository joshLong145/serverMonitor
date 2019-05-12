package com.casino.josh.servermonitor;

import android.widget.Toast
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class SshConnectionUtility() :  Runnable {

    private var username : String = ""
    private var hostname : String = ""
    private var password : String = ""
    private var processName : String = ""
    private var port : Int = 0
    private var outputData : MutableList<String> = mutableListOf()

    fun initConnectionParams(user : String,
                             host : String,
                             pass : String,
                             process : String,
                             port : Int = 22) {
        username = user
        hostname = host
        password = pass
        processName = process
        this.port = port
    }

    fun getData() : MutableList<String> {
        return outputData
    }

    /**
     * Overloaded function called when thread,start() is called.
     */
    override fun run() {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // predefined commands for pm2 logistics based on my personal needs.
        val commands = arrayOf("pm2 status | grep API | tr -d '\\200-\\377'",
                "tail -n 1 < ~/backups/output.log")

        // Run all commands within the commands array.
        // TODO: abstract commands to shared prefs
        try {
            for (command: String in commands) {
                // Create SSH Channel.
                val sshChannel = session.openChannel("exec") as ChannelExec
                val outputStream = ByteArrayOutputStream()
                sshChannel.outputStream = outputStream

                // Execute command.
                // Command is a grep and replace on pm2 status information
                // for custom pm2 process.
                sshChannel.setCommand(command)
                sshChannel.connect()

                // Sleep needed in order to wait long enough to get result back.
                Thread.sleep(1_000)

                // Disconnect from the ssh session after we are done.
                sshChannel.disconnect()

                outputData.add(outputStream.toString())
            }
        } catch(error: Exception) {
            throw error;
        }

        session.disconnect()
    }
}