package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.MediaType
import com.example.data.model.VideoResolution
import com.example.ui.i18n.LocalAppStrings

/**
 * ExposedDropdownMenuBox component allowing users to dynamically select
 * video resolution (e.g. 1080p, 720p, 480p, 360p, 240p) or audio quality (e.g. 320kbps, 256kbps, 128kbps)
 * based on the active format/media type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityResolutionDropdownSelector(
    config: ConversionConfig,
    durationSeconds: Long,
    onConfigChange: (ConversionConfig) -> Unit,
    showModeToggle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isAudio = config.extractAudioOnly || config.mediaType == MediaType.AUDIO
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showModeToggle) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("mode_segmented_button_row")
            ) {
                SegmentedButton(
                    selected = !isAudio,
                    onClick = {
                        onConfigChange(config.copy(mediaType = MediaType.VIDEO, extractAudioOnly = false))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = !isAudio) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = strings.modeVideo,
                            fontWeight = if (!isAudio) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("segment_video")
                )

                SegmentedButton(
                    selected = isAudio,
                    onClick = {
                        onConfigChange(config.copy(mediaType = MediaType.AUDIO, extractAudioOnly = true))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = isAudio) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = strings.modeAudioOnly,
                            fontWeight = if (isAudio) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("segment_audio_only")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAudio) Icons.Default.MusicNote else Icons.Default.Hd,
                    contentDescription = null,
                    tint = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAudio) strings.audioQualityLabel else strings.videoResolutionLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                color = if (isAudio) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isAudio) config.audioQuality.badge else config.videoResolution.bitrateLabel.substringBefore(" •"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAudio) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quality_resolution_dropdown_box")
        ) {
            val displayValue = if (isAudio) {
                "${config.audioQuality.label} (${config.audioQuality.badge})"
            } else {
                "${config.videoResolution.label} — ${config.videoResolution.bitrateLabel}"
            }

            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.HighQuality else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .testTag("dropdown_selected_value_field")
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag("exposed_dropdown_menu_items")
            ) {
                if (isAudio) {
                    // Audio Qualities: 320kbps, 256kbps, 192kbps, 128kbps, 64kbps
                    AudioQuality.values().forEach { quality ->
                        val isSelected = config.audioQuality == quality
                        val estimatedSize = "%.1f MB".format(
                            ((quality.bitrateKbps * 1024L / 8L) * durationSeconds) / (1024f * 1024f)
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = quality.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${quality.bitrateKbps} kbps • ${quality.badge}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = estimatedSize,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(20.dp))
                                }
                            },
                            onClick = {
                                onConfigChange(config.copy(audioQuality = quality))
                                isExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_audio_${quality.name}")
                        )
                    }
                } else {
                    // Video Resolutions: 1080p, 720p, 480p, 360p, 240p
                    VideoResolution.values().forEach { resolution ->
                        val isSelected = config.videoResolution == resolution
                        val estimatedSize = "%.1f MB".format(
                            (300_000L * resolution.sizeFactor * durationSeconds) / (1024f * 1024f)
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = resolution.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = resolution.bitrateLabel,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = estimatedSize,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(20.dp))
                                }
                            },
                            onClick = {
                                onConfigChange(config.copy(videoResolution = resolution))
                                isExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_video_${resolution.name}")
                        )
                    }
                }
            }
        }
    }
}
