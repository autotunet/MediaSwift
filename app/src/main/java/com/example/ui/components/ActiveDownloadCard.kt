package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.CrimsonPrimary
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun ActiveDownloadCard(
    download: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    queueIndex: Int? = null,
    isTurboActive: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${download.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = download.thumbnailUrl,
                        contentDescription = "Miniatura",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    // Format pill on bottom
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = download.targetFormat.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Author and Badges
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = download.author,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quality pill & status pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val specText = download.qualityLabel ?: download.resolutionLabel ?: download.targetFormat.uppercase()
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = specText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        StatusBadge(status = download.status, queueIndex = queueIndex)
                    }
                }

                // Action Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING -> {
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pause_download_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pausar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("resume_download_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reanudar",
                                    tint = SuccessGreen
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("retry_download_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reintentar",
                                    tint = WarningAmber
                                )
                            }
                        }
                        DownloadStatus.CONVERTING -> {
                            val infiniteTransition = rememberInfiniteTransition(label = "converting_spin")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "spin"
                            )
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Convirtiendo",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(rotation)
                            )
                        }
                        else -> Unit
                    }

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("cancel_download_${download.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced Realtime Download Progress Bar with real-time speed in KB/s or MB/s
            RealtimeDownloadProgressBar(
                progressPercent = download.progressPercent,
                speedBytesPerSec = download.speedBytesPerSec,
                formattedSpeed = download.formattedSpeed,
                formattedDownloadedSize = download.formattedDownloadedSize,
                status = download.status,
                isTurboActive = isTurboActive,
                barHeight = 9.dp,
                testTagPrefix = "card_${download.id}"
            )
        }
    }
}

@Composable
fun StatusBadge(status: DownloadStatus, queueIndex: Int? = null) {
    val (label, bg, fg) = when (status) {
        DownloadStatus.QUEUED -> Triple(
            if (queueIndex != null) "#$queueIndex En cola" else "En cola",
            Color(0xFF6B7280).copy(alpha = 0.2f),
            Color(0xFF9CA3AF)
        )
        DownloadStatus.CONNECTING -> Triple("Conectando...", Color(0xFF3B82F6).copy(alpha = 0.2f), Color(0xFF60A5FA))
        DownloadStatus.DOWNLOADING -> Triple("Descargando", CrimsonPrimary.copy(alpha = 0.2f), CrimsonPrimary)
        DownloadStatus.CONVERTING -> Triple("Convirtiendo", Color(0xFFA855F7).copy(alpha = 0.2f), Color(0xFFA855F7))
        DownloadStatus.PAUSED -> Triple("Pausado", WarningAmber.copy(alpha = 0.2f), WarningAmber)
        DownloadStatus.FAILED -> Triple("Error", ErrorRed.copy(alpha = 0.2f), ErrorRed)
        DownloadStatus.COMPLETED -> Triple("Completado", SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
