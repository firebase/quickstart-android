package com.google.firebase.quickstart.analytics;


import androidx.annotation.StringRes;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.quickstart.analytics.java.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
        ViewInteraction viewPager = onView(withId(R.id.viewPager));
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
