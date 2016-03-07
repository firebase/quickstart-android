/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.invites;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import com.google.android.gms.appinvite.AppInviteReferral;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class UiAutomatorTest {

    private static final long LAUNCH_TIMEOUT = 5000;
    private static final long UI_TIMEOUT = 5000;

    private static final String APP_PACKAGE = MainActivity.class.getPackage().getName();

    private static final String CLASS_BUTTON = "android.widget.Button";
    private static final String CLASS_CHECKED_TEXT_VIEW = "android.widget.CheckedTextView";

    private UiDevice mDevice;
    private Context mContext;

    @Before
    public void setUp() {
        // Get the device instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertThat(mDevice, notNullValue());

        // Start from the home screen
        mDevice.pressHome();

        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        Intent intent = new Intent()
                .setClassName(APP_PACKAGE, APP_PACKAGE + ".MainActivity")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testLaunchDialog() {
        // Floating action button
        BySelector fabSelector = By.res(APP_PACKAGE, "invite_button");

        // Click floating action button
        Assert.assertTrue(mDevice.wait(Until.hasObject(fabSelector), UI_TIMEOUT));
        mDevice.findObject(fabSelector).click();

        // Radio button in the account picker dialog
        BySelector firstAccountSelector = By.clazz(CLASS_CHECKED_TEXT_VIEW);

        // Wait for account picker (may not appear)
        if (mDevice.wait(Until.hasObject(firstAccountSelector), UI_TIMEOUT)) {
            // Click first account
            mDevice.findObjects(firstAccountSelector).get(0).click();

            // Button with the text "OK" (enabled)
            BySelector okButtonSelector = By.clazz(CLASS_BUTTON).text("OK").enabled(true);

            // Click OK on account picker
            Assert.assertTrue(mDevice.wait(Until.hasObject(okButtonSelector), UI_TIMEOUT));
            mDevice.findObject(okButtonSelector).click();
        }

        // Check that we can see the title and message
        String titleText = mContext.getString(R.string.invitation_title);
        String messageText = mContext.getString(R.string.invitation_message);

        BySelector titleTextSelector = By.text(titleText);
        BySelector messageTextSelector = By.text(messageText);

        assertTrue(mDevice.wait(Until.hasObject(titleTextSelector), UI_TIMEOUT));
        assertTrue(mDevice.wait(Until.hasObject(messageTextSelector), UI_TIMEOUT));

        // Back out of the dialog
        mDevice.pressBack();
    }

    @Test
    public void testDeepLink() {
        // Create intent to MainActivity
        Intent deepLinkIntent = new Intent()
            .setClassName(APP_PACKAGE, APP_PACKAGE + ".MainActivity")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Load it with AppInvite information
        String invitationId = "12345";
        String deepLink = "http://example.com/12345";
        AppInviteReferral.addReferralDataToIntent(invitationId, deepLink, deepLinkIntent);

        // Start activity
        mContext.startActivity(deepLinkIntent);

        // Check that invitation ID is displayed
        String invitationIdText = mContext.getString(R.string.invitation_id_fmt, invitationId);
        assertTrue(mDevice.wait(Until.hasObject(By.text(invitationIdText)), UI_TIMEOUT));

        // Check that deep link is displayed
        String deepLinkText = mContext.getString(R.string.deep_link_fmt, deepLink);
        assertTrue(mDevice.wait(Until.hasObject(By.text(deepLinkText)), UI_TIMEOUT));

        // Make sure there is an OK button, click it
        String okText = mContext.getString(android.R.string.ok);
        assertTrue(mDevice.wait(Until.hasObject(By.text(okText)), UI_TIMEOUT));
        mDevice.findObject(By.text(okText)).click();

        // Make sure that the dialog is gone
        String dialogTitleText = mContext.getString(R.string.invite_dialog_title);
        assertTrue(mDevice.wait(Until.gone(By.text(dialogTitleText)), UI_TIMEOUT));
    }
}
