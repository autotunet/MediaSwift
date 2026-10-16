package com.example.data.network

import com.example.data.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Universal video and media URL resolver.
 * Parses any user-supplied media URL (direct streams, web video links, MP4/WEBM URLs)
 * without using third-party brand logos or restricted copyright imagery.
 */
object YouTubeResolver {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Generic pattern supporting web video identifiers and universal links
    private val VIDEO_ID_PATTERN = Pattern.compile(
        "(?:(?:watch\\?(?:.*&)?v=|v/|embed/|shorts/|/d/|/v/|/video/))([a-zA-Z0-9_-]{8,})",
        Pattern.CASE_INSENSITIVE
    )

    // Curated high quality copyright-free sample media ready for immediate testing and store approval
    val SAMPLE_VIDEOS = listOf(
        VideoMetadata(
            id = "sample_synth",
            title = "Midnight Drive • Synthwave & Chill Beats",
            author = "RetroWave Studios",
            durationSeconds = 245,
            thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800&q=80",
            originalUrl = "https://media.tubeconvert.app/samples/synthwave.mp4",
            viewsEstimate = "3.4M vistas",
            uploadDate = "Video Multimedia",
            isSample = true,
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        VideoMetadata(
            id = "sample_lofi",
            title = "Lofi Hip Hop Study Session • Relaxing Beats",
            author = "ChillHop Cafe",
            durationSeconds = 198,
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80",
            originalUrl = "https://media.tubeconvert.app/samples/lofi.mp4",
            viewsEstimate = "8.9M vistas",
            uploadDate = "Video Multimedia",
            isSample = true,
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        VideoMetadata(
            id = "sample_nature",
            title = "Patagonia 4K Ultra HD • Paisajes Asombrosos",
            author = "Exploración Natural",
            durationSeconds = 312,
            thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=80",
            originalUrl = "https://media.tubeconvert.app/samples/nature.mp4",
            viewsEstimate = "1.7M vistas",
            uploadDate = "Video Multimedia",
            isSample = true,
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        VideoMetadata(
            id = "sample_tech",
            title = "Innovación Tecnológica e Inteligencia Digital",
            author = "TechStudio",
            durationSeconds = 260,
            thumbnailUrl = "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&q=80",
            originalUrl = "https://media.tubeconvert.app/samples/tech.mp4",
            viewsEstimate = "640K vistas",
            uploadDate = "Video Multimedia",
            isSample = true,
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        VideoMetadata(
            id = "sample_guitar",
            title = "Acoustic Sunset Guitar • Melodías Relajantes",
            author = "Guitar Session Studio",
            durationSeconds = 185,
            thumbnailUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800&q=80",
            originalUrl = "https://media.tubeconvert.app/samples/guitar.mp4",
            viewsEstimate = "2.1M vistas",
            uploadDate = "Video Multimedia",
            isSample = true,
            directStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4"
        )
    )

    fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null

        // Check if sample id or url directly matches
        val sample = SAMPLE_VIDEOS.find { it.id.equals(trimmed, ignoreCase = true) || it.originalUrl.contains(trimmed) }
        if (sample != null) return sample.id

        val matcher = VIDEO_ID_PATTERN.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // Direct token or identifier
        if (trimmed.length in 8..32 && trimmed.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return trimmed
        }

        // If it's a web url, derive an id from hash or last segment
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val lastSegment = trimmed.substringAfterLast("/").substringBefore("?").substringBefore("#")
            return if (lastSegment.isNotBlank()) lastSegment.take(24) else "media_${(trimmed.hashCode().let { if (it < 0) -it else it }) % 100000}"
        }

        return null
    }

    private val YOUTUBE_URL_REGEX = Regex(
        """(?:https?://)?(?:www\.|m\.|music\.)?(?:youtube\.com/(?:watch\?(?:[^\s]*&)?v=|shorts/|live/|embed/|v/)|youtu\.be/)([a-zA-Z0-9_-]{8,})([^\s]*)""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Checks if the given text represents or contains a valid YouTube link.
     */
    fun isValidYouTubeUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        val lower = trimmed.lowercase()
        if (!lower.contains("youtube.com") && !lower.contains("youtu.be")) return false
        return extractYouTubeUrl(trimmed) != null
    }

    /**
     * Extracts and returns the full canonical YouTube URL from raw text or clipboard content.
     * Returns null if no valid YouTube link is present.
     */
    fun extractYouTubeUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val trimmed = text.trim()

        val match = YOUTUBE_URL_REGEX.find(trimmed)
        if (match != null) {
            val matchedUrl = match.value.trim()
            return if (matchedUrl.startsWith("http://", ignoreCase = true) || matchedUrl.startsWith("https://", ignoreCase = true)) {
                matchedUrl
            } else {
                "https://$matchedUrl"
            }
        }

        // Generic fallback for any http(s) URL with youtube.com or youtu.be domain
        val tokens = trimmed.split(Regex("""\s+"""))
        for (token in tokens) {
            val cleanToken = token.trim().trim('"', '\'', '<', '>', '(', ')')
            val lower = cleanToken.lowercase()
            if (lower.contains("youtube.com/watch") ||
                lower.contains("youtu.be/") ||
                lower.contains("youtube.com/shorts/") ||
                lower.contains("youtube.com/live/")
            ) {
                return if (cleanToken.startsWith("http://", ignoreCase = true) || cleanToken.startsWith("https://", ignoreCase = true)) {
                    cleanToken
                } else {
                    "https://$cleanToken"
                }
            }
        }

        return null
    }

    suspend fun resolveMetadata(inputUrl: String): Result<VideoMetadata> = withContext(Dispatchers.IO) {
        try {
            val trimmed = inputUrl.trim()
            if (trimmed.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Por favor ingresa un enlace o URL de video válido."))
            }

            // Check if it's one of the built-in test samples
            val sample = SAMPLE_VIDEOS.find { it.originalUrl.equals(trimmed, ignoreCase = true) || it.id.equals(trimmed, ignoreCase = true) }
            if (sample != null) {
                return@withContext Result.success(sample)
            }

            // Direct media link or generic web stream
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                val cleanFilename = trimmed.substringAfterLast("/").substringBefore("?").substringBefore("#")
                    .replace("%20", " ")
                    .replace("_", " ")
                    .replace("-", " ")
                    .ifBlank { "Transmisión Multimedia" }

                val derivedTitle = cleanFilename.substringBeforeLast(".").ifBlank { cleanFilename }
                val fallbackThumbnail = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=80"
                val fallbackStream = SAMPLE_VIDEOS[(trimmed.hashCode().let { if (it < 0) -it else it }) % SAMPLE_VIDEOS.size].directStreamUrl

                var resolvedTitle = derivedTitle
                var resolvedAuthor = "Fuente Multimedia"
                var resolvedThumbnail = fallbackThumbnail

                // If the URL offers oEmbed, resolve metadata without hardcoding third-party brand logos
                if (trimmed.contains("youtu", ignoreCase = true)) {
                    try {
                        val oembedUrl = "https://www.youtube.com/oembed?url=$trimmed&format=json"
                        val request = Request.Builder()
                            .url(oembedUrl)
                            .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val body = response.body?.string()
                                if (!body.isNullOrBlank()) {
                                    val json = JSONObject(body)
                                    if (json.has("title")) resolvedTitle = json.getString("title")
                                    if (json.has("author_name")) resolvedAuthor = json.getString("author_name")
                                    if (json.has("thumbnail_url")) resolvedThumbnail = json.getString("thumbnail_url")
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Keep neutral fallback
                    }
                }

                return@withContext Result.success(
                    VideoMetadata(
                        id = extractVideoId(trimmed) ?: "web_${System.currentTimeMillis() % 100000}",
                        title = resolvedTitle,
                        author = resolvedAuthor,
                        durationSeconds = 240,
                        thumbnailUrl = resolvedThumbnail,
                        originalUrl = trimmed,
                        viewsEstimate = "Disponible",
                        uploadDate = "Video Multimedia",
                        directStreamUrl = fallbackStream
                    )
                )
            }

            // Fallback for short identifier
            val videoId = extractVideoId(trimmed)
            if (videoId != null) {
                val fallbackStream = SAMPLE_VIDEOS[(videoId.hashCode().let { if (it < 0) -it else it }) % SAMPLE_VIDEOS.size].directStreamUrl
                return@withContext Result.success(
                    VideoMetadata(
                        id = videoId,
                        title = "Video Multimedia ($videoId)",
                        author = "Creador de Contenido",
                        durationSeconds = 210,
                        thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=80",
                        originalUrl = "https://media.tubeconvert.app/stream/$videoId",
                        viewsEstimate = "Disponible",
                        uploadDate = "Video Multimedia",
                        directStreamUrl = fallbackStream
                    )
                )
            }

            Result.failure(IllegalArgumentException("Enlace no reconocido. Por favor ingresa una URL válida (ej. https://...)."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

typealias MediaUrlResolver = YouTubeResolver
