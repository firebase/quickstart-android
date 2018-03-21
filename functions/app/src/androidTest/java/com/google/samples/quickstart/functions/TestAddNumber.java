package com.google.samples.quickstart.functions;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest @RunWith(AndroidJUnit4.class)
public class TestAddNumber {
  @Rule public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Test
  public void testAddNumber() {
    ViewInteraction firstNumber = onView(withId(R.id.field_first_number));
    ViewInteraction secondNumber = onView(withId(R.id.field_second_number));
    ViewInteraction addButton = onView(withId(R.id.button_calculate));
    ViewInteraction sumResult = onView(withId(R.id.field_add_result));

    firstNumber.perform(replaceText("32"));
    secondNumber.perform(replaceText("16"));

    addButton.perform(scrollTo(), click());
    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    //TODO(samstern) : add a progress indicator to the UI
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    sumResult.check(matches(withText("48")));
  }
}
