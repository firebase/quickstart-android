package com.google.samples.quickstart.app_indexing;
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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class UiAutomatorTest {

    private static final long LAUNCH_TIMEOUT = 10000;
    private static final long UI_TIMEOUT = 10000;

    private static final String APP_PACKAGE = MainActivity.class.getPackage().getName();

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
    public void testDeepLink() {
        // Start from the home screen
        mDevice.pressHome();
        // Create intent to MainActivity
        String link = "http://www.example.com/articles/test";
        Intent linkIntent = new Intent(Intent.ACTION_VIEW)
                .setClassName(APP_PACKAGE, APP_PACKAGE + ".MainActivity")
                .setData(Uri.parse(link))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Start activity
        mContext.startActivity(linkIntent);

        // Check that link text is displayed
        assertTrue(mDevice.wait(Until.hasObject(By.text(link)), UI_TIMEOUT));
    }
}
