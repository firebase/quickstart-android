package com.google.firebase.quickstart.deeplinks;


import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.quickstart.deeplinks.java.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * NOTE: This test will fail if R.string.app_code is not set in the build.gradle file
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void testDeepLinkReceive() {
        // Build a deep link
        Uri linkUri = Uri.parse("https://example.com/12345");
        Uri uri = rule.getActivity().buildDeepLink(linkUri, 0);

        // Launch an intent to view the deep link
        Intent intent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .setClass(rule.getActivity().getApplicationContext(), MainActivity.class)
                .setData(uri);
        rule.getActivity().startActivity(intent);

        // Confirm the deep link content is displayed
        onView(withId(R.id.linkViewReceive))
                .check(matches(allOf(
                        withText(containsString(linkUri.toString())),
                        isDisplayed())));
    }

}
