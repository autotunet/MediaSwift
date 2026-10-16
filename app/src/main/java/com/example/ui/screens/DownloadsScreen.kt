package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.platform.LocalContext
import com.example.data.ads.AdMobConfig
import com.example.data.model.DownloadStatus
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveDownloadCard
import com.example.ui.components.AdMobBanner
import com.example.ui.components.BatchQueueDialog
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val isQueuePaused by viewModel.isQueuePaused.collectAsState()
    val showBatchDialog by viewModel.showBatchDialog.collectAsState()
    val batchText by viewModel.batchUrlsInput.collectAsState()
    val isBatchResolving by viewModel.isBatchResolving.collectAsState()
    val batchResolvingProgress by viewModel.batchResolvingProgress.collectAsState()
    val batchError by viewModel.batchErrorMessage.collectAsState()
    val conversionConfig by viewModel.conversionConfig.collectAsState()
    val isTurboModeActive by viewModel.isTurboModeActive.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val detectedBatchUrls = viewModel.parseBatchUrls(batchText)

    val activeDownloadingItem = activeDownloads.firstOrNull {
        it.status == DownloadStatus.DOWNLOADING ||
        it.status == DownloadStatus.CONNECTING ||
        it.status == DownloadStatus.CONVERTING
    }
    val queuedList = activeDownloads.filter { it.status == DownloadStatus.QUEUED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("downloads_screen")
    ) {
        // Top header & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings.queueTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.queueSubtitle(activeDownloads.size),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add URLs Button
                FilledTonalButton(
                    onClick = { viewModel.setShowBatchDialog(true) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_urls_to_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.addMoreUrlsBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (activeDownloads.any { it.status == DownloadStatus.FAILED }) {
                    OutlinedButton(
                        onClick = { viewModel.clearFailedDownloads() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = strings.queueClearErrors, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sequential Queue Control Banner
        if (activeDownloads.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sequential_queue_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isQueuePaused) WarningAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isQueuePaused) Icons.Default.Pause else Icons.Default.Queue,
                                    contentDescription = null,
                                    tint = if (isQueuePaused) WarningAmber else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = strings.sequentialProcessingBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isQueuePaused) strings.sequentialStatusPaused
                                       else strings.sequentialStatusActive(
                                           if (activeDownloadingItem != null) 1 else 0,
                                           activeDownloads.size
                                       ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Queue Pause / Resume Toggle
                    OutlinedButton(
                        onClick = {
                            if (isQueuePaused) viewModel.resumeQueue() else viewModel.pauseQueue()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_queue_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isQueuePaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isQueuePaused) SuccessGreen else WarningAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isQueuePaused) strings.sequentialResumeQueueBtn else strings.sequentialPauseQueueBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // AdMob Banner VIP
        AdMobBanner(
            adUnitId = AdMobConfig.AD_UNIT_BANNER_VIP,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // VIP Turbo Mode Rewarded Ad Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("downloads_turbo_mode_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTurboModeActive) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, if (isTurboModeActive) SuccessGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTurboModeActive) Icons.Default.Bolt else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.turboModeTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isTurboModeActive) SuccessGreen else MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = if (isTurboModeActive) strings.turboModeActiveBadge else "VIP",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isTurboModeActive) "Velocidad máxima y prioridad activadas" else strings.turboModeDesc,
                            fontSize = 10.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isTurboModeActive && activity != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.showRewardedAd(activity) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("downloads_unlock_turbo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.turboModeUnlockBtn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeDownloads.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Downloading,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = strings.queueEmptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = strings.queueEmptyDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.setShowBatchDialog(true) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("empty_add_batch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.batchInputTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToHome,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.queueExploreBtn, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Sequential Active Queue Items with Individual Progress Bars
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                var queuedIndexCounter = 1
                itemsIndexed(activeDownloads, key = { _, item -> item.id }) { _, item ->
                    val queueIndex = if (item.status == DownloadStatus.QUEUED) queuedIndexCounter++ else null
                    ActiveDownloadCard(
                        download = item,
                        queueIndex = queueIndex,
                        isTurboActive = isTurboModeActive,
                        onPause = { viewModel.pauseDownload(item.id) },
                        onResume = { viewModel.resumeDownload(item.id) },
                        onCancel = { viewModel.cancelDownload(item.id) }
                    )
                }
            }
        }
    }

    // Batch Queue Dialog accessible directly from Queue screen
    BatchQueueDialog(
        isOpen = showBatchDialog,
        onDismiss = { viewModel.setShowBatchDialog(false) },
        batchText = batchText,
        onBatchTextChange = { viewModel.onBatchUrlsChanged(it) },
        onLoadSamples = { viewModel.loadBatchSamples() },
        onClear = { viewModel.clearBatchUrls() },
        onAddBatchToQueue = { viewModel.addBatchUrlsToQueue(onSuccessNavToDownloads = {}) },
        isResolving = isBatchResolving,
        resolvingProgress = batchResolvingProgress,
        errorMessage = batchError,
        detectedCount = detectedBatchUrls.size,
        conversionConfig = conversionConfig,
        onConfigChange = { viewModel.updateConversionConfig(it) }
    )
}
