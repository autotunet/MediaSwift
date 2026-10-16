package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoResolution
import com.example.ui.i18n.LocalAppStrings

enum class SelectorPresentationStyle {
    RADIO_BUTTONS,
    DROPDOWN_MENU
}

/**
 * Reusable component providing both a Radio Button Group and a Dropdown Menu
 * that allows users to select their preferred output format (MP3, MP4)
 * and resolution (e.g., 720p, 1080p) before starting a download.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatResolutionSelector(
    config: ConversionConfig,
    durationSeconds: Long,
    onConfigChange: (ConversionConfig) -> Unit,
    modifier: Modifier = Modifier,
    initialStyle: SelectorPresentationStyle = SelectorPresentationStyle.DROPDOWN_MENU,
    showContainerCard: Boolean = true,
    title: String? = null
) {
    val strings = LocalAppStrings.current
    var presentationStyle by remember { mutableStateOf(initialStyle) }
    val isAudio = config.extractAudioOnly || config.mediaType == MediaType.AUDIO

    val content = @Composable {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header title & Estimated Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title ?: strings.preferredOutputTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.preferredOutputSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAudio) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("preferred_output_size_badge")
                ) {
                    Text(
                        text = config.formatEstimatedSize(durationSeconds),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAudio) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector presentation style toggle: Radio Buttons vs Dropdown Menu
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selector_style_segmented_row")
            ) {
                SegmentedButton(
                    selected = presentationStyle == SelectorPresentationStyle.RADIO_BUTTONS,
                    onClick = { presentationStyle = SelectorPresentationStyle.RADIO_BUTTONS },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = presentationStyle == SelectorPresentationStyle.RADIO_BUTTONS) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonChecked,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = strings.selectorStyleRadio,
                            fontSize = 12.sp,
                            fontWeight = if (presentationStyle == SelectorPresentationStyle.RADIO_BUTTONS) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("style_radio_button")
                )

                SegmentedButton(
                    selected = presentationStyle == SelectorPresentationStyle.DROPDOWN_MENU,
                    onClick = { presentationStyle = SelectorPresentationStyle.DROPDOWN_MENU },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = presentationStyle == SelectorPresentationStyle.DROPDOWN_MENU) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = strings.selectorStyleDropdown,
                            fontSize = 12.sp,
                            fontWeight = if (presentationStyle == SelectorPresentationStyle.DROPDOWN_MENU) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("style_dropdown_menu")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = presentationStyle,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SelectorViewAnimation"
            ) { targetStyle ->
                when (targetStyle) {
                    SelectorPresentationStyle.RADIO_BUTTONS -> {
                        RadioButtonGroupView(
                            config = config,
                            durationSeconds = durationSeconds,
                            onConfigChange = onConfigChange
                        )
                    }
                    SelectorPresentationStyle.DROPDOWN_MENU -> {
                        DropdownMenuView(
                            config = config,
                            durationSeconds = durationSeconds,
                            onConfigChange = onConfigChange
                        )
                    }
                }
            }
        }
    }

    if (showContainerCard) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("format_resolution_selector_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Radio button group mode:
 * 1. Radio buttons to select format: MP4 Video vs MP3 Audio
 * 2. Radio buttons to select resolution (1080p, 720p, 480p, 360p) or bitrate (320k, 256k, 128k)
 */
