package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoResolution

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val videoId: String,
    val title: String,
    val author: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val mediaType: MediaType,
    val targetFormat: String, // e.g. "mp4", "mp3", "mkv"
    val resolutionLabel: String?, // e.g. "1080p Full HD"
    val qualityLabel: String?, // e.g. "320 kbps"
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val localFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
) {
    val isFinished: Boolean
        get() = status == DownloadStatus.COMPLETED

    val formattedDownloadedSize: String
        get() {
            val curMb = downloadedBytes / (1024f * 1024f)
            val totMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else 0f
            return if (totMb > 0f) {
                "%.1f MB / %.1f MB".format(curMb, totMb)
            } else {
                "%.1f MB".format(curMb)
            }
        }

    val formattedSpeed: String
        get() {
            val kb = speedBytesPerSec / 1024f
            return if (kb >= 1024f) {
                "%.1f MB/s".format(kb / 1024f)
            } else if (kb > 0f) {
                "%.0f KB/s".format(kb)
            } else {
                "0 KB/s"
            }
        }

    val formattedSpeedWithDirection: String
        get() = if (speedBytesPerSec > 0L) "↓ $formattedSpeed" else formattedSpeed

    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}
