package com.google.firebase.fiamquickstart;

import android.os.RemoteException;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

  private Matcher<Root> rootMatcher;

  @Before
  public void setUp() {
    rootMatcher = withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView())));
  }

  @After
  public void tearDown() {
      getView(R.id.collapse_button).perform(click());
  }


  @Test
  public void testFiamDisplaysOnForegroundCampaign() {
    reopen_app(); // reopen app to correctly trigger fetch
    getView(R.id.modal_root).check(matches(isDisplayed()));
  }

  @Test
  public void testFiamDisplaysContextualTriggerCampaign() {
    onView(withId(R.id.eventTriggerButton)).perform(click());
    getView(R.id.modal_root).check(matches(isDisplayed()));
  }

  private void reopen_app() {
    press_recent();
    press_back();
  }

  private void press_recent() {
    try {
      UiDevice.getInstance(getInstrumentation()).pressRecentApps();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    sleep();
  }

  private void press_back() {
    UiDevice.getInstance(getInstrumentation()).pressBack();
    sleep();

  }

  private void sleep() {
    try {
      Thread.sleep(3000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @NonNull
  private ViewInteraction getView(@IdRes int id) {
    return onView(withId(id)).inRoot(rootMatcher);
  }
}
