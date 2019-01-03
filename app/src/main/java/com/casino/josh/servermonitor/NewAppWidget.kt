package com.casino.josh.servermonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.jcraft.jsch.JSch
import android.widget.RemoteViews
import android.widget.TextView
import com.jcraft.jsch.ChannelExec
import java.io.ByteArrayOutputStream
import java.util.*
import android.content.ComponentName





/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {


    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        var test = 1
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        // Construct the RemoteViews object
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent != null) {
            val views = RemoteViews(context!!.packageName, R.layout.new_app_widget)


            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ip = preferences.getString("ip_address", "")
            val pass = preferences.getString("password", "")

            val data = executeRemoteCommand("root", pass, ip)

            views.setTextViewText(R.id.appwidget_text, data)

            var appNum = ComponentName(context, this::class.java)

            AppWidgetManager.getInstance(context).updateAppWidget(appNum, views)

        }
    }

    companion object {

        private fun executeRemoteCommand(username: String,
                                        password: String,
                                        hostname: String,
                                        port: Int = 22): String {
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
            // mpstat command to get current cpu usage
            sshChannel.setCommand("cat /proc/loadavg")
            sshChannel.connect()

            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()

            var serialCommandData = outputStream.toString()
            return serialCommandData
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.new_app_widget)


            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ip = preferences.getString("ip_address", "")
            val pass = preferences.getString("password", "")

            val data = executeRemoteCommand("root", pass, ip)

            views.setTextViewText(R.id.appwidget_text, data)

        }
    }
}

