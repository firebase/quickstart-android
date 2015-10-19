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

package com.google.samples.quickstart.config;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.config.Config;
import com.google.android.gms.config.ConfigApi;
import com.google.android.gms.config.ConfigStatusCodes;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MyTestApp";
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mClient = new GoogleApiClient.Builder(this, this, this)
                .addApi(Config.API)
                .enableAutoManage(this, this)
                .build();
    }

    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
        // [START fetch_config_request]
        ConfigApi.FetchConfigRequest request = new ConfigApi.FetchConfigRequest.Builder()
                .addCustomVariable("build", "dev")
                .build();
        // [END fetch_config_request]
        // [START fetch_config_callback]
        Config.ConfigApi.fetchConfig(mClient, request)
                .setResultCallback(new ResultCallback<ConfigApi.FetchConfigResult>() {
                    @Override
                    public void onResult(ConfigApi.FetchConfigResult fetchConfigResult) {
                        Log.i(TAG, "onResult");
                        if (fetchConfigResult.getStatus().isSuccess()) {
                            long price = 100;
                            boolean isPromo =
                                    fetchConfigResult.getAsBoolean("is-promotion-on", false);
                            long discount = fetchConfigResult.getAsLong("discount", 0);
                            if (isPromo) {
                                price = (price / 100) * (price - discount);
                            }
                            String priceMsg = "Your price is $" + price;
                            TextView pv = (TextView) findViewById(R.id.priceView);
                            pv.setText(priceMsg);
                            boolean isDevBuild =
                                    fetchConfigResult.getAsBoolean("dev-features-on", false);
                            if (isDevBuild) {
                                String debugMsg = "Config set size: " +
                                        fetchConfigResult.getAllConfigKeys().size();
                                TextView dv = (TextView) findViewById(R.id.debugView);
                                dv.setText(debugMsg);
                            }
                        } else {
                            // There has been an error fetching the config
                            Log.w(TAG, "Error fetching config: " +
                                    fetchConfigResult.getStatus().toString());
                        }
                    }
                });
                // [END fetch_config_callback]
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "suspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult status) {
        Log.w(TAG, "failed: " + status);
    }

}
