package com.google.samples.quickstart.admobexample.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.samples.quickstart.admobexample.R
import com.google.samples.quickstart.admobexample.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var interstitialAd: InterstitialAd? = null
    private lateinit var adView: AdView
    private lateinit var loadInterstitialButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adView = binding.adView
        loadInterstitialButton = binding.loadInterstitialButton

        checkIds()

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(requireContext())

        requestNewInterstitial()

        loadInterstitialButton.setOnClickListener {
            if (interstitialAd != null) {
                interstitialAd?.show(requireActivity())
            } else {
                goToNextFragment()
            }
        }

        // Disable button if an interstitial ad is not loaded yet.
        loadInterstitialButton.isEnabled = interstitialAd != null
    }

    /**
     * Load a new interstitial ad asynchronously.
     */
    private fun requestNewInterstitial() {
        // AdMob ad unit IDs are not currently stored inside the google-services.json file.
        // Developers using AdMob can store them as custom values in a string resource file or
        // simply use constants. Note that the ad units used here are configured to return only test
        // ads, and should not be used outside this sample.
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)

        InterstitialAd.load(
            requireContext(),
            getString(R.string.interstitial_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    interstitialAd = ad
                    // Ad received, ready to display
                    loadInterstitialButton.isEnabled = true

                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            goToNextFragment()
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    interstitialAd = null
                    Log.w(TAG, "onAdFailedToLoad:${error.message}")
                }
            },
        )
    }

    private fun goToNextFragment() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    /** Called when leaving the activity  */
    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        super.onResume()
        adView.resume()
        if (interstitialAd == null) {
            requestNewInterstitial()
        }
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    private fun checkIds() {
        if (TEST_APP_ID == getString(R.string.admob_app_id)) {
            Log.w(TAG, "Your admob_app_id is not configured correctly, please see the README")
        }
    }

    companion object {
        private const val TAG = "FirstFragment"
        private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
