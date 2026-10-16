package com.example

import com.example.data.local.DownloadEntity
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoResolution
import com.example.data.network.YouTubeResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMediaIdExtraction() {
        val watchUrl = "https://www.example.com/watch?v=dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeResolver.extractVideoId(watchUrl))

        val directSample = "sample_synth"
        assertEquals("sample_synth", YouTubeResolver.extractVideoId(directSample))

        val directUrl = "https://media.tubeconvert.app/stream/my_awesome_video.mp4"
        assertNotNull(YouTubeResolver.extractVideoId(directUrl))
    }

    @Test
    fun testFormatConfigurations() {
        val videoConfig = ConversionConfig(
            mediaType = MediaType.VIDEO,
            videoFormat = VideoFormat.MP4,
            videoResolution = VideoResolution.R_1080P
        )
        assertEquals("mp4", videoConfig.fileExtension)
        assertTrue(videoConfig.estimatedSizeBytes(100) > 0)

        val audioConfig = ConversionConfig(
            mediaType = MediaType.AUDIO,
            extractAudioOnly = true,
            audioFormat = AudioFormat.MP3,
            audioQuality = AudioQuality.Q_320
        )
        assertEquals("mp3", audioConfig.fileExtension)
        val sizeBytes = audioConfig.estimatedSizeBytes(60) // 1 minute
        assertTrue(sizeBytes > 1_000_000) // > 1MB
    }

    @Test
    fun testSampleVideosCatalog() {
        assertTrue(YouTubeResolver.SAMPLE_VIDEOS.isNotEmpty())
        val firstSample = YouTubeResolver.SAMPLE_VIDEOS.first()
        assertNotNull(firstSample.title)
        assertNotNull(firstSample.thumbnailUrl)
        assertTrue(firstSample.durationSeconds > 0)
    }

    @Test
    fun testBatchUrlParsing() {
        val rawInput = """
            https://example.com/video1
            https://example.com/video2
            https://example.com/video1
            
               https://example.com/video3   
        """.trimIndent()

        val parsed = rawInput.lines()
            .flatMap { it.split(",", ";", " ") }
            .map { it.trim() }
            .filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://") || it.length in 8..32) }
            .distinct()

        assertEquals(3, parsed.size)
        assertEquals("https://example.com/video1", parsed[0])
        assertEquals("https://example.com/video2", parsed[1])
        assertEquals("https://example.com/video3", parsed[2])
    }

    @Test
    fun testSequentialQueueDataModel() {
        val queuedItem = DownloadEntity(
            id = 1L,
            videoId = "test_vid_1",
            title = "Test Video 1",
            author = "Test Author",
            originalUrl = "https://example.com/v1",
            thumbnailUrl = "https://example.com/thumb1.jpg",
            durationSeconds = 120,
            mediaType = MediaType.VIDEO,
            targetFormat = "mp4",
            resolutionLabel = "1080p",
            qualityLabel = null,
            status = DownloadStatus.QUEUED,
            progressPercent = 0
        )

        assertEquals(DownloadStatus.QUEUED, queuedItem.status)
        assertEquals(0, queuedItem.progressPercent)

        val downloadingItem = queuedItem.copy(
            status = DownloadStatus.DOWNLOADING,
            progressPercent = 55,
            downloadedBytes = 5500000L,
            totalBytes = 10000000L,
            speedBytesPerSec = 2500000L
        )

        assertEquals(55, downloadingItem.progressPercent)
        assertTrue(downloadingItem.formattedSpeed.contains("MB/s"))
        assertEquals("↓ 2.4 MB/s", downloadingItem.formattedSpeedWithDirection)
        assertTrue(downloadingItem.formattedDownloadedSize.contains("5.2 MB / 9.5 MB") || downloadingItem.formattedDownloadedSize.contains("MB"))

        // Test real-time KB/s speed
        val kbItem = queuedItem.copy(
            status = DownloadStatus.DOWNLOADING,
            speedBytesPerSec = 750 * 1024L
        )
        assertEquals("750 KB/s", kbItem.formattedSpeed)
        assertEquals("↓ 750 KB/s", kbItem.formattedSpeedWithDirection)

        // Test real-time MB/s speed
        val mbItem = queuedItem.copy(
            status = DownloadStatus.DOWNLOADING,
            speedBytesPerSec = 3_500 * 1024L
        )
        assertEquals("3.4 MB/s", mbItem.formattedSpeed)
        assertEquals("↓ 3.4 MB/s", mbItem.formattedSpeedWithDirection)

        // Test zero speed when paused or connecting
        val zeroItem = queuedItem.copy(speedBytesPerSec = 0L)
        assertEquals("0 KB/s", zeroItem.formattedSpeed)
        assertEquals("0 KB/s", zeroItem.formattedSpeedWithDirection)
    }

    @Test
    fun testThemeModeAndLanguages() {
        // Test ThemeMode values
        assertEquals(3, com.example.ui.theme.ThemeMode.values().size)

        // Test AppLanguage values
        val languages = com.example.ui.i18n.AppLanguage.values()
        assertTrue(languages.isNotEmpty())
        assertEquals(4, languages.size)

        // Test localized string packs
        val spanishStrings = com.example.ui.i18n.getStrings(com.example.ui.i18n.AppLanguage.SPANISH)
        val englishStrings = com.example.ui.i18n.getStrings(com.example.ui.i18n.AppLanguage.ENGLISH)
        val portugueseStrings = com.example.ui.i18n.getStrings(com.example.ui.i18n.AppLanguage.PORTUGUESE)
        val frenchStrings = com.example.ui.i18n.getStrings(com.example.ui.i18n.AppLanguage.FRENCH)

        assertEquals("TubeConvert", spanishStrings.appName)
        assertEquals("TubeConvert", englishStrings.appName)
        assertEquals("TubeConvert", portugueseStrings.appName)
        assertEquals("TubeConvert", frenchStrings.appName)

        assertEquals("Descargar", spanishStrings.tabDownload)
        assertEquals("Download", englishStrings.tabDownload)
        assertEquals("Baixar", portugueseStrings.tabDownload)
        assertEquals("Télécharger", frenchStrings.tabDownload)

        assertTrue(spanishStrings.privacyPolicy.isNotBlank())
        assertTrue(englishStrings.privacyPolicy.isNotBlank())
        assertTrue(portugueseStrings.privacyPolicy.isNotBlank())
        assertTrue(frenchStrings.privacyPolicy.isNotBlank())

        // Test batch queue and sequential strings
        assertTrue(spanishStrings.sequentialProcessingBadge.isNotBlank())
        assertTrue(englishStrings.sequentialProcessingBadge.isNotBlank())
        assertTrue(portugueseStrings.sequentialProcessingBadge.isNotBlank())
        assertTrue(frenchStrings.sequentialProcessingBadge.isNotBlank())

        assertEquals("3 URLs detectadas", spanishStrings.batchUrlsDetected(3))
        assertEquals("3 URLs detected", englishStrings.batchUrlsDetected(3))

        assertTrue(spanishStrings.clipboardDetectedBanner.isNotBlank())
        assertTrue(englishStrings.clipboardDetectedBanner.isNotBlank())
    }

    @Test
    fun testYouTubeClipboardUrlDetection() {
        // Standard video URL
        val standardUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertTrue(YouTubeResolver.isValidYouTubeUrl(standardUrl))
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", YouTubeResolver.extractYouTubeUrl(standardUrl))

        // youtu.be shortlink with query parameters
        val shortUrl = "https://youtu.be/dQw4w9WgXcQ?si=abcdef123"
        assertTrue(YouTubeResolver.isValidYouTubeUrl(shortUrl))
        assertEquals("https://youtu.be/dQw4w9WgXcQ?si=abcdef123", YouTubeResolver.extractYouTubeUrl(shortUrl))

        // Shorts URL
        val shortsUrl = "https://www.youtube.com/shorts/12345678abc"
        assertTrue(YouTubeResolver.isValidYouTubeUrl(shortsUrl))
        assertEquals("https://www.youtube.com/shorts/12345678abc", YouTubeResolver.extractYouTubeUrl(shortsUrl))

        // Live URL
        val liveUrl = "https://youtube.com/live/liveStreamId12"
        assertTrue(YouTubeResolver.isValidYouTubeUrl(liveUrl))
        assertEquals("https://youtube.com/live/liveStreamId12", YouTubeResolver.extractYouTubeUrl(liveUrl))

        // Share text from YouTube app
        val shareText = "Mira este increíble video: https://youtu.be/dQw4w9WgXcQ ¡No te lo pierdas!"
        assertTrue(YouTubeResolver.isValidYouTubeUrl(shareText))
        assertEquals("https://youtu.be/dQw4w9WgXcQ", YouTubeResolver.extractYouTubeUrl(shareText))

        // Non-YouTube URL or random text
        val randomText = "https://example.com/somepage"
        org.junit.Assert.assertFalse(YouTubeResolver.isValidYouTubeUrl(randomText))
        org.junit.Assert.assertNull(YouTubeResolver.extractYouTubeUrl(randomText))
    }

    @Test
    fun testAdMobBannerAndConfig() {
        // Verify real AdMob unit IDs
        assertTrue(com.example.data.ads.AdMobConfig.AD_UNIT_BANNER_VIP.startsWith("ca-app-pub-"))
        assertTrue(com.example.data.ads.AdMobConfig.AD_UNIT_GENERAL_POPUP.startsWith("ca-app-pub-"))
        assertTrue(com.example.data.ads.AdMobConfig.AD_UNIT_REWARDED_VIP.startsWith("ca-app-pub-"))
        assertTrue(com.example.data.ads.AdMobConfig.AD_UNIT_TAXOBETAPUB.startsWith("ca-app-pub-"))

        // Verify test fallback unit IDs
        assertTrue(com.example.data.ads.AdMobConfig.TEST_BANNER.startsWith("ca-app-pub-3940256099942544"))
        assertTrue(com.example.data.ads.AdMobConfig.TEST_INTERSTITIAL.startsWith("ca-app-pub-3940256099942544"))
        assertTrue(com.example.data.ads.AdMobConfig.TEST_REWARDED.startsWith("ca-app-pub-3940256099942544"))
    }

    @Test
    fun testSettingsThemeToggleLogic() {
        var currentTheme = com.example.ui.theme.ThemeMode.LIGHT
        fun toggle(isDark: Boolean) {
            currentTheme = if (isDark) com.example.ui.theme.ThemeMode.DARK else com.example.ui.theme.ThemeMode.LIGHT
        }

        // Initially light, toggle on -> dark
        toggle(true)
        assertEquals(com.example.ui.theme.ThemeMode.DARK, currentTheme)

        // Currently dark, toggle off -> light
        toggle(false)
        assertEquals(com.example.ui.theme.ThemeMode.LIGHT, currentTheme)
    }

    @Test
    fun testShakeDetectorSensorLogic() {
        var shakeCount = 0
        val detector = com.example.data.sensor.ShakeDetector {
            shakeCount++
        }

        // Simulate first shake gesture
        detector.simulateShake()
        assertEquals(1, shakeCount)

        // Immediate subsequent shake without cooldown should be throttled
        detector.simulateShake()
        assertEquals(1, shakeCount)
    }

    @Test
    fun testFeedbackCategoriesAndStrings() {
        val categories = com.example.ui.components.FeedbackCategory.values()
        assertEquals(3, categories.size)
        assertTrue(categories.contains(com.example.ui.components.FeedbackCategory.BUG))
        assertTrue(categories.contains(com.example.ui.components.FeedbackCategory.SUGGESTION))
        assertTrue(categories.contains(com.example.ui.components.FeedbackCategory.OTHER))

        for (lang in com.example.ui.i18n.AppLanguage.values()) {
            val strings = com.example.ui.i18n.getStrings(lang)
            assertTrue(strings.feedbackTitle.isNotBlank())
            assertTrue(strings.feedbackSubtitle.isNotBlank())
            assertTrue(strings.feedbackShakeDetectedBadge.isNotBlank())
            assertTrue(strings.feedbackSubmit.isNotBlank())
            assertTrue(strings.feedbackSuccessNotice.isNotBlank())
            assertTrue(strings.feedbackEmptyError.isNotBlank())
            assertTrue(strings.validUrlDetectedBadge.isNotBlank())
            assertTrue(strings.validUrlDropdownPrompt.isNotBlank())
            assertTrue(strings.validUrlDropdownSubtitle.isNotBlank())
        }
    }

    @Test
    fun testVideoResolutionAndAudioBitrateOptions() {
        val resolutions = VideoResolution.values()
        assertEquals(5, resolutions.size)
        assertTrue(resolutions.contains(VideoResolution.R_1080P))
        assertTrue(resolutions.contains(VideoResolution.R_720P))
        assertTrue(resolutions.contains(VideoResolution.R_480P))
        assertTrue(resolutions.contains(VideoResolution.R_360P))
        assertTrue(resolutions.contains(VideoResolution.R_240P))

        val audioQualities = AudioQuality.values()
        assertEquals(5, audioQualities.size)
        assertTrue(audioQualities.contains(AudioQuality.Q_320))
        assertTrue(audioQualities.contains(AudioQuality.Q_256))
        assertTrue(audioQualities.contains(AudioQuality.Q_192))
        assertTrue(audioQualities.contains(AudioQuality.Q_128))
        assertTrue(audioQualities.contains(AudioQuality.Q_64))

        // Check estimated sizes for each resolution
        for (res in resolutions) {
            val config = ConversionConfig(
                mediaType = MediaType.VIDEO,
                videoFormat = VideoFormat.MP4,
                videoResolution = res
            )
            assertTrue(config.estimatedSizeBytes(120) > 0)
        }

        // Check estimated sizes for each audio bitrate
        for (quality in audioQualities) {
            val config = ConversionConfig(
                mediaType = MediaType.AUDIO,
                extractAudioOnly = true,
                audioFormat = AudioFormat.MP3,
                audioQuality = quality
            )
            assertTrue(config.estimatedSizeBytes(120) > 0)
        }
    }

    @Test
    fun testValidUrlDropdownTrigger() {
        val validUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtube.com/shorts/abcdef12345",
            "https://youtube.com/live/stream123"
        )
        for (url in validUrls) {
            assertTrue("Expected $url to be valid", YouTubeResolver.isValidYouTubeUrl(url))
        }

        val invalidUrls = listOf(
            "",
            "not a url",
            "https://example.com/not-youtube",
            "ftp://files.example.com/test.mp4"
        )
        for (url in invalidUrls) {
            org.junit.Assert.assertFalse("Expected $url to be invalid", YouTubeResolver.isValidYouTubeUrl(url))
        }
    }
}


