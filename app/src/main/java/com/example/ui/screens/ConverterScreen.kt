package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalContext
import com.example.data.ads.AdMobConfig
import com.example.data.ads.AdMobManager
import com.example.ui.components.AdMobBanner
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoResolution
import com.example.ui.MainViewModel
import com.example.ui.components.FormatQualitySelector

@Composable
fun ConverterScreen(
    viewModel: MainViewModel,
    onNavigateToDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedItems by viewModel.filteredLibraryItems.collectAsState()
    var selectedItem by remember(completedItems) {
        mutableStateOf(completedItems.firstOrNull())
    }

    var config by remember {
        mutableStateOf(
            ConversionConfig(
                mediaType = MediaType.AUDIO,
                extractAudioOnly = true,
                audioFormat = AudioFormat.MP3,
                audioQuality = AudioQuality.Q_320,
                videoFormat = VideoFormat.MP4,
                videoResolution = VideoResolution.R_720P
            )
        )
    }

    val activity = LocalContext.current as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("converter_screen")
    ) {
        // Header
        Text(
            text = "Estudio de Conversión",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Convierte videos a MP3 o transcodifica formatos libremente",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AdMob Banner VIP
        AdMobBanner(
            adUnitId = AdMobConfig.AD_UNIT_BANNER_VIP,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (completedItems.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Primero descarga o añade un video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Una vez descargado, podrás convertirlo a MP3 (320kbps, 256kbps), extraer pistas de audio o cambiar entre MP4, MKV, WebM y AVI.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            // Horizontal list to choose source file
            Text(
                text = "1. Selecciona el archivo de origen:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                completedItems.forEach { item ->
                    val isSelected = selectedItem?.id == item.id
                    SourceItemChip(
                        item = item,
                        isSelected = isSelected,
                        onClick = { selectedItem = item }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val currentItem = selectedItem ?: completedItems.first()
            Text(
                text = "2. Configura el formato y calidad de salida:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormatQualitySelector(
                config = config,
                durationSeconds = currentItem.durationSeconds,
                onConfigChange = { config = it },
                onStartDownload = {
                    if (activity != null) {
                        AdMobManager.showInterstitialAd(activity, AdMobConfig.AD_UNIT_CONVERSION) {
                            viewModel.convertMedia(
                                sourceEntity = currentItem,
                                targetMediaType = config.mediaType,
                                targetVideoFormat = if (config.mediaType == MediaType.VIDEO) config.videoFormat else null,
                                targetVideoResolution = if (config.mediaType == MediaType.VIDEO) config.videoResolution else null,
                                targetAudioFormat = if (config.mediaType == MediaType.AUDIO) config.audioFormat else null,
                                targetAudioQuality = if (config.mediaType == MediaType.AUDIO) config.audioQuality else null,
                                onCreated = {
                                    onNavigateToDownloads()
                                }
                            )
                        }
                    } else {
                        viewModel.convertMedia(
                            sourceEntity = currentItem,
                            targetMediaType = config.mediaType,
                            targetVideoFormat = if (config.mediaType == MediaType.VIDEO) config.videoFormat else null,
                            targetVideoResolution = if (config.mediaType == MediaType.VIDEO) config.videoResolution else null,
                            targetAudioFormat = if (config.mediaType == MediaType.AUDIO) config.audioFormat else null,
                            targetAudioQuality = if (config.mediaType == MediaType.AUDIO) config.audioQuality else null,
                            onCreated = {
                                onNavigateToDownloads()
                            }
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SourceItemChip(
    item: DownloadEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isAudio = item.mediaType == MediaType.AUDIO
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("source_chip_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isAudio) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Default.MusicNote else Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.targetFormat.uppercase()} • ${item.durationFormatted}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
