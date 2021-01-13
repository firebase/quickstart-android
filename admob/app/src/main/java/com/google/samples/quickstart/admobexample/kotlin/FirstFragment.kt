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
import com.google.android.gms.ads.MobileAds
import com.google.samples.quickstart.admobexample.R
import com.google.samples.quickstart.admobexample.databinding.FragmentFirstBinding

// [SNIPPET load_banner_ad]
// Load an ad into the AdView.
// [START load_banner_ad]
class FirstFragment : Fragment() {

    // [START_EXCLUDE]
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
    // [END_EXCLUDE]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // [START_EXCLUDE]
        adView = binding.adView
        loadInterstitialButton = binding.loadInterstitialButton
        // [END_EXCLUDE]

        checkIds()

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(context)

        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        // [END load_banner_ad]

        // AdMob ad unit IDs are not currently stored inside the google-services.json file.
        // Developers using AdMob can store them as custom values in a string resource file or
        // simply use constants. Note that the ad units used here are configured to return only test
        // ads, and should not be used outside this sample.

        // [START instantiate_interstitial_ad]
        // Create an InterstitialAd object. This same object can be re-used whenever you want to
        // show an interstitial.
        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = getString(R.string.interstitial_ad_unit_id)
        // [END instantiate_interstitial_ad]

        // [START create_interstitial_ad_listener]
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                requestNewInterstitial()
                goToNextFragment()
            }

            override fun onAdLoaded() {
                // Ad received, ready to display
                // [START_EXCLUDE]
                loadInterstitialButton.isEnabled = true
                // [END_EXCLUDE]
            }

            override fun onAdFailedToLoad(i: Int) {
                // See https://goo.gl/sCZj0H for possible error codes.
                Log.w(TAG, "onAdFailedToLoad:$i")
            }
        }
        // [END create_interstitial_ad_listener]

        // [START display_interstitial_ad]
        loadInterstitialButton.setOnClickListener {
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            } else {
                goToNextFragment()
            }
        }
        // [END display_interstitial_ad]

        // Disable button if an interstitial ad is not loaded yet.
        loadInterstitialButton.isEnabled = interstitialAd.isLoaded
    }

    /**
     * Load a new interstitial ad asynchronously.
     */
    // [START request_new_interstitial]
    private fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder().build()

        interstitialAd.loadAd(adRequest)
    }
    // [END request_new_interstitial]

    private fun goToNextFragment() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    // [START add_lifecycle_methods]
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
    // [END add_lifecycle_methods]

    private fun checkIds() {
        if (TEST_APP_ID == getString(R.string.admob_app_id)) {
            Log.w(TAG, "Your admob_app_id is not configured correctly, please see the README")
        }
    }

    companion object {
        private const val TAG = "FirstFragment"
        private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }

    // [START_EXCLUDE]
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // [END_EXCLUDE]
}