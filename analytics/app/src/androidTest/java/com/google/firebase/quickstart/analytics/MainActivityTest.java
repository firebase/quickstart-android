package com.google.firebase.quickstart.analytics;


import android.support.annotation.StringRes;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        // Select favorite food if the dialog appears
        selectFavoriteFoodIfPossible("Hot Dogs");

        // Initial title
        checkTitleText(R.string.pattern1_title);

        // Swipe, make sure we see the right titles
        ViewInteraction viewPager = onView(withId(R.id.pager));
        viewPager.check(matches(isDisplayed()));

        // Swipe left
        viewPager.perform(swipeLeft());
        checkTitleText(R.string.pattern2_title);

        // Swipe left
        viewPager.perform(swipeLeft());
        checkTitleText(R.string.pattern3_title);

        // Swipe back right
        viewPager.perform(swipeRight());
        checkTitleText(R.string.pattern2_title);
    }

    private void checkTitleText(@StringRes int resId) {
        onView(withText(resId))
                .check(matches(isDisplayed()));
    }

    private void selectFavoriteFoodIfPossible(String food) {
        try{
            ViewInteraction appCompatTextView = onView(
                    allOf(withId(android.R.id.text1), withText(food),
                            withParent(allOf(withId(R.id.select_dialog_listview),
                                    withParent(withId(R.id.contentPanel)))),
                            isDisplayed()));
            appCompatTextView.perform(click());
        } catch (NoMatchingViewException e) {
            // This is ok
        }
    }
}
