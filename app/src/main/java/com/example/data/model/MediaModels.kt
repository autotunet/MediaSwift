package com.example.data.model

enum class MediaType {
    VIDEO,
    AUDIO
}

enum class VideoFormat(
    val extension: String,
    val mimeType: String,
    val label: String,
    val description: String
) {
    MP4("mp4", "video/mp4", "MP4", "Universal H.264 / AAC"),
    MKV("mkv", "video/x-matroska", "MKV", "Alta fidelidad Matroska"),
    WEBM("webm", "video/webm", "WebM", "Optimizado Google VP9"),
    AVI("avi", "video/x-msvideo", "AVI", "Compatible reproductores"),
    THREE_GP("3gp", "video/3gpp", "3GP", "Ultra ligero para móvil")
}

enum class AudioFormat(
    val extension: String,
    val mimeType: String,
    val label: String,
    val description: String
) {
    MP3("mp3", "audio/mpeg", "MP3", "Máxima compatibilidad (MPEG)"),
    M4A("m4a", "audio/mp4", "M4A", "Excelente compresión AAC"),
    AAC("aac", "audio/aac", "AAC", "Audio de alta fidelidad"),
    WAV("wav", "audio/wav", "WAV", "Sin compresión (Estudio)"),
    FLAC("flac", "audio/flac", "FLAC", "Lossless de alta resolución"),
    OGG("ogg", "audio/ogg", "OGG", "Código abierto Vorbis")
}

enum class VideoResolution(
    val label: String,
    val height: Int,
    val bitrateLabel: String,
    val sizeFactor: Float
) {
    R_1080P("1080p Full HD", 1080, "60 FPS • ~4.5 Mbps", 1.8f),
    R_720P("720p HD", 720, "30 FPS • ~2.5 Mbps", 1.0f),
    R_480P("480p SD", 480, "30 FPS • ~1.2 Mbps", 0.65f),
    R_360P("360p Medio", 360, "30 FPS • ~700 kbps", 0.4f),
    R_240P("240p Ligero", 240, "24 FPS • ~400 kbps", 0.25f)
}

enum class AudioQuality(
    val label: String,
    val bitrateKbps: Int,
    val badge: String,
    val sizeFactor: Float
) {
    Q_320("320 kbps", 320, "Ultra HQ", 1.4f),
    Q_256("256 kbps", 256, "Alta Calidad", 1.15f),
    Q_192("192 kbps", 192, "Estándar", 0.85f),
    Q_128("128 kbps", 128, "Económico", 0.6f),
    Q_64("64 kbps", 64, "Podcast / Voz", 0.35f)
}

enum class DownloadStatus {
    QUEUED,
    CONNECTING,
    DOWNLOADING,
    CONVERTING,
    COMPLETED,
    PAUSED,
    FAILED
}

data class VideoMetadata(
    val id: String,
    val title: String,
    val author: String,
    val durationSeconds: Long = 214, // default 3m 34s
    val thumbnailUrl: String,
    val originalUrl: String,
    val viewsEstimate: String = "1.2M vistas",
    val uploadDate: String = "Hace 3 días",
    val isSample: Boolean = false,
    val directStreamUrl: String? = null
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

data class ConversionConfig(
    val mediaType: MediaType = MediaType.VIDEO,
    val videoFormat: VideoFormat = VideoFormat.MP4,
    val videoResolution: VideoResolution = VideoResolution.R_720P,
    val audioFormat: AudioFormat = AudioFormat.MP3,
    val audioQuality: AudioQuality = AudioQuality.Q_320,
    val extractAudioOnly: Boolean = false,
    val customFileName: String = ""
) {
    fun estimatedSizeBytes(durationSeconds: Long): Long {
        val baseSec = if (durationSeconds > 0) durationSeconds else 180
        return if (extractAudioOnly || mediaType == MediaType.AUDIO) {
            // Bitrate in kbps -> bytes per second = (bitrate * 1000) / 8
            val bytesPerSec = (audioQuality.bitrateKbps * 1024L) / 8L
            (bytesPerSec * baseSec)
        } else {
            val baseVideoBytesPerSec = 300_000L // ~2.4 Mbps base
            ((baseVideoBytesPerSec * videoResolution.sizeFactor) * baseSec).toLong()
        }
    }

    fun formatEstimatedSize(durationSeconds: Long): String {
        val bytes = estimatedSizeBytes(durationSeconds)
        val mb = bytes / (1024f * 1024f)
        return "%.1f MB".format(mb)
    }

    val fileExtension: String
        get() = if (extractAudioOnly || mediaType == MediaType.AUDIO) {
            audioFormat.extension
        } else {
            videoFormat.extension
        }
}
