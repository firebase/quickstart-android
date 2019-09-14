package com.google.firebase.quickstart.firebasestorage;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import android.util.Log;

import com.google.firebase.quickstart.firebasestorage.java.MainActivity;
import com.google.firebase.quickstart.firebasestorage.java.MyUploadService;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static final String TAG = "MainActivityTest";

    private ServiceIdlingResource mUploadIdlingResource;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void grantPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getTargetContext().getPackageName();
            String testPackageName = packageName + ".test";

            // Grant "WRITE_EXTERNAL_STORAGE"
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + packageName + Manifest.permission.WRITE_EXTERNAL_STORAGE);
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + testPackageName + Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Before
    public void before() {
        // Initialize intents
        Intents.init();

        // Idling resource
        mUploadIdlingResource = new ServiceIdlingResource(mActivityTestRule.getActivity(),
                MyUploadService.class);
        Espresso.registerIdlingResources(mUploadIdlingResource);
    }

    @After
    public void after() {
        // Release intents
        Intents.release();

        // Idling resource
        if (mUploadIdlingResource != null) {
            Espresso.unregisterIdlingResources(mUploadIdlingResource);
        }
    }


    @Test
    public void uploadPhotoTest() throws InterruptedException {
        // Log out to start
        logOutIfPossible();

        // Create a temp file
        createTempFile();

        // Click sign in
        ViewInteraction signInButton = onView(
                allOf(withId(R.id.buttonSignIn), withText(R.string.sign_in_anonymously),
                        isDisplayed()));
        signInButton.perform(click());

        // Wait for sign in
        Thread.sleep(5000);

        // Click upload
        ViewInteraction uploadButton = onView(
                allOf(withId(R.id.buttonCamera), withText(R.string.camera_button_text),
                        isDisplayed()));
        uploadButton.perform(click());

        // Confirm that download link label is displayed
        onView(withText(R.string.label_link))
                .check(matches(isDisplayed()));

        // Confirm that there is a download link on screen
        onView(withId(R.id.pictureDownloadUri))
                .check(matches(withText(startsWith("https://firebasestorage.googleapis.com"))));

        // Click download
        ViewInteraction downloadButton = onView(
                allOf(withId(R.id.buttonDownload), withText(R.string.download),
                        isDisplayed()));
        downloadButton.perform(click());

        // Wait for download
        Thread.sleep(5000);

        // Confirm that a success dialog appears
        onView(withText(R.string.success)).inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Create a file to be selected by tests.
     */
    private void createTempFile() {
        // Create fake RESULT_OK Intent
        Intent intent = new Intent();
        intent.putExtra("is-espresso-test", true);

        // Create a temporary file for the result of the intent
        File external = mActivityTestRule.getActivity().getExternalFilesDir(null);
        File imageFile = new File(external, "tmp.jpg");
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "createNewFile", e);
        }
        intent.setData(Uri.fromFile(imageFile));

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(
                Activity.RESULT_OK, intent);

        // Intercept photo intent
        Matcher<Intent> pictureIntentMatch = allOf(hasAction(Intent.ACTION_GET_CONTENT));
        intending(pictureIntentMatch).respondWith(result);
    }

    /**
     * Click the 'Log Out' overflow menu if it exists (which would mean we're signed in).
     */
    private void logOutIfPossible() {
        try {
            openActionBarOverflowOrOptionsMenu(getTargetContext());
            onView(withText(R.string.log_out)).perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore exception since we only want to do this operation if it's easy.
        }

    }
}
