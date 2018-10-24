package com.google.firebase.quickstart.auth;


import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import com.google.firebase.quickstart.auth.java.EmailPasswordActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EmailPasswordTest {

    private IdlingResource mActivityResource;

    @Rule
    public ActivityTestRule<EmailPasswordActivity> mActivityTestRule =
            new ActivityTestRule<>(EmailPasswordActivity.class);

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
    public void failedSignInTest() {
        String email = "test@test.com";
        String password = "123456";

        // Make sure we're signed out
        signOutIfPossible();

        // Enter email
        enterEmail(email);

        // Enter password
        enterPassword(password);

        // Click sign in
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.emailSignInButton), withText(R.string.sign_in),
                        withParent(withId(R.id.emailPasswordButtons)),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Check that auth failed
        onView(withText(R.string.auth_failed))
                .check(matches(isDisplayed()));
    }

    @Test
    public void successfulSignUpAndSignInTest() {
        String email = "user" + randomInt() + "@example.com";
        String password = "password" + randomInt();

        // Make sure we're signed out
        signOutIfPossible();

        // Enter email
        enterEmail(email);

        // Enter password
        enterPassword(password);

        // Click sign up
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.emailCreateAccountButton), withText(R.string.create_account),
                        withParent(withId(R.id.emailPasswordButtons)),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Sign out button shown
        onView(allOf(withId(R.id.signOutButton), withText(R.string.sign_out), isDisplayed()));

        // User email shown
        String emailString = mActivityTestRule.getActivity()
                .getString(R.string.emailpassword_status_fmt, email);
        onView(withText(emailString))
                .check(matches(isDisplayed()));

        // Sign out
        signOutIfPossible();

        // Sign back in with the email and password
        enterEmail(email);
        enterPassword(password);

        // Click sign in
        ViewInteraction signInButton = onView(
                allOf(withId(R.id.emailSignInButton), withText(R.string.sign_in),
                        withParent(withId(R.id.emailPasswordButtons)),
                        isDisplayed()));
        signInButton.perform(click());

        // User email shown
        onView(withText(emailString))
                .check(matches(isDisplayed()));
    }

    private void signOutIfPossible() {
        try {
            onView(allOf(withId(R.id.signOutButton), withText(R.string.sign_out), isDisplayed()))
                    .perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore
        }

    }

    private void enterEmail(String email) {
        ViewInteraction emailField = onView(
                allOf(withId(R.id.fieldEmail),
                        withParent(withId(R.id.emailPasswordFields)),
                        isDisplayed()));
        emailField.perform(replaceText(email));
    }

    private void enterPassword(String password) {
        ViewInteraction passwordField = onView(
                allOf(withId(R.id.fieldPassword),
                        withParent(withId(R.id.emailPasswordFields)),
                        isDisplayed()));
        passwordField.perform(replaceText(password));
    }

    private String randomInt() {
        return String.valueOf(((new Random()).nextInt(100000)));
    }

}