@Composable
private fun RadioButtonGroupView(
    config: ConversionConfig,
    durationSeconds: Long,
    onConfigChange: (ConversionConfig) -> Unit
) {
    val strings = LocalAppStrings.current
    val isAudio = config.extractAudioOnly || config.mediaType == MediaType.AUDIO

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Output Format Radio Button Group (MP4 vs MP3)
        Text(
            text = strings.formatSelectionLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // MP4 Video Option
        FormatRadioOptionCard(
            title = strings.formatMp4Title,
            description = strings.formatMp4Desc,
            icon = Icons.Default.Videocam,
            isSelected = !isAudio,
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = {
                onConfigChange(
                    config.copy(
                        mediaType = MediaType.VIDEO,
                        videoFormat = VideoFormat.MP4,
                        extractAudioOnly = false
                    )
                )
            },
            tag = "radio_option_mp4"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // MP3 Audio Option
        FormatRadioOptionCard(
            title = strings.formatMp3Title,
            description = strings.formatMp3Desc,
            icon = Icons.Default.MusicNote,
            isSelected = isAudio,
            selectedColor = MaterialTheme.colorScheme.secondary,
            onClick = {
                onConfigChange(
                    config.copy(
                        mediaType = MediaType.AUDIO,
                        audioFormat = AudioFormat.MP3,
                        extractAudioOnly = true
                    )
                )
            },
            tag = "radio_option_mp3"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Resolution or Audio Quality Radio Button Group
        Text(
            text = if (isAudio) strings.qualitySelectionLabel else strings.resolutionSelectionLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!isAudio) {
            // Video Resolutions: 1080p, 720p, 480p, 360p
            val resolutions = listOf(
                VideoResolution.R_1080P,
                VideoResolution.R_720P,
                VideoResolution.R_480P,
                VideoResolution.R_360P
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                resolutions.forEach { res ->
                    val isSelected = config.videoResolution == res
                    val estSize = "%.1f MB".format(
                        (300_000L * res.sizeFactor * durationSeconds) / (1024f * 1024f)
                    )

                    ResolutionRadioOptionItem(
                        title = res.label,
                        subtitle = res.bitrateLabel,
                        badge = if (res == VideoResolution.R_720P) "Recomendado" else if (res == VideoResolution.R_1080P) "Full HD" else null,
                        estimatedSize = estSize,
                        isSelected = isSelected,
                        tintColor = MaterialTheme.colorScheme.primary,
                        onClick = { onConfigChange(config.copy(videoResolution = res)) },
                        tag = "radio_res_${res.name}"
                    )
                }
            }
        } else {
            // Audio Qualities: 320 kbps, 256 kbps, 192 kbps, 128 kbps
            val qualities = listOf(
                AudioQuality.Q_320,
                AudioQuality.Q_256,
                AudioQuality.Q_192,
                AudioQuality.Q_128
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                qualities.forEach { q ->
                    val isSelected = config.audioQuality == q
                    val estSize = "%.1f MB".format(
                        ((q.bitrateKbps * 1024L / 8L) * durationSeconds) / (1024f * 1024f)
                    )

                    ResolutionRadioOptionItem(
                        title = "${q.label} • ${q.badge}",
                        subtitle = "${q.bitrateKbps} kbps MP3 Audio Stream",
                        badge = if (q == AudioQuality.Q_320) "Estudio HQ" else null,
                        estimatedSize = estSize,
                        isSelected = isSelected,
                        tintColor = MaterialTheme.colorScheme.secondary,
                        onClick = { onConfigChange(config.copy(audioQuality = q)) },
                        tag = "radio_quality_${q.name}"
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatRadioOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) BorderStroke(1.5.dp, selectedColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = selectedColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResolutionRadioOptionItem(
    title: String,
    subtitle: String,
    badge: String?,
    estimatedSize: String,
    isSelected: Boolean,
    tintColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) tintColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = if (isSelected) BorderStroke(1.5.dp, tintColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = tintColor,
                        unselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (badge != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tintColor.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tintColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = estimatedSize,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tintColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Dropdown Menu mode:
 * 1. ExposedDropdownMenuBox for Format (MP4, MP3, WEBM, MKV, M4A, etc.)
 * 2. ExposedDropdownMenuBox for Resolution (1080p, 720p, 480p, 360p) or Bitrate
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuView(
    config: ConversionConfig,
    durationSeconds: Long,
    onConfigChange: (ConversionConfig) -> Unit
) {
    val strings = LocalAppStrings.current
    val isAudio = config.extractAudioOnly || config.mediaType == MediaType.AUDIO

    var isFormatDropdownExpanded by remember { mutableStateOf(false) }
    var isResolutionDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Output Format Dropdown Menu
        Text(
            text = strings.formatSelectionLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = isFormatDropdownExpanded,
            onExpandedChange = { isFormatDropdownExpanded = !isFormatDropdownExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("output_format_dropdown_box")
        ) {
            val formatDisplayText = if (isAudio) {
                "${config.audioFormat.name} • ${strings.modeAudioOnly}"
            } else {
                "${config.videoFormat.name} • ${strings.modeVideo}"
            }

            OutlinedTextField(
                value = formatDisplayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFormatDropdownExpanded)
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.MusicNote else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .testTag("format_dropdown_textfield")
            )

            ExposedDropdownMenu(
                expanded = isFormatDropdownExpanded,
                onDismissRequest = { isFormatDropdownExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag("format_dropdown_menu")
            ) {
                // Primary Option: MP4 Video
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = strings.formatMp4Title,
                                fontWeight = if (!isAudio && config.videoFormat == VideoFormat.MP4) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = strings.formatMp4Desc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (!isAudio && config.videoFormat == VideoFormat.MP4) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        onConfigChange(
                            config.copy(
                                mediaType = MediaType.VIDEO,
                                videoFormat = VideoFormat.MP4,
                                extractAudioOnly = false
                            )
                        )
                        isFormatDropdownExpanded = false
                    },
                    modifier = Modifier.testTag("dropdown_format_mp4")
                )

                // Primary Option: MP3 Audio
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = strings.formatMp3Title,
                                fontWeight = if (isAudio && config.audioFormat == AudioFormat.MP3) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = strings.formatMp3Desc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingIcon = {
                        if (isAudio && config.audioFormat == AudioFormat.MP3) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    },
                    onClick = {
                        onConfigChange(
                            config.copy(
                                mediaType = MediaType.AUDIO,
                                audioFormat = AudioFormat.MP3,
                                extractAudioOnly = true
                            )
                        )
                        isFormatDropdownExpanded = false
                    },
                    modifier = Modifier.testTag("dropdown_format_mp3")
                )

                // Other Video Formats (WEBM, MKV)
                VideoFormat.values().filter { it != VideoFormat.MP4 }.forEach { vf ->
                    DropdownMenuItem(
                        text = {
                            Text(text = "${vf.label} (${vf.extension.uppercase()}) • ${vf.description}")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    mediaType = MediaType.VIDEO,
                                    videoFormat = vf,
                                    extractAudioOnly = false
                                )
                            )
                            isFormatDropdownExpanded = false
                        }
                    )
                }

                // Other Audio Formats (M4A, AAC, WAV)
                AudioFormat.values().filter { it != AudioFormat.MP3 }.forEach { af ->
                    DropdownMenuItem(
                        text = {
                            Text(text = "${af.label} (${af.extension.uppercase()}) • ${af.description}")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    mediaType = MediaType.AUDIO,
                                    audioFormat = af,
                                    extractAudioOnly = true
                                )
                            )
                            isFormatDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Resolution or Quality Dropdown Menu
        Text(
            text = if (isAudio) strings.qualitySelectionLabel else strings.resolutionSelectionLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = isResolutionDropdownExpanded,
            onExpandedChange = { isResolutionDropdownExpanded = !isResolutionDropdownExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("resolution_dropdown_box")
        ) {
            val resDisplayText = if (isAudio) {
                "${config.audioQuality.label} (${config.audioQuality.badge}) • ${config.audioQuality.bitrateKbps} kbps"
            } else {
                "${config.videoResolution.label} — ${config.videoResolution.bitrateLabel}"
            }

            OutlinedTextField(
                value = resDisplayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isResolutionDropdownExpanded)
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.HighQuality else Icons.Default.Hd,
                        contentDescription = null,
                        tint = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .testTag("resolution_dropdown_textfield")
            )

            ExposedDropdownMenu(
                expanded = isResolutionDropdownExpanded,
                onDismissRequest = { isResolutionDropdownExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag("resolution_dropdown_menu")
            ) {
                if (!isAudio) {
                    VideoResolution.values().forEach { res ->
                        val isSelected = config.videoResolution == res
                        val estSize = "%.1f MB".format(
                            (300_000L * res.sizeFactor * durationSeconds) / (1024f * 1024f)
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
                                            text = res.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = res.bitrateLabel,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = estSize,
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
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onConfigChange(config.copy(videoResolution = res))
                                isResolutionDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${res.name}")
                        )
                    }
                } else {
                    AudioQuality.values().forEach { q ->
                        val isSelected = config.audioQuality == q
                        val estSize = "%.1f MB".format(
                            ((q.bitrateKbps * 1024L / 8L) * durationSeconds) / (1024f * 1024f)
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
                                            text = "${q.label} • ${q.badge}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${q.bitrateKbps} kbps MP3 Stream",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = estSize,
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
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onConfigChange(config.copy(audioQuality = q))
                                isResolutionDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_audio_${q.name}")
                        )
                    }
                }
            }
        }
    }
}
