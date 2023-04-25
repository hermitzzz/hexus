package com.example.hexusbykotlin.ViewHolder.Service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.hexusbykotlin.Model.User
import com.example.hexusbykotlin.R
import com.example.hexusbykotlin.Utils.Common
import com.example.hexusbykotlin.Utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val user = FirebaseAuth.getInstance().currentUser

        if(user != null) {
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)

            tokens.child(user!!.uid).setValue(s)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if(p0!!.data != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationWithChannel(p0)
            else
                sendNotification(p0)

            addRequestToUserInformation(p0.data)
        }
    }

    private fun sendNotification(p0: RemoteMessage) {
        val data = p0.data
        val title = "Friend Request"
        val content = "New Friends request from "+data[Common.FROM_EMAIL]!!

        val builder = NotificationCompat.Builder(this, "")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(), builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotificationWithChannel(p0: RemoteMessage) {
        val data = p0.data
        val title = "Friend Request"
        val content = "New Friends request from "+data[Common.FROM_EMAIL]!!
        val helper: NotificationHelper = NotificationHelper(this)

        val builder:Notification.Builder = helper.getRealtimeTrackingNotification(title, content)


        helper.getManager()!!.notify(Random().nextInt(),builder.build())

    }

    private fun addRequestToUserInformation(data: Map<String, String>) {

        val friend_request = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(data[Common.TO_UID]!!)
            .child(Common.FRIEND_REQUEST)

        val user = User(data[Common.From_UID]!!, data[Common.FROM_EMAIL]!!)
        friend_request.child(user.uid!!).setValue(user)

    }
}