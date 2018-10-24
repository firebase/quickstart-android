package com.google.firebase.example.fireeats;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.view.accessibility.AccessibilityWindowInfo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.java.MainActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;

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
    getActionBarItem(new UiSelector().textContains("Add Random Items"), TIMEOUT).click();
    device.waitForIdle(TIMEOUT);

    // Click on the first restaurant
    getById("recycler_restaurants").getChild(new UiSelector().index(0))
        .clickAndWaitForNewWindow(TIMEOUT);

    // Click add review
    getById("fabShowRatingDialog").click();

    //Write a review
    getById("restaurant_form_text").setText("\uD83D\uDE0E\uD83D\uDE00");
    closeKeyboard();

    //Submit the review
    getById("restaurant_form_button").clickAndWaitForNewWindow(TIMEOUT);

    // Assert that the review exists
    UiScrollable ratingsList = new UiScrollable(getIdSelector("recyclerRatings"));
    ratingsList.waitForExists(TIMEOUT);
    ratingsList.scrollToBeginning(100);
    Assert.assertTrue(
        getById("recyclerRatings")
            .getChild(new UiSelector().text("\uD83D\uDE0E\uD83D\uDE00"))
            .waitForExists(TIMEOUT));
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

  private UiObject getActionBarItem(UiSelector selector, long timeout) throws InterruptedException {
    final long STEP_TIMEOUT = 5000;
    UiObject item = device.findObject(selector);

    for (long i = 0; i < timeout; i += STEP_TIMEOUT) {
      openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

      if (item.waitForExists(STEP_TIMEOUT)) {
        break;
      }
    }

    return item;
  }
}
