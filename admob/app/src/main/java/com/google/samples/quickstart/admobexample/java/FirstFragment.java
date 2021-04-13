package com.google.samples.quickstart.admobexample.java;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.samples.quickstart.admobexample.R;
import com.google.samples.quickstart.admobexample.databinding.FragmentFirstBinding;

class FirstFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private static final String TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713";

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private Button mLoadInterstitialButton;
    private FragmentFirstBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkIds();

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(getContext());

        mAdView = binding.adView;

        requestNewInterstitial();

        mLoadInterstitialButton = binding.loadInterstitialButton;
        mLoadInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(getActivity());
                } else {
                    beginSecondActivity();
                }
            }
        });

        // Disable button if an interstitial ad is not loaded yet.
        mLoadInterstitialButton.setEnabled(mInterstitialAd != null);
    }

    /**
     * Load a new interstitial ad asynchronously.
     */
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // AdMob ad unit IDs are not currently stored inside the google-services.json file.
        // Developers using AdMob can store them as custom values in a string resource file or
        // simply use constants. Note that the ad units used here are configured to return only test
        // ads, and should not be used outside this sample.
        InterstitialAd.load(getContext(), getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;

                // Ad received, ready to display
                if (mLoadInterstitialButton != null) {
                    mLoadInterstitialButton.setEnabled(true);
                }

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        requestNewInterstitial();
                        beginSecondActivity();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd = null;
                Log.w(TAG, "onAdFailedToLoad:" + loadAdError.getMessage());
            }
        });
    }

    private void beginSecondActivity() {
        NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SecondFragment);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (mInterstitialAd == null) {
            requestNewInterstitial();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @VisibleForTesting
    public AdView getAdView() {
        return mAdView;
    }

    private void checkIds() {
        if (TEST_APP_ID.equals(getString(R.string.admob_app_id))) {
            Log.w(TAG, "Your admob_app_id is not configured correctly, please see the README");
        }
    }
}
