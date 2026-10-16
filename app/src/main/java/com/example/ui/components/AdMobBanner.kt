package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ads.AdMobConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Google AdMob Banner component.
 *
 * Supports both standalone card mode and automatic bottom-docked mode ([isBottomDocked] = true)
 * that integrates cleanly above the bottom navigation bar.
 */
@Composable
fun AdMobBanner(
    adUnitId: String = AdMobConfig.AD_UNIT_BANNER_VIP,
    modifier: Modifier = Modifier,
    isBottomDocked: Boolean = false
) {
    var isAdLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var currentAdView by remember { mutableStateOf<AdView?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, currentAdView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> currentAdView?.resume()
                Lifecycle.Event.ON_PAUSE -> currentAdView?.pause()
                Lifecycle.Event.ON_DESTROY -> currentAdView?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentAdView?.destroy()
        }
    }

    if (isBottomDocked) {
        // Automatic bottom banner docked above navigation bar
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("automatic_bottom_admob_banner"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subtle top hairline separator
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // Compact Ad tag row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = "ANUNCIO • GOOGLE ADMOB",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                // AdView Container (standard 320x50 banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            var triedFallback = false
                            AdView(ctx).apply {
                                try {
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                } catch (ignored: Throwable) {}
                                setAdSize(AdSize.BANNER)
                                setAdUnitId(adUnitId)
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        isAdLoaded = true
                                        hasError = false
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        if (!triedFallback && adUnitId != AdMobConfig.TEST_BANNER) {
                                            triedFallback = true
                                            setAdUnitId(AdMobConfig.TEST_BANNER)
                                            loadAd(AdRequest.Builder().build())
                                        } else {
                                            isAdLoaded = false
                                            hasError = true
                                        }
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                                currentAdView = this
                            }
                        },
                        modifier = Modifier.testTag("admob_adview")
                    )

                    if (!isAdLoaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasError) "Banner AdMob activo • $adUnitId" else "Google AdMob cargando...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Standalone card banner for inside screens
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("admob_banner_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ad indicator tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ANUNCIO • ADMOB",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // AdView Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            var triedFallback = false
                            AdView(ctx).apply {
                                try {
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                } catch (ignored: Throwable) {}
                                setAdSize(AdSize.BANNER)
                                setAdUnitId(adUnitId)
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        isAdLoaded = true
                                        hasError = false
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        if (!triedFallback && adUnitId != AdMobConfig.TEST_BANNER) {
                                            triedFallback = true
                                            setAdUnitId(AdMobConfig.TEST_BANNER)
                                            loadAd(AdRequest.Builder().build())
                                        } else {
                                            isAdLoaded = false
                                            hasError = true
                                        }
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                                currentAdView = this
                            }
                        },
                        modifier = Modifier.testTag("admob_adview")
                    )

                    if (!isAdLoaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasError) "Banner AdMob activo • $adUnitId" else "Cargando anuncio Google AdMob...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
