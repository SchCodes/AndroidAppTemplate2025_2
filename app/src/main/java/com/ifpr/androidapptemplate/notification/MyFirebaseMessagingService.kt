package com.ifpr.androidapptemplate.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val status = remoteMessage.data["status"]
            ?: remoteMessage.data["message"]
            ?: remoteMessage.notification?.body
            ?: "em preparo"
        NotificationRepository.addNotification(applicationContext, status)
        sendStatusNotification(status)
    }

    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_meu_icone)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mensagens",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendStatusNotification(status: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "status_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Status do Pedido",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val targetProgress = when (status.lowercase()) {
            "em preparo" -> 33
            "saindo para entrega" -> 66
            "entregue" -> 100
            else -> 0
        }

        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_meu_icone)
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        val notificationId = 1001
        var currentProgress = 0

        val handler = android.os.Handler(mainLooper)
        val updateRunnable = object : Runnable {
            override fun run() {
                if (currentProgress <= targetProgress) {
                    remoteViews.setTextViewText(R.id.status_text, "Pedido: $status")
                    remoteViews.setProgressBar(R.id.progress_bar, 100, currentProgress, false)
                    notificationManager.notify(notificationId, builder.build())
                    currentProgress++
                    handler.postDelayed(this, 50L)
                }
            }
        }

        handler.post(updateRunnable)

        if (targetProgress == 100) {
            handler.postDelayed({
                notificationManager.cancel(notificationId)
            }, 3000)
        }
    }
}
