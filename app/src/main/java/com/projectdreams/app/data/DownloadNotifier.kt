package com.projectdreams.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.projectdreams.app.MainActivity
import com.projectdreams.app.R
import com.projectdreams.app.data.model.DownloadProgress

/**
 * Shows a live progress notification while an app is being downloaded,
 * mirroring the in-app download bar so the user can see it from the shade.
 */
class DownloadNotifier(private val context: Context) {

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Progress of app downloads"
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showProgress(progress: DownloadProgress) {
        val percent = (progress.fraction.coerceIn(0f, 1f) * 100).toInt()
        val parts = buildList {
            add(progress.status ?: "Downloading…")
            if (progress.bytesPerSecond > 0f) {
                add(Format.speed(progress.bytesPerSecond))
                progress.etaSeconds?.let { add(Format.eta(it)) }
            }
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading hololive Dreams")
            .setContentText(parts.joinToString(" · "))
            .setContentIntent(pendingOpen())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, percent, false)
            .build()
        notify(notification)
    }

    fun showInstalling() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Installing hololive Dreams")
            .setContentText("Almost there…")
            .setContentIntent(pendingOpen())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, 100, false)
            .build()
        notify(notification)
    }

    fun showDone(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("hololive Dreams")
            .setContentText(message)
            .setContentIntent(pendingOpen())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }

    fun dismiss() {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (_: SecurityException) {
        }
    }

    /**
     * Opens the app without destroying the running activity (singleTask), and
     * flags the resume request so an interrupted download continues.
     */
    private fun pendingOpen(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_RESUME, true)
        }
        return PendingIntent.getActivity(
            context, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(notification: android.app.Notification) {
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted — nothing we can do.
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "downloads"
        const val EXTRA_RESUME = "resume_download"
    }
}
