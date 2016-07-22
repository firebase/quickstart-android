package com.google.firebase.quickstart.firebasestorage;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static final String TAG = "MainActivityTest";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void before() {
        // Initialize intents
        Intents.init();

        // Create fake RESULT_OK Intent
        Intent intent = new Intent();
        intent.putExtra("is-espresso-test", true);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(
                Activity.RESULT_OK, intent);

        // Intercept photo intent
        Matcher<Intent> pictureIntentMatch = allOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
        intending(pictureIntentMatch).respondWith(result);
    }

    @After
    public void after() {
        // Release intents
        Intents.release();
    }


    @Test
    public void uploadPhotoTest() {
        // Log out to start
        logOutIfPossible();

        // Click sign in
        ViewInteraction signInButton = onView(
                allOf(withId(R.id.button_sign_in), withText(R.string.sign_in_anonymously),
                        isDisplayed()));
        signInButton.perform(click());

        // TODO(samstern): what if permission has not been granted yet?

        // Click upload
        ViewInteraction uploadButton = onView(
                allOf(withId(R.id.button_camera), withText(R.string.camera_button_text),
                        isDisplayed()));
        uploadButton.perform(click());

        // Confirm that download link label is displayed
        onView(withText(R.string.label_link))
                .check(matches(isDisplayed()));

        // Confirm that there is a download link on screen
        onView(withId(R.id.picture_download_uri))
                .check(matches(withText(startsWith("https://firebasestorage.googleapis.com"))));

        // Click download
        ViewInteraction downloadButton = onView(
                allOf(withId(R.id.button_download), withText(R.string.download),
                        isDisplayed()));
        downloadButton.perform(click());

        // Confirm that a success dialog appears
        onView(withText(R.string.success)).inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Click the 'Log Out' overflow menu if it exists (which would mean we're signed in).
     */
    private void logOutIfPossible() {
        try {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
            onView(withText(R.string.log_out)).perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore exception since we only want to do this operation if it's easy.
        }

    }
}
