package com.google.firebase.quickstart.auth;

import android.app.ProgressDialog;
import android.support.test.espresso.IdlingResource;

/**
 * Monitor Activity idle status by watching ProgressDialog.
 */
public class BaseActivityIdlingResource implements IdlingResource {

    private BaseActivity mActivity;
    private ResourceCallback mCallback;

    public BaseActivityIdlingResource(BaseActivity activity) {
        mActivity = activity;
    }

    @Override
    public String getName() {
        return "BaseActivityIdlingResource:" + mActivity.getLocalClassName();
    }

    @Override
    public boolean isIdleNow() {
        ProgressDialog dialog = mActivity.mProgressDialog;
        boolean idle = (dialog == null || !dialog.isShowing());

        if (mCallback != null && idle) {
            mCallback.onTransitionToIdle();
        }

        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }
}
