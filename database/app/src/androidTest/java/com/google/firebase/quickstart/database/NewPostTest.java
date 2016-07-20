package com.google.firebase.quickstart.database;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
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
public class NewPostTest {

    @Rule
    public ActivityTestRule<SignInActivity> mActivityTestRule = new ActivityTestRule<>(SignInActivity.class);

    @Test
    public void newPostTest() {
        // Generate user and post content
        String username = "user" + randomDigits();
        String email = username + "@example.com";
        String password = "testuser";
        String postTitle = "Title " + randomDigits();
        String postContent = "Content " + randomDigits();

        // Go back to the sign in screen if we're logged in from a previous test
        logOutIfPossible();

        // Select email field
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.field_email),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText.perform(click());

        // Enter email address
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.field_email),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(email));

        // Enter password
        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.field_password),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText(password));

        // Click sign up
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_sign_up), withText(R.string.sign_up),
                        withParent(withId(R.id.layout_buttons)),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Click new post button
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_new_post), isDisplayed()));
        floatingActionButton.perform(click());

        // Enter post title
        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.field_title), isDisplayed()));
        appCompatEditText4.perform(replaceText(postTitle));

        // Enter post content
        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.field_body), isDisplayed()));
        appCompatEditText5.perform(replaceText(postContent));

        // Click submit button
        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.fab_submit_post), isDisplayed()));
        floatingActionButton2.perform(click());

        // Navigate to "My Posts"
        ViewInteraction appCompatTextView = onView(
                allOf(withText(R.string.heading_my_posts), isDisplayed()));
        appCompatTextView.perform(click());

        // Check that the title is correct
        ViewInteraction textView = onView(
                allOf(withId(R.id.post_title), withText(postTitle), isDisplayed()));
        textView.check(matches(withText(postTitle)));

        // Check that the content is correct
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.post_body), withText(postContent), isDisplayed()));
        textView2.check(matches(withText(postContent)));

        // Check that it has zero stars
        ViewInteraction textView3 = onView(
                allOf(withId(R.id.post_num_stars), withText("0"),
                        withParent(withId(R.id.star_layout)),
                        isDisplayed()));
        textView3.check(matches(withText("0")));

    }

    /**
     * Click the 'Log Out' overflow menu if it exists (which would mean we're signed in).
     */
    private void logOutIfPossible() {
        try {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
            onView(withText(R.string.menu_logout)).perform(click());
        } catch (NoMatchingViewException e) {
            // Ignore exception since we only want to do this operation if it's easy.
        }

    }

    /**
     * Generate a random string of digits.
     */
    private String randomDigits() {
        Random random = new Random();
        return String.valueOf(random.nextInt(99999999));
    }

}
