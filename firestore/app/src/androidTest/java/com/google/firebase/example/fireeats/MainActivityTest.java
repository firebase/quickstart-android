package com.google.firebase.example.fireeats;

import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.UiScrollable;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.google.firebase.auth.FirebaseAuth;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest @RunWith(AndroidJUnit4.class) public class MainActivityTest {

  @Rule public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class, false, false);

  UiDevice device;
  final long TIMEOUT = 300000;  // Five minute timeout because our CI is slooow.

  @Before public void before() {
    // Sign out of any existing sessions
    FirebaseAuth.getInstance().signOut();
    device = UiDevice.getInstance(getInstrumentation());
  }

  @Test public void testAddItemsAndReview() throws Exception {
    mActivityTestRule.launchActivity(new Intent());

    // Input email for account created in the setup.sh
    getById("email").setText("test@mailinator.com");
    closeKeyboard();
    getById("button_next").clickAndWaitForNewWindow(TIMEOUT);

    //Input password
    getById("password").setText("password");
    closeKeyboard();
    getById("button_done").clickAndWaitForNewWindow(TIMEOUT);

    // Add random items
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    ViewInteraction appCompatTextView = onView(
        allOf(withId(R.id.title), withText("Add Random Items"), childAtPosition(
            childAtPosition(withClassName(is("android.support.v7.view.menu.ListMenuItemView")), 0),
            0), isDisplayed()));
    appCompatTextView.perform(click());
    device.waitForIdle(TIMEOUT);

    // Click on the first restaurant
    getById("recycler_restaurants").getChild(new UiSelector().index(0))
        .clickAndWaitForNewWindow(TIMEOUT);

    // Click add review
    getById("fab_show_rating_dialog").click();

    //Write a review
    getById("restaurant_form_text").setText("\uD83D\uDE0E\uD83D\uDE00");
    closeKeyboard();

    //Submit the review
    getById("restaurant_form_button").clickAndWaitForNewWindow(TIMEOUT);

    // Assert that the review exists (getChildByText() throws on failure)
    new UiScrollable(getIdSelector("recycler_ratings"))
        .getChildByText(new UiSelector(), "\uD83D\uDE0E\uD83D\uDE00");
  }

  private UiObject getById(String id) {
    UiObject obj = device.findObject(getIdSelector(id));
    obj.waitForExists(TIMEOUT);
    return obj;
  }

  private UiSelector getIdSelector(String id) {
    return new UiSelector().resourceId("com.google.firebase.example.fireeats:id/" + id);
  }

  private void closeKeyboard() {
    for (AccessibilityWindowInfo w : getInstrumentation().getUiAutomation().getWindows()) {
      if (w.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
        device.pressBack();
        return;
      }
    }
  }

  private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher,
      final int position) {

    return new TypeSafeMatcher<View>() {
      @Override public void describeTo(Description description) {
        description.appendText("Child at position " + position + " in parent ");
        parentMatcher.describeTo(description);
      }

      @Override public boolean matchesSafely(View view) {
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(
            ((ViewGroup) parent).getChildAt(position));
      }
    };
  }
}
