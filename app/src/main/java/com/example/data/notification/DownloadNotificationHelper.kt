package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class DownloadNotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "DownloadNotifier"
        const val CHANNEL_ID_DOWNLOADS = "tubeconvert_downloads_channel"
        const val CHANNEL_NAME = "Descargas y Conversiones"
        const val CHANNEL_DESC = "Alertas de descargas completadas y errores de procesamiento multimedia"

        // Base notification offsets
        private const val NOTIFICATION_ID_PROGRESS_BASE = 1000
        private const val NOTIFICATION_ID_SUCCESS_BASE = 2000
        private const val NOTIFICATION_ID_ERROR_BASE = 3000
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID_DOWNLOADS)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_DOWNLOADS,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Canal de notificaciones creado: $CHANNEL_ID_DOWNLOADS")
            }
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_downloads", true)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    /**
     * Posts a notification alerting the user that a video or audio download has completed successfully.
     */
    fun notifyDownloadCompleted(
        downloadId: Long,
        title: String,
        targetFormat: String,
        fileSizeFormatted: String,
        isAudio: Boolean = false
    ) {
        if (!canPostNotifications()) {
            Log.d(TAG, "Permiso de notificaciones no disponible para $title")
            return
        }

        try {
            cancelProgressNotification(downloadId)

            val mediaLabel = if (isAudio) "Audio" else "Video"
            val formatUpper = targetFormat.uppercase()
            val contentText = "$mediaLabel $formatUpper listo • $fileSizeFormatted"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOADS)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("¡Descarga completada con éxito!")
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$title\n$contentText\nGuardado en tu carpeta de Descargas.")
                        .setBigContentTitle("¡Descarga completada!")
                        .setSummaryText("TubeConvert")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(createContentIntent())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationId = (NOTIFICATION_ID_SUCCESS_BASE + (downloadId % 1000)).toInt()
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Notificación de éxito enviada para: $title (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Permiso denegado para notificaciones: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación de éxito", e)
        }
    }

    /**
     * Posts a notification alerting the user that an error occurred while downloading or converting.
     */
    fun notifyDownloadError(
        downloadId: Long,
        title: String,
        errorMessage: String
    ) {
        if (!canPostNotifications()) {
            Log.d(TAG, "Permiso de notificaciones no disponible para error en $title")
            return
        }

        try {
            cancelProgressNotification(downloadId)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOADS)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Error en la descarga")
                .setContentText("$title: $errorMessage")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("No se pudo completar la descarga de '$title'.\nDetalle: $errorMessage")
                        .setBigContentTitle("Error en el procesamiento")
                        .setSummaryText("TubeConvert")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setAutoCancel(true)
                .setContentIntent(createContentIntent())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationId = (NOTIFICATION_ID_ERROR_BASE + (downloadId % 1000)).toInt()
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Notificación de error enviada para: $title (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Permiso denegado para notificaciones: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación de error", e)
        }
    }

    /**
     * Posts or updates an ongoing download or conversion progress notification.
     */
    fun notifyDownloadProgress(
        downloadId: Long,
        title: String,
        progressPercent: Int,
        isConverting: Boolean = false,
        speedFormatted: String? = null
    ) {
        if (!canPostNotifications()) return

        try {
            val statusText = if (isConverting) {
                "Convirtiendo formato..."
            } else if (!speedFormatted.isNullOrBlank() && speedFormatted != "0 KB/s") {
                "Descargando ($progressPercent%) • $speedFormatted"
            } else {
                "Descargando ($progressPercent%)..."
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOADS)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(if (isConverting) "Convirtiendo: $title" else "Descargando: $title")
                .setContentText(statusText)
                .setProgress(100, progressPercent.coerceIn(0, 100), isConverting)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .build()

            val notificationId = (NOTIFICATION_ID_PROGRESS_BASE + (downloadId % 1000)).toInt()
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            // Silently ignore progress notification updates if restricted
        }
    }

    fun cancelProgressNotification(downloadId: Long) {
        try {
            val notificationId = (NOTIFICATION_ID_PROGRESS_BASE + (downloadId % 1000)).toInt()
            notificationManager.cancel(notificationId)
        } catch (e: Exception) {
            // Ignore cancel errors
        }
    }

    fun cancelAll() {
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
