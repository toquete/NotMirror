package com.toquete.notmirror.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.toquete.notmirror.R
import com.toquete.notmirror.data.AllowlistDataStore
import com.toquete.notmirror.model.NotificationPayload
import com.toquete.notmirror.network.NotificationWebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "NotificationListener"
private const val CHANNEL_ID = "notmirror_service"
private const val FOREGROUND_NOTIFICATION_ID = 1

class NotificationListenerService : NotificationListenerService() {

    private lateinit var server: NotificationWebSocketServer
    private lateinit var allowlistDataStore: AllowlistDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var currentAllowlist: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        allowlistDataStore = AllowlistDataStore(applicationContext)
        server = NotificationWebSocketServer(port = 8765)

        createNotificationChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())

        server.start()

        scope.launch {
            allowlistDataStore.getAllowlistFlow().collect { allowlist ->
                currentAllowlist = allowlist
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in currentAllowlist) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val payload = NotificationPayload(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            timestamp = sbn.postTime
        )

        server.broadcast(payload)
        Log.d(TAG, "Forwarded: [$appName] $title")
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, NotificationListenerService::class.java))
    }

    override fun onDestroy() {
        scope.cancel()
        try {
            server.stop(500)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping WebSocket server", e)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(): android.app.Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
}
