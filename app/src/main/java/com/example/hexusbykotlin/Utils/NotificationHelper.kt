package com.example.hexusbykotlin.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hexusbykotlin.R

class NotificationHelper(base: Context):ContextWrapper(base) {

    companion object{
        private val EDMT_CHANNEL_ID = "com.example.hexusbykotlin"
        private val EDMT_CHANNEL_NAME = "HexusByKotlin"
    }

    private var manager:NotificationManager?=null
    init{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(defaultUri: Uri?) {
        val edmtChannel = NotificationChannel(EDMT_CHANNEL_ID, EDMT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

        edmtChannel.enableLights(true)
        edmtChannel.enableVibration(true)

        edmtChannel.lockscreenVisibility= Notification.VISIBILITY_PRIVATE

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        edmtChannel.setSound(defaultUri!!, audioAttributes)

        getManager()!!.createNotificationChannel(edmtChannel)
    }

    public fun getManager(): NotificationManager {
        if(manager == null)
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRealtimeTrackingNotification(title:String, content:String):Notification.Builder{
        return Notification.Builder(applicationContext, EDMT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
    }
}