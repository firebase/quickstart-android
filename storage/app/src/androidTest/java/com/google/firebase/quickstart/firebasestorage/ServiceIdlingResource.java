package com.google.firebase.quickstart.firebasestorage;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.espresso.IdlingResource;

/**
 * Idling Resource for when service is running.
 * Adapted from: https://github.com/chiuki/espresso-samples
 */
public class ServiceIdlingResource implements IdlingResource {

    private final Context mContext;
    private final Class mServiceClass;
    private ResourceCallback mResourceCallback;

    public ServiceIdlingResource(Context context, Class serviceClass) {
        this.mContext = context;
        this.mServiceClass = serviceClass;
    }

    @Override
    public String getName() {
        return "IdlingResource<" + mServiceClass.getSimpleName() +">";
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = !isIntentServiceRunning();
        if (idle && mResourceCallback != null) {
            mResourceCallback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.mResourceCallback = resourceCallback;
    }

    private boolean isIntentServiceRunning() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (mServiceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
