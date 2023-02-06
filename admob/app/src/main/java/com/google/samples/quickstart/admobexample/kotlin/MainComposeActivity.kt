package com.google.samples.quickstart.admobexample.kotlin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.samples.quickstart.admobexample.kotlin.ui.theme.AdmobTheme
import com.google.samples.quickstart.admobexample.R
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainComposeActivity : ComponentActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var adUnitId: String //="ca-app-pub-3940256099942544/1033173712" //could be set to another id
    private val buttonClickLambda = { displayNewInterstitial() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adUnitId  = getString(R.string.interstitial_ad_unit_id)
        
        setContent {

            AdmobTheme {
                 //A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppView( buttonClickEventAdLoader = buttonClickLambda ) // call Composable UI
                }
            }

            initializeInterstitial()
        }
    }

    private fun setInterstitialCallback() {


        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                initializeInterstitial()    // get a new ad
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
                initializeInterstitial()    // get a new ad
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    private fun initializeInterstitial(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun displayNewInterstitial(){
        if (mInterstitialAd != null) {  // ad is available
            setInterstitialCallback()   // set the callback methods
            mInterstitialAd?.show(this)
        } else {                        // ad is not available
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
            initializeInterstitial()
        }
    }


}


@Composable
fun MainAppView(modifier: Modifier = Modifier, buttonClickEventAdLoader : () -> Unit = {}){
    Scaffold(
        topBar = {  // top bar with app name
            AppNameBanner()
                 },
        content = {

            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxWidth()
                    .fillMaxHeight(),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Image(painter = painterResource(R.drawable.firebase_lockup_400), contentDescription = "")
                Spacer(modifier = Modifier.height(160.dp))
                InterstitialButton(myClickEventInterstitialLoader = { buttonClickEventAdLoader() })

            }
                  },
        bottomBar = { // keeps the banner ad at the bottom!
            AdvertBanner()
        }
    )

}

@Composable
fun AppNameBanner(modifier: Modifier = Modifier){
    TopAppBar(
        backgroundColor = colorResource(R.color.colorPrimary)
    ) {
        androidx.compose.material.Text(
            text = stringResource(R.string.app_name),
            style = androidx.compose.material.MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
            color = Color.White
        )
    }
}

@Composable
fun InterstitialButton(modifier: Modifier = Modifier, myClickEventInterstitialLoader : () -> Unit = {}){
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorAccent)),
        onClick = { myClickEventInterstitialLoader() }    //lambda for onClick action
    ) {
        Text(
            text = stringResource(R.string.interstitial_button_text),
            fontSize = 24.sp
        )
    }
}

@Composable
fun AdvertBanner(modifier: Modifier = Modifier) { // banner advert

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = context.getString(R.string.banner_ad_unit_id)
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }

}
