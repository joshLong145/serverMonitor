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

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val ip = preferences.getString("ip_address", "")
        val pass = preferences.getString("password", "")
        val process = preferences.getString("process", "")

        var ipComp = findViewById<EditText>(R.id.ip)
        var passComp = findViewById<EditText>(R.id.password)
        var processComp = findViewById<EditText>(R.id.process_name)

        if(ip != "" && pass != "") {
            ipComp.setText(ip)
            passComp.setText(pass)
            processComp.setText(process)
        }

    }


    /**
     * Get data from input fields on button click
     */
    private fun cacheData(){
        var ip = findViewById<EditText>(R.id.ip)
        var pass = findViewById<EditText>(R.id.password)
        var process = findViewById<EditText>(R.id.process_name)

        if(ip != null && pass != null){
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)

            preferences.edit().putString("ip_address", ip.text.toString()).apply()
            preferences.edit().putString("password", pass.text.toString()).apply()
            preferences.edit().putString("process", process.text.toString()).apply()

            Toast.makeText(this, "data saved", Toast.LENGTH_LONG).show()
        }

    }
}
