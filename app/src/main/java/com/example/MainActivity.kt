package com.example

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.LaunchedEffect
import com.example.data.network.YouTubeResolver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.ConversionModalSheet
import com.example.ui.components.MediaPlayerSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Settings
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.sensor.ShakeDetector
import com.example.ui.components.FeedbackDialog
import com.example.data.ads.AdMobConfig
import com.example.ui.components.AdMobBanner
import com.example.ui.components.ThemeLanguageDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.getStrings
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.WarningAmber

enum class ScreenTab {
    HOME,
    DOWNLOADS,
    LIBRARY,
    CONVERTER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var clipboardManager: ClipboardManager? = null
    private var clipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var lastHandledClipboardUrl: String? = null
    private var sensorManager: SensorManager? = null
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google AdMob SDK with preloading for interstitial and rewarded ads
        com.example.data.ads.AdMobManager.initialize(this)

        // Setup system clipboard listener
        setupClipboardListener()

        // Setup accelerometer device sensor listener for Shake-to-Report feedback
        setupShakeDetector()

        // Handle shared media link or URL from external apps or web browser
        handleIncomingIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()
            val strings = getStrings(appLanguage)

            MyApplicationTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    MainAppContent(
                        viewModel = viewModel,
                        currentThemeMode = themeMode,
                        currentLanguage = appLanguage
                    )
                }
            }
        }
    }

    private fun setupClipboardListener() {
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            checkClipboardForYouTubeUrl()
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the clipboard listener while active in the foreground
        try {
            clipChangedListener?.let { listener ->
                clipboardManager?.addPrimaryClipChangedListener(listener)
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to add primary clip changed listener: ${e.message}")
        }

        // Check system clipboard immediately when coming into the foreground
        checkClipboardForYouTubeUrl()

        // Register shake sensor listener while app is active
        try {
            shakeDetector?.start(sensorManager)
        } catch (e: Exception) {
            Log.w("MainActivity", "Error starting shake detector: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister listener when moving to background to prevent resource leaks
        try {
            clipChangedListener?.let { listener ->
                clipboardManager?.removePrimaryClipChangedListener(listener)
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to remove primary clip changed listener: ${e.message}")
        }

        // Stop shake sensor listener when backgrounded to conserve battery
        try {
            shakeDetector?.stop(sensorManager)
        } catch (e: Exception) {
            Log.w("MainActivity", "Error stopping shake detector: ${e.message}")
        }
    }

    private fun setupShakeDetector() {
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            shakeDetector = ShakeDetector {
                handleShakeDetected()
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to setup shake detector: ${e.message}")
        }
    }

    private fun handleShakeDetected() {
        Log.i("MainActivity", "Device shake sensor event detected -> opening feedback dialog")
        triggerShakeHapticFeedback()
        viewModel.openFeedbackDialog()
    }

    private fun triggerShakeHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(120)
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Haptic vibration failed: ${e.message}")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Android 10+ restricts clipboard access unless the app currently holds window focus
        if (hasFocus) {
            checkClipboardForYouTubeUrl()
        }
    }

    /**
     * Inspects the system clipboard. If a valid YouTube link is detected,
     * automatically populates the URL input field in the ViewModel.
     */
    private fun checkClipboardForYouTubeUrl() {
        try {
            val cm = clipboardManager ?: (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager) ?: return
            if (!cm.hasPrimaryClip()) return

            val clip = cm.primaryClip ?: return
            if (clip.itemCount == 0) return

            val clipItem = clip.getItemAt(0) ?: return
            val text = clipItem.text?.toString() ?: clipItem.uri?.toString() ?: ""
            if (text.isBlank()) return

            val extractedUrl = YouTubeResolver.extractYouTubeUrl(text)
            if (extractedUrl != null && YouTubeResolver.isValidYouTubeUrl(extractedUrl)) {
                if (extractedUrl != lastHandledClipboardUrl) {
                    lastHandledClipboardUrl = extractedUrl
                    Log.d("MainActivity", "Valid YouTube link detected in clipboard: $extractedUrl")
                    viewModel.onClipboardYouTubeUrlDetected(extractedUrl)
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Error checking clipboard: ${e.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val extractedUrl = sharedText.split(" ", "\n").find {
                    it.startsWith("http://") || it.startsWith("https://")
                } ?: sharedText

                viewModel.onUrlChanged(extractedUrl.trim())
                viewModel.resolveCurrentUrl(extractedUrl.trim())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    currentThemeMode: ThemeMode,
    currentLanguage: AppLanguage
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val strings = LocalAppStrings.current
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var showPreferencesDialog by remember { mutableStateOf(false) }

    val clipboardNotice by viewModel.clipboardDetectedNotice.collectAsState()

    // Automatically navigate to HOME tab when a new clipboard YouTube link is detected
    LaunchedEffect(clipboardNotice) {
        if (clipboardNotice != null) {
            currentTab = ScreenTab.HOME
        }
    }

    val activeCount by viewModel.activeCount.collectAsState()
    val mediaForPlayback by viewModel.mediaForPlayback.collectAsState()
    val mediaForConversion by viewModel.mediaForConversion.collectAsState()
    val isFeedbackDialogOpen by viewModel.isFeedbackDialogOpen.collectAsState()

    val playerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val conversionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isSystemInDark = isSystemInDarkTheme()
    val isDarkNow = when (currentThemeMode) {
        ThemeMode.SYSTEM -> isSystemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.appName,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Global Theme Switcher Toggle button
                    IconButton(
                        onClick = { viewModel.toggleTheme(isDarkNow) },
                        modifier = Modifier.testTag("theme_switcher_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkNow) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkNow) strings.lightMode else strings.darkMode,
                            tint = if (isDarkNow) WarningAmber else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Settings Preferences Dialog button
                    IconButton(
                        onClick = { showPreferencesDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.preferences,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Multi-language Picker button
                    IconButton(
                        onClick = { showPreferencesDialog = true },
                        modifier = Modifier.testTag("language_switcher_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentLanguage.flag,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Automatic Bottom Banner Google AdMob
                AdMobBanner(
                    adUnitId = AdMobConfig.AD_UNIT_BANNER_VIP,
                    isBottomDocked = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("automatic_bottom_admob_banner")
                )

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                // Home Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { currentTab = ScreenTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = strings.tabDownload
                        )
                    },
                    label = { Text(strings.tabDownload, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_home"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Downloads Queue Tab with Badge
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DOWNLOADS,
                    onClick = { currentTab = ScreenTab.DOWNLOADS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("$activeCount", color = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.DOWNLOADS) Icons.Filled.Download else Icons.Outlined.Download,
                                contentDescription = strings.tabQueue
                            )
                        }
                    },
                    label = { Text(strings.tabQueue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_downloads"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Library Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.LIBRARY,
                    onClick = { currentTab = ScreenTab.LIBRARY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.LIBRARY) Icons.Filled.Folder else Icons.Outlined.Folder,
                            contentDescription = strings.tabLibrary
                        )
                    },
                    label = { Text(strings.tabLibrary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_library"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Converter Studio Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.CONVERTER,
                    onClick = { currentTab = ScreenTab.CONVERTER },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.CONVERTER) Icons.Filled.Sync else Icons.Outlined.Sync,
                            contentDescription = strings.tabConverter
                        )
                    },
                    label = { Text(strings.tabConverter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_converter"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToDownloads = { currentTab = ScreenTab.DOWNLOADS }
                    )
                }
                ScreenTab.DOWNLOADS -> {
                    DownloadsScreen(
                        viewModel = viewModel,
                        onNavigateToHome = { currentTab = ScreenTab.HOME }
                    )
                }
                ScreenTab.LIBRARY -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onNavigateToHome = { currentTab = ScreenTab.HOME }
                    )
                }
                ScreenTab.CONVERTER -> {
                    ConverterScreen(
                        viewModel = viewModel,
                        onNavigateToDownloads = { currentTab = ScreenTab.DOWNLOADS }
                    )
                }
            }

            // Media Player Modal Bottom Sheet
            mediaForPlayback?.let { item ->
                MediaPlayerSheet(
                    item = item,
                    sheetState = playerSheetState,
                    onDismiss = { viewModel.closePlayer() }
                )
            }

            // Quick Format Conversion Modal Bottom Sheet
            mediaForConversion?.let { item ->
                ConversionModalSheet(
                    item = item,
                    sheetState = conversionSheetState,
                    onDismiss = { viewModel.closeConverter() },
                    onConfirmConvert = { mediaType, videoFormat, videoRes, audioFormat, audioQuality ->
                        if (activity != null) {
                            com.example.data.ads.AdMobManager.showInterstitialAd(activity, com.example.data.ads.AdMobConfig.AD_UNIT_TAXIGORD_V1) {
                                viewModel.convertMedia(
                                    sourceEntity = item,
                                    targetMediaType = mediaType,
                                    targetVideoFormat = videoFormat,
                                    targetVideoResolution = videoRes,
                                    targetAudioFormat = audioFormat,
                                    targetAudioQuality = audioQuality,
                                    onCreated = {
                                        currentTab = ScreenTab.DOWNLOADS
                                    }
                                )
                            }
                        } else {
                            viewModel.convertMedia(
                                sourceEntity = item,
                                targetMediaType = mediaType,
                                targetVideoFormat = videoFormat,
                                targetVideoResolution = videoRes,
                                targetAudioFormat = audioFormat,
                                targetAudioQuality = audioQuality,
                                onCreated = {
                                    currentTab = ScreenTab.DOWNLOADS
                                }
                            )
                        }
                    }
                )
            }

            // Global Theme & Language Preferences Dialog
            if (showPreferencesDialog) {
                ThemeLanguageDialog(
                    currentThemeMode = currentThemeMode,
                    currentLanguage = currentLanguage,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onDismissRequest = { showPreferencesDialog = false },
                    onOpenFeedback = { viewModel.openFeedbackDialog() }
                )
            }

            // Device Sensor Shake-to-Report Feedback Dialog
            FeedbackDialog(
                isOpen = isFeedbackDialogOpen,
                onDismiss = { viewModel.closeFeedbackDialog() },
                onSubmit = { category, message, includeDeviceInfo ->
                    viewModel.submitFeedback(category, message, includeDeviceInfo)
                }
            )
        }
    }
}

