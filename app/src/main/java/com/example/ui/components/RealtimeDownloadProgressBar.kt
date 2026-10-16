package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

/**
 * Modern, high-visibility progress bar component displaying real-time
 * download speed (KB/s or MB/s), animated progress, status, and byte counts.
 */
@Composable
fun RealtimeDownloadProgressBar(
    progressPercent: Int,
    speedBytesPerSec: Long,
    formattedSpeed: String,
    formattedDownloadedSize: String,
    status: DownloadStatus,
    modifier: Modifier = Modifier,
    barHeight: Dp = 9.dp,
    isTurboActive: Boolean = false,
    testTagPrefix: String = "realtime"
) {
    // Smooth animated progress between 0.0f and 1.0f
    val targetProgress = (progressPercent / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "realtime_progress_animation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_progress_container")
    ) {
        // Top Header Row: Progress percentage & Live Speed Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status & Percentage
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (status) {
                        DownloadStatus.CONVERTING -> "$progressPercent% • Convirtiendo..."
                        DownloadStatus.QUEUED -> "0% • En cola"
                        DownloadStatus.PAUSED -> "$progressPercent% • Pausado"
                        DownloadStatus.FAILED -> "$progressPercent% • Error"
                        DownloadStatus.CONNECTING -> "Conectando..."
                        else -> "$progressPercent%"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        DownloadStatus.CONVERTING -> MaterialTheme.colorScheme.secondary
                        DownloadStatus.PAUSED -> WarningAmber
                        DownloadStatus.FAILED -> ErrorRed
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.testTag("${testTagPrefix}_progress_percentage_text")
                )

                if (isTurboActive && status == DownloadStatus.DOWNLOADING) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "TURBO 2X",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            // Right: Real-time download speed badge
            when (status) {
                DownloadStatus.DOWNLOADING -> {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("realtime_speed_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Velocidad de descarga",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (speedBytesPerSec > 0L) formattedSpeed else "Calculando...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("download_speed_text")
                            )
                        }
                    }
                }
                DownloadStatus.CONVERTING -> {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Procesando",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                DownloadStatus.PAUSED -> {
                    Surface(
                        color = WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "0 KB/s",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier.testTag("download_speed_text")
                            )
                        }
                    }
                }
                DownloadStatus.QUEUED -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "En espera",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // LinearProgressIndicator bar with rounded edges and state-aware colors
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(CircleShape)
                .testTag("linear_progress_indicator"),
            color = when (status) {
                DownloadStatus.CONVERTING -> MaterialTheme.colorScheme.secondary
                DownloadStatus.PAUSED -> WarningAmber
                DownloadStatus.FAILED -> ErrorRed
                DownloadStatus.QUEUED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Detail Row: Downloaded bytes / total bytes & Speed note
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDownloadedSize,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("${testTagPrefix}_size_text")
            )

            if (status == DownloadStatus.DOWNLOADING && speedBytesPerSec > 0L) {
                Text(
                    text = "Velocidad en tiempo real: $formattedSpeed",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("${testTagPrefix}_speed_subtext")
                )
            } else if (status == DownloadStatus.QUEUED) {
                Text(
                    text = "Procesamiento secuencial",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
