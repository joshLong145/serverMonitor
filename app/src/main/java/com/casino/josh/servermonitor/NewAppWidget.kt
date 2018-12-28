package com.casino.josh.servermonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.jcraft.jsch.JSch
import android.widget.RemoteViews
import com.jcraft.jsch.ChannelExec
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {


    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent != null) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
            }
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
            sshChannel.setCommand("ls")
            sshChannel.connect()

            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()

            return outputStream.toString()
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.new_app_widget)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ip = preferences.getString("ip_address", "")
            val pass = preferences.getString("password", "")

            val data = executeRemoteCommand("root", pass, ip)

            views.setTextViewText(R.id.appwidget_text, data)

        }
    }
}

