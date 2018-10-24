package com.google.samples.quickstart.functions;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.filters.LargeTest;

import com.google.samples.quickstart.functions.java.MainActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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

  @Before public void setUp() {
    UiDevice.getInstance(getInstrumentation());
  }

  @Test
  public void testAddNumber() {
    ViewInteraction firstNumber = onView(withId(R.id.fieldFirstNumber));
    ViewInteraction secondNumber = onView(withId(R.id.fieldSecondNumber));
    ViewInteraction addButton = onView(withId(R.id.buttonCalculate));

    firstNumber.perform(replaceText("32"));
    secondNumber.perform(replaceText("16"));

    addButton.perform(scrollTo(), click());

    Assert.assertTrue(
        new UiObject(new UiSelector()
          .resourceId("com.google.samples.quickstart.functions:id/fieldAddResult").text("48"))
        .waitForExists(60000));
  }
}
