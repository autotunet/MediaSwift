package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.model.AudioFormat
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionConfig
import com.example.data.model.MediaType
import com.example.data.model.VideoFormat
import com.example.data.model.VideoResolution

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionModalSheet(
    item: DownloadEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmConvert: (
        targetMediaType: MediaType,
        targetVideoFormat: VideoFormat?,
        targetVideoResolution: VideoResolution?,
        targetAudioFormat: AudioFormat?,
        targetAudioQuality: AudioQuality?
    ) -> Unit
) {
    var config by remember {
        mutableStateOf(
            ConversionConfig(
                mediaType = if (item.mediaType == MediaType.VIDEO) MediaType.AUDIO else MediaType.VIDEO,
                extractAudioOnly = item.mediaType == MediaType.VIDEO,
                audioFormat = AudioFormat.MP3,
                audioQuality = AudioQuality.Q_320,
                videoFormat = VideoFormat.MP4,
                videoResolution = VideoResolution.R_720P
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("conversion_modal_sheet")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Convertir Archivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Archivo origen: ${item.title} (${item.targetFormat.uppercase()})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormatQualitySelector(
                config = config,
                durationSeconds = item.durationSeconds,
                onConfigChange = { config = it },
                onStartDownload = {
                    onConfirmConvert(
                        config.mediaType,
                        if (config.mediaType == MediaType.VIDEO) config.videoFormat else null,
                        if (config.mediaType == MediaType.VIDEO) config.videoResolution else null,
                        if (config.mediaType == MediaType.AUDIO) config.audioFormat else null,
                        if (config.mediaType == MediaType.AUDIO) config.audioQuality else null
                    )
                    onDismiss()
                }
            )
        }
    }
}
