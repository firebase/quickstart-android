package com.google.samples.quickstart.functions;

import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.filters.LargeTest;

import com.google.samples.quickstart.functions.java.MainActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
