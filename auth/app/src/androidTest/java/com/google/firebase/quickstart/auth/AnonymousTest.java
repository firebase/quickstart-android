package com.google.firebase.quickstart.auth;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.quickstart.auth.java.AnonymousAuthActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AnonymousTest {

    private IdlingResource mActivityResource;

    @Rule
    public ActivityTestRule<AnonymousAuthActivity> mActivityTestRule =
            new ActivityTestRule<>(AnonymousAuthActivity.class);

    @Before
    public void setUp() {
        if (mActivityResource != null) {
            Espresso.unregisterIdlingResources(mActivityResource);
        }

        // Register Activity as idling resource
        mActivityResource = new BaseActivityIdlingResource(mActivityTestRule.getActivity());
        Espresso.registerIdlingResources(mActivityResource);
    }

    @After
    public void tearDown() {
        if (mActivityResource != null) {
            Espresso.unregisterIdlingResources(mActivityResource);
        }
    }

    @Test
    public void anonymousSignInTest() {
        // Sign out if possible
        signOutIfPossible();

        // Click sign in
        onView(allOf(withId(R.id.buttonAnonymousSignIn),
                withText(R.string.sign_in), isDisplayed())).perform(click());

        // Make sure userID and email look right
        String idString = mActivityTestRule.getActivity().getString(R.string.id_fmt, "");
        String emailString = mActivityTestRule.getActivity().getString(R.string.email_fmt, "");

        onView(withText(startsWith(idString)))
                .check(matches(isDisplayed()));

        onView(withText(startsWith(emailString)))
                .check(matches(isDisplayed()));
    }

    private void signOutIfPossible() {
        try {
            onView(allOf(withId(R.id.buttonAnonymousSignOut), isDisplayed()))
                    .perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore
        }

    }
}
