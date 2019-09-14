package com.google.firebase.quickstart.auth;

import android.app.ProgressDialog;
import androidx.test.espresso.IdlingResource;

import com.google.firebase.quickstart.auth.java.AnonymousAuthActivity;
import com.google.firebase.quickstart.auth.java.BaseActivity;
import com.google.firebase.quickstart.auth.java.EmailPasswordActivity;

/**
 * Monitor Activity idle status by watching ProgressDialog.
 */
public class BaseActivityIdlingResource implements IdlingResource {

    private BaseActivity mActivity;
    private ResourceCallback mCallback;

    public BaseActivityIdlingResource(AnonymousAuthActivity activity) {
        mActivity = activity;
    }

    public BaseActivityIdlingResource(EmailPasswordActivity activity) {
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
