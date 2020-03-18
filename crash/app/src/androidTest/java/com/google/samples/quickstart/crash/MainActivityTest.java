package com.google.samples.quickstart.crash;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.filters.LargeTest;
import android.widget.CheckBox;

import com.google.samples.quickstart.crash.java.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void caughtExceptionTest() {
        // Make sure the checkbox is on screen
        ViewInteraction al = onView(
                allOf(withId(R.id.catchCrashCheckBox),
                        withText(R.string.catch_crash_checkbox_label),
                        isDisplayed()));

        // Click the checkbox if it's not already checked
        CheckBox checkBox = (CheckBox) mActivityTestRule.getActivity()
                .findViewById(R.id.catchCrashCheckBox);
        if (!checkBox.isChecked()) {
            al.perform(click());
        }

        // Cause a crash
        ViewInteraction ak = onView(
                allOf(withId(R.id.crashButton),
                        withText(R.string.crash_button_label),
                        isDisplayed()));
        ak.perform(click());
    }
}
