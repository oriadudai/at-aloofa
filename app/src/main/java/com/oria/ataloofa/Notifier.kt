package com.oria.ataloofa

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val CHANNEL_ID = "envelopes"
const val EXTRA_ID = "envelope_id"

object Notifier {

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "מעטפות", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "הודעות מאוריה" }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /** Schedule every envelope whose time has not passed yet (regular, non-exact). */
    fun scheduleAll(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val now = System.currentTimeMillis()
        for (e in ENVELOPES) {
            val pi = alarmIntent(context, e.id)
            am.cancel(pi)
            if (e.timeMillis() > now) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, e.timeMillis(), pi)
            }
        }
    }

    private fun alarmIntent(context: Context, id: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, id,
            Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun show(context: Context, id: Int, title: String, body: String) {
        val open = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_ID, id)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setColor(0xFFF49AC2.toInt())
            .setContentTitle(title)
            .setContentText("יש לך מעטפה חדשה 💌")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) {
            // notification permission not granted
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, -1)
        val e = ENVELOPES.firstOrNull { it.id == id } ?: return
        Notifier.createChannel(context)
        Notifier.show(context, e.id, e.title, e.body)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifier.createChannel(context)
        Notifier.scheduleAll(context)
    }
}
