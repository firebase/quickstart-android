package com.google.firebase.quickstart.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Receiver to capture tokens broadcast via ADB and insert them into the
 * running application to facilitate easy testing of custom authentication.
 */
public abstract class TokenBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "TokenBroadcastReceiver";

    public static final String ACTION_TOKEN = "com.google.example.ACTION_TOKEN";
    public static final String EXTRA_KEY_TOKEN = "key_token";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive:" + intent);

        if (ACTION_TOKEN.equals(intent.getAction())) {
            String token = intent.getExtras().getString(EXTRA_KEY_TOKEN);
            onNewToken(token);
        }
    }

    public static IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter(ACTION_TOKEN);
        return filter;
    }

    public abstract void onNewToken(String token);

}
