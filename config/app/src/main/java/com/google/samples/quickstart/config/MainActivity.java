/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * For more information on setting up and running this sample code, see
 * https://developers.google.com/firebase/docs/remote-config/android
 */

package com.google.samples.quickstart.config;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.config.FirebaseRemoteConfig;
import com.google.android.gms.config.FirebaseRemoteConfigException;
import com.google.android.gms.config.FirebaseRemoteConfigFetchCallback;
import com.google.android.gms.config.FirebaseRemoteConfigSettings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyTestApp";
    // Original price
    private static final long PRICE = 100;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView mPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPriceTextView = (TextView) findViewById(R.id.priceView);

        Button fetchButton = (Button) findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDiscount();
            }
        });

        // Initialize Firebase App. This is required to use Firebase Remote Config.
        FirebaseApp.initializeApp(this, getResources().getString(R.string.google_app_id),
                new FirebaseOptions.Builder(getResources().getString(R.string.google_api_key)).build());

        // Get Remote Config instance.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        // Fetch discount config.
        fetchDiscount();
    }

    /**
     * Fetch discount from server.
     */
    private void fetchDiscount() {
        mPriceTextView.setText(getResources().getString(R.string.fetching_msg));

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        // [START fetch_config_with_callback]
        mFirebaseRemoteConfig.fetch(cacheExpiration, new FirebaseRemoteConfigFetchCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Fetch Succeeded");
                // Once the config is successfully fetched it must be activated before newly fetched
                // values are returned.
                mFirebaseRemoteConfig.activateFetched();
                displayPrice();
            }

            @Override
            public void onFailure(FirebaseRemoteConfigException e) {
                Log.d(TAG, "Fetch failed");
                mPriceTextView.setText(getResources().getString(R.string.price_prefix) + PRICE);
            }
        });
        // [END fetch_config_with_callback]
    }

    /**
     * Display price with discount applied if promotion is on. Otherwise display original price.
     */
    private void displayPrice() {
        if (mFirebaseRemoteConfig.getBoolean("is_promotion_on")) {
            long discountedPrice = PRICE - mFirebaseRemoteConfig.getLong("discount");
            mPriceTextView.setText(getResources().getString(R.string.price_prefix) + discountedPrice);
        } else {
            mPriceTextView.setText(getResources().getString(R.string.price_prefix) + PRICE);
        }

    }
}
