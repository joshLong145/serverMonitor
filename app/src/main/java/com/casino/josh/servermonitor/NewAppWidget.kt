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
import android.content.ComponentName
import android.widget.Toast
import java.lang.StringBuilder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


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
        // Construct the RemoteViews object

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.new_app_widget)

        val preferences = context.getSharedPreferences(PreferenceManager
                          .getDefaultSharedPreferencesName(context), 0)


        val ip = preferences.getString("ip_address", "")
        val pass = preferences.getString("password", "")
        val processName = preferences.getString("process", "")

        val data = executeRemoteCommand("root", pass, ip, processName)

        views.setTextViewText(R.id.appwidget_text, data[0])
        views.setTextViewText(R.id.appwidget_ip_address, ip)

        var widgetNum = ComponentName(context, this::class.java)
        AppWidgetManager.getInstance(context).updateAppWidget(widgetNum, views)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(intent != null){
        }
    }

    companion object {

        private fun parsePMData(serialStream : String) : String{
            // split on spaces, we dont care about the number since
            // the upper bound is relatively small ( < 100 )
            var attributes = serialStream.split(" ")

            if(!attributes.contains("online") && !attributes.contains("offline")){
                // if there is no way to parse the first line then return the whole string back
                // this will be changed, doing this to see the kind of data being given to
                // the function.
                return serialStream
            }

            var dataStream = StringBuilder()

            dataStream.append("Recent backup log:\n")
            dataStream.append(DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now()))
            dataStream.append("\n")

            dataStream.append("status: ")
            for(attr in attributes){ // search for the cpu Usage
                if(attr.contains("online") || attr.contains("offline")){
                    dataStream.append(attr)
                    dataStream.append("\n")
                    // break out of the loop once found.
                    break
                }
            }

            dataStream.append("Usage: ")
            for(attr in attributes){ // search for the cpu Usage
                if(attr.contains("%")){
                    dataStream.append(attr)
                    dataStream.append("\n")
                    // break out of the loop once found.
                    break
                }
            }

            dataStream.append("Mem: ")
            for(attr in attributes){ // search for the cpu Usage
                if(attr.contains(".")){
                    dataStream.append(attr)

                }

                if(attr.contains("MB")) {
                    dataStream.append(" MB")
                    // break out of the loop once found.
                    break
                }else if(attr.contains("GB")){
                    dataStream.append(" GB")
                    // break out of the loop once found.
                    break
                }
            }

            return dataStream.toString()
        }

        private fun executeRemoteCommand(username : String,
                                        password : String,
                                        hostname : String,
                                        process : String,
                                        port: Int = 22) : List<String> {
            var data: MutableList<String>;

            try {
                val conn = SshConnectionUtility()

                // TODO: abstract user option, root user terrible option.
                conn.initConnectionParams(username, hostname, password, process)

                val thrd = Thread(conn)
                thrd.start()

                while (thrd.isAlive) {
                } // halt execution until thread finishes running.

                data = conn.getData()

                // Data parsed from bash grep chain server side.
                // TODO: abstract so other functionality other than pm2 data parsing can be utilized.
                data[0] = parsePMData(data[0])
            } catch(error: Error) {
                data = mutableListOf()
                data.add(0, error.message.toString())
            }

            return data
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            val quotes = mutableListOf<String>(
                    "Sometimes the worst place you can be at is in your own head",
                    "ou may have to fight a battle more than once",
                    "You can't control everything. \n Sometimes you just need to relax and have faith that things will work out.",
                    "I know you're tired. \n I know you're physically and emotionally drained, \n but you have to keep going",
                    "My feelings are valid",
                    "Recovery is not one and done. \n It is a lifelong journey that takes place one day, \n one step at a time"
            );

            fun IntRange.random() =
                    Random().nextInt((endInclusive + 1) - start) + start

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.new_app_widget)


            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ip = preferences.getString("ip_address", "")
            val pass = preferences.getString("password", "")
            val process = preferences.getString("process", "")

            val data = executeRemoteCommand("root", pass, ip, process)

            views.setTextViewText(R.id.appwidget_text, data[0])
            views.setTextViewText(R.id.appwidget_ip_address, ip)
            views.setTextViewText(R.id.appwidget_logs, quotes.get((0..5).random()))
            views.setTextViewText(R.id.appwidget_log_header, "Last log entry:")

            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
        }
    }
}

