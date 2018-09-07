package com.google.samples.quickstart.admobexample.kotlin

import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.samples.quickstart.admobexample.R
import com.google.samples.quickstart.admobexample.java.SecondActivity
import kotlinx.android.synthetic.main.activity_main.*


class KotlinMainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    // [END add_lifecycle_methods]

    @get:VisibleForTesting
    internal var adView: AdView? = null
        private set
    // [START_EXCLUDE]
    private lateinit var mInterstitialAd: InterstitialAd

    // [END_EXCLUDE]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
        // [END load_banner_ad]

        // AdMob ad unit IDs are not currently stored inside the google-services.json file.
        // Developers using AdMob can store them as custom values in a string resource file or
        // simply use constants. Note that the ad units used here are configured to return only test
        // ads, and should not be used outside this sample.

        // [START instantiate_interstitial_ad]
        // Create an InterstitialAd object. This same object can be re-used whenever you want to
        // show an interstitial.
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.interstitial_ad_unit_id)
        // [END instantiate_interstitial_ad]

        // [START create_interstitial_ad_listener]
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                requestNewInterstitial()
                beginSecondActivity()
            }

            override fun onAdLoaded() {
                // Ad received, ready to display
                // [START_EXCLUDE]
                load_interstitial_button.isEnabled = true
                // [END_EXCLUDE]
            }

            override fun onAdFailedToLoad(i: Int) {
                // See https://goo.gl/sCZj0H for possible error codes.
                Log.w(TAG, "onAdFailedToLoad:$i")
            }
        }
        // [END create_interstitial_ad_listener]

        // [START display_interstitial_ad]
        load_interstitial_button.setOnClickListener {
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                beginSecondActivity()
            }
        }
        // [END display_interstitial_ad]

        // Disable button if an interstitial ad is not loaded yet.
        load_interstitial_button.isEnabled = mInterstitialAd.isLoaded
    }

    /**
     * Load a new interstitial ad asynchronously.
     */
    // [START request_new_interstitial]
    private fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder()
                .build()

        mInterstitialAd.loadAd(adRequest)
    }
    // [END request_new_interstitial]

    private fun beginSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }

    // [START add_lifecycle_methods]
    /** Called when leaving the activity  */
    public override fun onPause() {
        if (adView != null) {
            adView!!.pause()
        }
        super.onPause()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()
        if (adView != null) {
            adView!!.resume()
        }
        if (!mInterstitialAd.isLoaded) {
            requestNewInterstitial()
        }
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroy()
    }

}
