package com.google.samples.quickstart.admobexample.kotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.samples.quickstart.admobexample.R
import com.google.samples.quickstart.admobexample.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var adView: AdView
    private lateinit var loadInterstitialButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adView = binding.adView
        loadInterstitialButton = binding.loadInterstitialButton

        checkIds()

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(context)

        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)

        // AdMob ad unit IDs are not currently stored inside the google-services.json file.
        // Developers using AdMob can store them as custom values in a string resource file or
        // simply use constants. Note that the ad units used here are configured to return only test
        // ads, and should not be used outside this sample.

        // Create an InterstitialAd object. This same object can be re-used whenever you want to
        // show an interstitial.
        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = getString(R.string.interstitial_ad_unit_id)

        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                requestNewInterstitial()
                goToNextFragment()
            }

            override fun onAdLoaded() {
                // Ad received, ready to display
                loadInterstitialButton.isEnabled = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "onAdFailedToLoad:${error.message}")
            }
        }

        loadInterstitialButton.setOnClickListener {
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            } else {
                goToNextFragment()
            }
        }

        // Disable button if an interstitial ad is not loaded yet.
        loadInterstitialButton.isEnabled = interstitialAd.isLoaded
    }

    /**
     * Load a new interstitial ad asynchronously.
     */
    private fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder().build()

        interstitialAd.loadAd(adRequest)
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
        if (!interstitialAd.isLoaded) {
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