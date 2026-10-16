package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ads.AdMobConfig
import com.example.data.ads.AdMobManager
import com.example.data.network.YouTubeResolver
import com.example.ui.InputMode
import com.example.ui.MainViewModel
import com.example.ui.components.AdMobBanner
import com.example.ui.components.BatchUrlQueueCard
import com.example.ui.components.FormatQualitySelector
import com.example.ui.components.FormatResolutionSelector
import com.example.ui.components.QualityResolutionDropdownSelector
import com.example.ui.components.StoragePermissionHandler
import com.example.ui.components.TaskProgressBanner
import com.example.ui.components.VideoMetadataCard
import com.example.ui.components.YouTubeUrlInputCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val urlInput by viewModel.urlInput.collectAsState()
    val isResolving by viewModel.isResolvingUrl.collectAsState()
    val metadata by viewModel.resolvedMetadata.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val conversionConfig by viewModel.conversionConfig.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val clipboardNotice by viewModel.clipboardDetectedNotice.collectAsState()
    val isTurboModeActive by viewModel.isTurboModeActive.collectAsState()
    val isInputUrlValid = remember(urlInput) { YouTubeResolver.isValidYouTubeUrl(urlInput) }

    val batchText by viewModel.batchUrlsInput.collectAsState()
    val isBatchResolving by viewModel.isBatchResolving.collectAsState()
    val batchResolvingProgress by viewModel.batchResolvingProgress.collectAsState()
    val batchError by viewModel.batchErrorMessage.collectAsState()
    val detectedBatchUrls = viewModel.parseBatchUrls(batchText)

    val activity = context as? Activity
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val primaryActiveTask = activeDownloads.firstOrNull {
        it.status == com.example.data.model.DownloadStatus.DOWNLOADING ||
        it.status == com.example.data.model.DownloadStatus.CONVERTING ||
        it.status == com.example.data.model.DownloadStatus.CONNECTING
    } ?: activeDownloads.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_screen")
    ) {
        // Hero Visual Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_banner),
                contentDescription = "TubeConvert Studio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F091A).copy(alpha = 0.88f),
                                Color(0xFF0F091A).copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .align(Alignment.CenterStart)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = strings.heroBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.heroTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = strings.heroSubtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Live Download / Conversion Task Progress Banner with LinearProgressIndicator
        if (primaryActiveTask != null) {
            Spacer(modifier = Modifier.height(14.dp))
            TaskProgressBanner(
                activeItem = primaryActiveTask,
                onViewQueueClick = onNavigateToDownloads,
                onPause = { viewModel.pauseDownload(it) },
                onResume = { viewModel.resumeDownload(it) },
                onCancel = { viewModel.cancelDownload(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local Storage & Notification Permission Requester Card
        StoragePermissionHandler(
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // AdMob VIP Turbo Mode Rewarded Ad Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .testTag("admob_turbo_mode_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTurboModeActive) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, if (isTurboModeActive) SuccessGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isTurboModeActive) SuccessGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTurboModeActive) Icons.Default.Bolt else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.turboModeTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isTurboModeActive) SuccessGreen else MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = if (isTurboModeActive) strings.turboModeActiveBadge else "VIP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = strings.turboModeDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isTurboModeActive && activity != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.showRewardedAd(activity) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("unlock_turbo_rewarded_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.turboModeUnlockBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mode Selector: Single URL vs Batch URLs Queue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.setInputMode(InputMode.SINGLE) }
                    .testTag("mode_single_url_button"),
                shape = RoundedCornerShape(12.dp),
                color = if (inputMode == InputMode.SINGLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = strings.modeSingleUrl,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inputMode == InputMode.SINGLE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.setInputMode(InputMode.BATCH) }
                    .testTag("mode_batch_urls_button"),
                shape = RoundedCornerShape(12.dp),
                color = if (inputMode == InputMode.BATCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Queue,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (inputMode == InputMode.BATCH) Color.White else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.modeBatchUrls,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inputMode == InputMode.BATCH) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (inputMode == InputMode.SINGLE) {
            // Visual alert banner when a YouTube link was auto-detected and populated from the system clipboard
            AnimatedVisibility(
                visible = clipboardNotice != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("clipboard_detected_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentPasteGo,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = strings.clipboardDetectedBadge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.clipboardDetectedBanner,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = clipboardNotice ?: "",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.dismissClipboardNotice() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("dismiss_clipboard_banner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.close,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Single URL input card
            YouTubeUrlInputCard(
                urlValue = urlInput,
                onUrlChange = { viewModel.onUrlChanged(it) },
                onFetchClick = {
                    if (activity != null) {
                        AdMobManager.showInterstitialAd(activity, AdMobConfig.AD_UNIT_IMPORT_URL) {
                            viewModel.resolveCurrentUrl()
                        }
                    } else {
                        viewModel.resolveCurrentUrl()
                    }
                },
                isLoading = isResolving,
                errorMessage = errorMessage
            )

            // Dropdown menu for selecting video resolution (e.g., 1080p, 720p, 480p) or audio bitrate, triggered when a URL is valid
            AnimatedVisibility(
                visible = isInputUrlValid,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .testTag("valid_url_dropdown_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Badge indicating Valid URL detected
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("valid_url_detected_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.validUrlDetectedBadge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Text(
                                text = if (conversionConfig.extractAudioOnly) strings.modeAudioOnly else strings.modeVideo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (conversionConfig.extractAudioOnly) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = strings.validUrlDropdownPrompt,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.validUrlDropdownSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dropdown menu for selecting video resolution (1080p, 720p, 480p, 360p, 240p) or audio bitrate
                        QualityResolutionDropdownSelector(
                            config = conversionConfig,
                            durationSeconds = metadata?.durationSeconds ?: 214L,
                            onConfigChange = { viewModel.updateConversionConfig(it) },
                            showModeToggle = true,
                            modifier = Modifier.testTag("valid_url_resolution_dropdown")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick sample videos chips
            Text(
                text = strings.sampleVideosLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                YouTubeResolver.SAMPLE_VIDEOS.forEach { sample ->
                    val isSelected = metadata?.id == sample.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectSampleVideo(sample) }
                            .testTag("sample_chip_${sample.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sample.title.take(22) + "...",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred output format (MP3, MP4) & resolution selector before starting a download
            if (metadata == null && !isInputUrlValid) {
                FormatResolutionSelector(
                    config = conversionConfig,
                    durationSeconds = 214,
                    onConfigChange = { viewModel.updateConversionConfig(it) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // If metadata is loaded, show Video Metadata Card & Format Quality Selector with Add to Queue
            AnimatedVisibility(
                visible = metadata != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val currentMeta = metadata
                if (currentMeta != null) {
                    Column {
                        VideoMetadataCard(metadata = currentMeta)

                        Spacer(modifier = Modifier.height(16.dp))

                        FormatQualitySelector(
                            config = conversionConfig,
                            durationSeconds = currentMeta.durationSeconds,
                            onConfigChange = { viewModel.updateConversionConfig(it) },
                            onStartDownload = {
                                if (activity != null) {
                                    AdMobManager.showInterstitialAd(activity, AdMobConfig.AD_UNIT_DOWNLOAD) {
                                        viewModel.startDownload(onSuccessNavToDownloads = onNavigateToDownloads)
                                    }
                                } else {
                                    viewModel.startDownload(onSuccessNavToDownloads = onNavigateToDownloads)
                                }
                            },
                            onAddToQueue = {
                                viewModel.addToQueue(onSuccessNavToDownloads = onNavigateToDownloads)
                            }
                        )
                    }
                }
            }
        } else {
            // Batch URLs Queue Card
            BatchUrlQueueCard(
                batchText = batchText,
                onBatchTextChange = { viewModel.onBatchUrlsChanged(it) },
                onLoadSamples = { viewModel.loadBatchSamples() },
                onClear = { viewModel.clearBatchUrls() },
                onAddBatchToQueue = {
                    if (activity != null) {
                        AdMobManager.showInterstitialAd(activity, AdMobConfig.AD_UNIT_DOWNLOAD) {
                            viewModel.addBatchUrlsToQueue(onSuccessNavToDownloads = onNavigateToDownloads)
                        }
                    } else {
                        viewModel.addBatchUrlsToQueue(onSuccessNavToDownloads = onNavigateToDownloads)
                    }
                },
                isResolving = isBatchResolving,
                resolvingProgress = batchResolvingProgress,
                errorMessage = batchError,
                detectedCount = detectedBatchUrls.size,
                conversionConfig = conversionConfig
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Format quality selector for the batch items
            FormatQualitySelector(
                config = conversionConfig,
                durationSeconds = 240,
                onConfigChange = { viewModel.updateConversionConfig(it) },
                onStartDownload = {
                    viewModel.addBatchUrlsToQueue(onSuccessNavToDownloads = onNavigateToDownloads)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google AdMob Banner VIP
        AdMobBanner(
            adUnitId = AdMobConfig.AD_UNIT_BANNER_VIP,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
