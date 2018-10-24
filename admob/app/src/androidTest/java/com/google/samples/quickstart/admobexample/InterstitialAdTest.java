package com.google.samples.quickstart.admobexample;


import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import com.google.samples.quickstart.admobexample.java.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class InterstitialAdTest {

    private AdViewIdlingResource mAdResource;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mAdResource = new AdViewIdlingResource(mActivityTestRule.getActivity().getAdView());
        Espresso.registerIdlingResources(mAdResource);
    }

    @After
    public void tearDown() {
        Espresso.unregisterIdlingResources(mAdResource);
    }

    @Test
    public void interstitialAdTest() {
        // Wait for ad to load
        mAdResource.setIsLoadingAd(true);

        // Confirm that banner ad appears
        onView(withId(R.id.adView))
                .check(matches(isDisplayed()));

        // Click show interstitial button
        ViewInteraction showInterstitialButton = onView(
                allOf(withId(R.id.loadInterstitialButton),
                        withText(R.string.interstitial_button_text),
                        isDisplayed()));
        showInterstitialButton.perform(click());

        // Click close interstitial button
        ViewInteraction closeInterstitialButton = onView(
                allOf(withContentDescription("Interstitial close button"), isDisplayed()));
        closeInterstitialButton.perform(click());

        // Confirm that we're on the second activity
        onView(withText(R.string.second_activity_content))
                .check(matches(isDisplayed()));
    }
}
