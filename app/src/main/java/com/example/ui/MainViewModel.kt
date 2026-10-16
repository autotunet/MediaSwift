package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.converter.MediaConverterEngine
import com.example.data.download.DownloadManagerEngine
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoMetadata
import com.example.data.model.VideoResolution
import com.example.data.network.YouTubeResolver
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryFilter {
    ALL,
    VIDEOS,
    AUDIOS
}

enum class InputMode {
    SINGLE,
    BATCH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("tubeconvert_prefs", android.content.Context.MODE_PRIVATE)

    // Global Theme Mode State (SYSTEM, LIGHT, DARK)
    private val _themeMode = MutableStateFlow(
        try {
            val saved = prefs.getString("pref_theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(saved)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Global App Language State (SPANISH, ENGLISH, PORTUGUESE, FRENCH)
    private val _appLanguage = MutableStateFlow(
        try {
            val saved = prefs.getString("pref_app_language", AppLanguage.SPANISH.code) ?: AppLanguage.SPANISH.code
            AppLanguage.fromCode(saved)
        } catch (e: Exception) {
            AppLanguage.SPANISH
        }
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val database = AppDatabase.getInstance(application)
    private val downloadDao = database.downloadDao()
    private val downloadEngine = DownloadManagerEngine(application, database)
    private val converterEngine = MediaConverterEngine(application, database)

    // AdMob VIP Turbo Mode (unlocked by Rewarded Ads)
    val isTurboModeActive: StateFlow<Boolean> = com.example.data.ads.AdMobManager.isTurboModeActive

    // Sequential Queue State
    val isQueuePaused: StateFlow<Boolean> = downloadEngine.isQueuePaused

    // Input mode selector: Single URL vs Batch URLs
    private val _inputMode = MutableStateFlow(InputMode.SINGLE)
    val inputMode: StateFlow<InputMode> = _inputMode.asStateFlow()

    // Input & Resolver State for Single URL
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isResolvingUrl = MutableStateFlow(false)
    val isResolvingUrl: StateFlow<Boolean> = _isResolvingUrl.asStateFlow()

    private val _resolvedMetadata = MutableStateFlow<VideoMetadata?>(null)
    val resolvedMetadata: StateFlow<VideoMetadata?> = _resolvedMetadata.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Clipboard Auto-Detection Notice
    private val _clipboardDetectedNotice = MutableStateFlow<String?>(null)
    val clipboardDetectedNotice: StateFlow<String?> = _clipboardDetectedNotice.asStateFlow()

    // Batch URLs Input State
    private val _batchUrlsInput = MutableStateFlow("")
    val batchUrlsInput: StateFlow<String> = _batchUrlsInput.asStateFlow()

    private val _isBatchResolving = MutableStateFlow(false)
    val isBatchResolving: StateFlow<Boolean> = _isBatchResolving.asStateFlow()

    private val _batchResolvingProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val batchResolvingProgress: StateFlow<Pair<Int, Int>?> = _batchResolvingProgress.asStateFlow()

    private val _batchErrorMessage = MutableStateFlow<String?>(null)
    val batchErrorMessage: StateFlow<String?> = _batchErrorMessage.asStateFlow()

    private val _showBatchDialog = MutableStateFlow(false)
    val showBatchDialog: StateFlow<Boolean> = _showBatchDialog.asStateFlow()

    // Selected Conversion Configuration
    private val _conversionConfig = MutableStateFlow(
        ConversionConfig(
            mediaType = MediaType.VIDEO,
            videoFormat = VideoFormat.MP4,
            videoResolution = VideoResolution.R_720P,
            audioFormat = AudioFormat.MP3,
            audioQuality = AudioQuality.Q_320
        )
    )
    val conversionConfig: StateFlow<ConversionConfig> = _conversionConfig.asStateFlow()

    // Active Downloads Queue from Room (prioritized: active downloading item first, then queued in FIFO)
    val activeDownloads: StateFlow<List<DownloadEntity>> = downloadDao.getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = downloadDao.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val queuedCount: StateFlow<Int> = downloadDao.getQueuedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalActiveAndQueuedCount: StateFlow<Int> = downloadDao.getTotalActiveAndQueuedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Completed Library Items
    private val _libraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val libraryFilter: StateFlow<LibraryFilter> = _libraryFilter.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    val filteredLibraryItems: StateFlow<List<DownloadEntity>> = combine(
        downloadDao.getCompletedDownloads(),
        _libraryFilter,
        _librarySearchQuery
    ) { items, filter, query ->
        items.filter { item ->
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.VIDEOS -> item.mediaType == MediaType.VIDEO
                LibraryFilter.AUDIOS -> item.mediaType == MediaType.AUDIO
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.author.contains(query, ignoreCase = true) ||
                    item.targetFormat.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Player and Converter Sheet states
    private val _mediaForPlayback = MutableStateFlow<DownloadEntity?>(null)
    val mediaForPlayback: StateFlow<DownloadEntity?> = _mediaForPlayback.asStateFlow()

    private val _mediaForConversion = MutableStateFlow<DownloadEntity?>(null)
    val mediaForConversion: StateFlow<DownloadEntity?> = _mediaForConversion.asStateFlow()

    init {
        // Automatically preselect the first sample video
        val defaultSample = YouTubeResolver.SAMPLE_VIDEOS.first()
        _urlInput.value = defaultSample.originalUrl
        _resolvedMetadata.value = defaultSample
    }

    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
    }

    fun setShowBatchDialog(show: Boolean) {
        _showBatchDialog.value = show
        if (!show) {
            _batchErrorMessage.value = null
        }
    }

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
        _errorMessage.value = null
        if (newUrl.isBlank()) {
            _resolvedMetadata.value = null
        }
    }

    /**
     * Automatically populates the URL input field and resolves metadata
     * when a valid YouTube link is detected from the system clipboard.
     */
    fun onClipboardYouTubeUrlDetected(url: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return

        // Switch to single URL mode so the input is immediately visible
        _inputMode.value = InputMode.SINGLE
        _urlInput.value = cleanUrl
        _errorMessage.value = null
        _clipboardDetectedNotice.value = cleanUrl

        // Automatically resolve metadata for the detected URL
        resolveCurrentUrl(cleanUrl)
    }

    fun dismissClipboardNotice() {
        _clipboardDetectedNotice.value = null
    }

    fun onBatchUrlsChanged(newText: String) {
        _batchUrlsInput.value = newText
        _batchErrorMessage.value = null
    }

    fun clearBatchUrls() {
        _batchUrlsInput.value = ""
        _batchErrorMessage.value = null
    }

    fun loadBatchSamples() {
        val sampleUrls = YouTubeResolver.SAMPLE_VIDEOS.take(3).joinToString("\n") { it.originalUrl }
        _batchUrlsInput.value = sampleUrls
        _batchErrorMessage.value = null
    }

    fun parseBatchUrls(text: String): List<String> {
        return text.lines()
            .flatMap { it.split(",", ";", " ") }
            .map { it.trim() }
            .filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://") || it.length in 8..32) }
            .distinct()
    }

    fun resolveCurrentUrl(urlToResolve: String = _urlInput.value) {
        if (urlToResolve.isBlank()) {
            _errorMessage.value = "Por favor ingresa un enlace o URL de video válido"
            return
        }

        viewModelScope.launch {
            _isResolvingUrl.value = true
            _errorMessage.value = null

            val result = YouTubeResolver.resolveMetadata(urlToResolve)
            result.onSuccess { meta ->
                _resolvedMetadata.value = meta
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "No se pudo obtener información del video"
            }

            _isResolvingUrl.value = false
        }
    }

    fun selectSampleVideo(sample: VideoMetadata) {
        _urlInput.value = sample.originalUrl
        _resolvedMetadata.value = sample
        _errorMessage.value = null
    }

    fun updateConversionConfig(newConfig: ConversionConfig) {
        _conversionConfig.value = newConfig
    }

    fun startDownload(onSuccessNavToDownloads: () -> Unit) {
        val meta = _resolvedMetadata.value
        if (meta == null) {
            _errorMessage.value = "Primero analiza un enlace válido"
            return
        }

        viewModelScope.launch {
            downloadEngine.enqueueDownload(meta, _conversionConfig.value)
            onSuccessNavToDownloads()
        }
    }

    fun addToQueue(onSuccessNavToDownloads: () -> Unit) {
        startDownload(onSuccessNavToDownloads)
    }

    fun addBatchUrlsToQueue(onSuccessNavToDownloads: () -> Unit) {
        val urls = parseBatchUrls(_batchUrlsInput.value)
        if (urls.isEmpty()) {
            _batchErrorMessage.value = "Ingresa al menos una URL válida (ej. https://...)"
            return
        }

        viewModelScope.launch {
            _isBatchResolving.value = true
            _batchErrorMessage.value = null

            val resolvedList = mutableListOf<Pair<VideoMetadata, ConversionConfig>>()
            val total = urls.size

            for ((index, url) in urls.withIndex()) {
                _batchResolvingProgress.value = Pair(index + 1, total)
                val result = YouTubeResolver.resolveMetadata(url)
                val metadata = result.getOrElse {
                    val id = YouTubeResolver.extractVideoId(url) ?: "item_${(url.hashCode().let { if (it < 0) -it else it }) % 100000}"
                    val sample = YouTubeResolver.SAMPLE_VIDEOS[(url.hashCode().let { if (it < 0) -it else it }) % YouTubeResolver.SAMPLE_VIDEOS.size]
                    VideoMetadata(
                        id = id,
                        title = sample.title,
                        author = sample.author,
                        durationSeconds = sample.durationSeconds,
                        thumbnailUrl = sample.thumbnailUrl,
                        originalUrl = url,
                        directStreamUrl = sample.directStreamUrl
                    )
                }
                resolvedList.add(metadata to _conversionConfig.value)
            }

            downloadEngine.enqueueBatchDownloads(resolvedList)
            _isBatchResolving.value = false
            _batchResolvingProgress.value = null
            _batchUrlsInput.value = ""
            _showBatchDialog.value = false
            onSuccessNavToDownloads()
        }
    }

    fun pauseQueue() {
        downloadEngine.pauseQueue()
    }

    fun resumeQueue() {
        downloadEngine.resumeQueue()
    }

    fun pauseDownload(id: Long) {
        downloadEngine.pauseDownload(id)
    }

    fun resumeDownload(id: Long) {
        downloadEngine.resumeDownload(id)
    }

    fun cancelDownload(id: Long) {
        downloadEngine.cancelDownload(id)
    }

    fun deleteLibraryItem(item: DownloadEntity) {
        viewModelScope.launch {
            downloadDao.deleteDownload(item)
        }
    }

    fun clearFailedDownloads() {
        viewModelScope.launch {
            downloadDao.clearFailed()
        }
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun openPlayer(item: DownloadEntity) {
        _mediaForPlayback.value = item
    }

    fun closePlayer() {
        _mediaForPlayback.value = null
    }

    fun openConverter(item: DownloadEntity) {
        _mediaForConversion.value = item
    }

    fun closeConverter() {
        _mediaForConversion.value = null
    }

    fun convertMedia(
        sourceEntity: DownloadEntity,
        targetMediaType: MediaType,
        targetVideoFormat: VideoFormat?,
        targetVideoResolution: VideoResolution?,
        targetAudioFormat: AudioFormat?,
        targetAudioQuality: AudioQuality?,
        onCreated: (Long) -> Unit
    ) {
        converterEngine.convertMedia(
            sourceEntity = sourceEntity,
            targetMediaType = targetMediaType,
            targetVideoFormat = targetVideoFormat,
            targetVideoResolution = targetVideoResolution,
            targetAudioFormat = targetAudioFormat,
            targetAudioQuality = targetAudioQuality,
            onCreated = onCreated
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("pref_theme_mode", mode.name).apply()
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        val nextMode = if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(nextMode)
    }

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("pref_app_language", language.code).apply()
    }

    fun showRewardedAd(activity: android.app.Activity, onCompleted: () -> Unit = {}) {
        com.example.data.ads.AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = onCompleted,
            onClosed = {}
        )
    }

    // Shake-to-Report Feedback Dialog State
    private val _isFeedbackDialogOpen = MutableStateFlow(false)
    val isFeedbackDialogOpen: StateFlow<Boolean> = _isFeedbackDialogOpen.asStateFlow()

    private val _lastSubmittedFeedback = MutableStateFlow<String?>(null)
    val lastSubmittedFeedback: StateFlow<String?> = _lastSubmittedFeedback.asStateFlow()

    fun openFeedbackDialog() {
        _isFeedbackDialogOpen.value = true
    }

    fun closeFeedbackDialog() {
        _isFeedbackDialogOpen.value = false
    }

    fun submitFeedback(
        category: com.example.ui.components.FeedbackCategory,
        message: String,
        includeDeviceInfo: Boolean
    ) {
        val deviceInfo = if (includeDeviceInfo) {
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE}, API ${android.os.Build.VERSION.SDK_INT})"
        } else {
            "Omitted"
        }
        val feedbackSummary = "[${category.name}] $message (Device: $deviceInfo)"
        android.util.Log.i("MainViewModel", "User feedback received via shake gesture: $feedbackSummary")

        prefs.edit().putLong("last_feedback_timestamp", System.currentTimeMillis()).apply()
        _lastSubmittedFeedback.value = feedbackSummary
    }
}
