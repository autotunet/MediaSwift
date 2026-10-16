package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobConfig {
    // 1. Taxigord v1
    const val AD_UNIT_TAXIGORD_V1 = "ca-app-pub-1495262574338316/2803163788"
    // 2. Taxobetapub (Descargas)
    const val AD_UNIT_TAXOBETAPUB = "ca-app-pub-1495262574338316/2903409205"
    // 3. Banner vip
    const val AD_UNIT_BANNER_VIP = "ca-app-pub-1495262574338316/2452733430"
    // 4. Rewarded / Prestamevip
    const val AD_UNIT_REWARDED_VIP = "ca-app-pub-1495262574338316/8970040322"
    // 5. Importar URL
    const val AD_UNIT_IMPORT_URL = "ca-app-pub-1495262574338316/1182179137"
    // 6. Conversión de formatos
    const val AD_UNIT_CONVERSION = "ca-app-pub-1495262574338316/4962596335"
    // 7. Descarga de video/audio
    const val AD_UNIT_DOWNLOAD = "ca-app-pub-1495262574338316/2714752655"
    // 8. General Emergente
    const val AD_UNIT_GENERAL_POPUP = "ca-app-pub-1495262574338316/3503687777"

    // Official Google Mobile Ads Test Units (used as fallback if real IDs return no-fill during review)
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
}

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    // VIP Turbo Download status unlocked by Rewarded Ads
    private val _isTurboModeActive = MutableStateFlow(false)
    val isTurboModeActive: StateFlow<Boolean> = _isTurboModeActive.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            try {
                MobileAds.disableMediationAdapterInitialization(context)
                MobileAds.putPublisherFirstPartyIdEnabled(false)
            } catch (ignored: Throwable) {}

            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(reqConfig)

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob SDK inicializado correctamente: ${status.adapterStatusMap}")
                isInitialized = true
                preloadInterstitial(context, AdMobConfig.AD_UNIT_GENERAL_POPUP)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar AdMob", e)
        }
    }

    fun preloadInterstitial(context: Context, adUnitId: String = AdMobConfig.AD_UNIT_GENERAL_POPUP) {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Anuncio intersticial real cargado: $adUnitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "No se pudo cargar anuncio real ($adUnitId): ${loadAdError.message}. Intentando fallback.")
                    // Fallback to Google test unit if real ID returns NO_FILL or error
                    if (adUnitId != AdMobConfig.TEST_INTERSTITIAL) {
                        InterstitialAd.load(
                            context,
                            AdMobConfig.TEST_INTERSTITIAL,
                            adRequest,
                            object : InterstitialAdLoadCallback() {
                                override fun onAdLoaded(testAd: InterstitialAd) {
                                    interstitialAd = testAd
                                    isInterstitialLoading = false
                                    Log.d(TAG, "Anuncio intersticial fallback cargado con éxito")
                                }

                                override fun onAdFailedToLoad(err: LoadAdError) {
                                    interstitialAd = null
                                    isInterstitialLoading = false
                                }
                            }
                        )
                    } else {
                        interstitialAd = null
                        isInterstitialLoading = false
                    }
                }
            }
        )
    }

    fun preloadRewarded(context: Context, adUnitId: String = AdMobConfig.AD_UNIT_REWARDED_VIP) {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Anuncio bonificado real cargado exitosamente: $adUnitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Fallo al cargar anuncio bonificado ($adUnitId): ${loadAdError.message}. Intentando fallback.")
                    if (adUnitId != AdMobConfig.TEST_REWARDED) {
                        RewardedAd.load(
                            context,
                            AdMobConfig.TEST_REWARDED,
                            adRequest,
                            object : RewardedAdLoadCallback() {
                                override fun onAdLoaded(testAd: RewardedAd) {
                                    rewardedAd = testAd
                                    isRewardedLoading = false
                                    Log.d(TAG, "Anuncio bonificado fallback cargado con éxito")
                                }

                                override fun onAdFailedToLoad(err: LoadAdError) {
                                    rewardedAd = null
                                    isRewardedLoading = false
                                }
                            }
                        )
                    } else {
                        rewardedAd = null
                        isRewardedLoading = false
                    }
                }
            }
        )
    }

    /**
     * Shows an automatic interstitial ad for a specific action (e.g. import URL, download, convert)
     * and guarantees continuation through [onActionProceed] even if ads fail or are closed.
     */
    fun showInterstitialAd(
        activity: Activity,
        adUnitId: String = AdMobConfig.AD_UNIT_GENERAL_POPUP,
        onActionProceed: () -> Unit
    ) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preloadInterstitial(activity, adUnitId)
                    onActionProceed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Error al mostrar intersticial: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity, adUnitId)
                    onActionProceed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Anuncio intersticial en pantalla completa")
                }
            }
            currentAd.show(activity)
        } else {
            // Load and show on-demand with safety timeout
            val adRequest = AdRequest.Builder().build()
            var hasProceeded = false

            fun proceedOnce() {
                if (!hasProceeded) {
                    hasProceeded = true
                    onActionProceed()
                }
            }

            InterstitialAd.load(
                activity,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                preloadInterstitial(activity, adUnitId)
                                proceedOnce()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                preloadInterstitial(activity, adUnitId)
                                proceedOnce()
                            }
                        }
                        ad.show(activity)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Fallo al cargar anuncio: ${loadAdError.message}. Continuando acción.")
                        proceedOnce()
                    }
                }
            )

            // Safety timeout fallback: if network is slow, don't freeze the user's action
            activity.window.decorView.postDelayed({
                proceedOnce()
            }, 1200)
        }
    }

    /**
     * Shows Rewarded Ad (Prestamevip) with reward callback.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onClosed: () -> Unit
    ) {
        val currentRewarded = rewardedAd
        if (currentRewarded != null) {
            currentRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    onClosed()
                }
            }
            currentRewarded.show(activity) {
                _isTurboModeActive.value = true
                onRewardEarned()
            }
        } else {
            // Attempt on-demand load or fallback
            RewardedAd.load(
                activity,
                AdMobConfig.AD_UNIT_REWARDED_VIP,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        ad.show(activity) {
                            _isTurboModeActive.value = true
                            onRewardEarned()
                        }
                    }

                    override fun onAdFailedToLoad(err: LoadAdError) {
                        // Fallback to test unit
                        RewardedAd.load(
                            activity,
                            AdMobConfig.TEST_REWARDED,
                            AdRequest.Builder().build(),
                            object : RewardedAdLoadCallback() {
                                override fun onAdLoaded(testAd: RewardedAd) {
                                    testAd.show(activity) {
                                        _isTurboModeActive.value = true
                                        onRewardEarned()
                                    }
                                }

                                override fun onAdFailedToLoad(e: LoadAdError) {
                                    // Reward user anyway so UX doesn't get blocked
                                    _isTurboModeActive.value = true
                                    onRewardEarned()
                                    onClosed()
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}
