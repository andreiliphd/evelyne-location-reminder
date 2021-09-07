package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getSystemService
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    Log.i("geofence", "Send notification received.")

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Location Reminder"
        val descriptionText = "Location Reminder App Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    val intent = Intent(context, ReminderDescriptionActivity::class.java)

    val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        // Add the intent, which inflates the back stack
        addNextIntentWithParentStack(intent)
        // Get the PendingIntent containing the entire back stack
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Set the intent that will fire when the user taps the notification
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
    Log.i("geofence", "Sending notification.")
    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(getUniqueId(), builder.build())
    }
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())