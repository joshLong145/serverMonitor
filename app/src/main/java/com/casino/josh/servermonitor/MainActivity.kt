package com.casino.josh.servermonitor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.util.*
import android.os.StrictMode



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var cacheButton = findViewById<Button>(R.id.saveData)

        cacheButton?.setOnClickListener { cacheData() }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }


    /**
     * Get data from input fields on button click
     */
    private fun cacheData(){
        var ip = findViewById<EditText>(R.id.ip)
        var pass = findViewById<EditText>(R.id.password)

        if(ip != null && pass != null){
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)

            preferences.edit().putString("ip_address", ip.text.toString()).apply()
            preferences.edit().putString("password", pass.text.toString()).apply()

            Toast.makeText(this, "data saved", Toast.LENGTH_LONG).show()

            val ip_address = ip.text.toString()
            val server_pass = pass.text.toString()

            val jsch = JSch()
            val session = jsch.getSession("root", ip_address, 22)
            session.setPassword(server_pass)

            // Avoid asking for key confirmation.
            val properties = Properties()
            properties.put("StrictHostKeyChecking", "no")
            session.setConfig(properties)

            val thread = Runnable {
                session.connect()

                // Create SSH Channel.
                val sshChannel = session.openChannel("exec") as ChannelExec
                val outputStream = ByteArrayOutputStream()
                sshChannel.outputStream = outputStream

                // Execute command.
                sshChannel.setCommand("ls")
                sshChannel.connect()

                var data = outputStream.toString()

                // Sleep needed in order to wait long enough to get result back.
                Thread.sleep(1_000)
                sshChannel.disconnect()

                session.disconnect()
            }

            thread.run()
        }

    }
}
