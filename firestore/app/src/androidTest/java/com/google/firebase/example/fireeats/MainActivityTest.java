package com.google.firebase.example.fireeats;

import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Rating;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class, false, false);

  @Before
  public void before() {
    // Sign out of any existing sessions
    FirebaseAuth.getInstance().signOut();
  }

  @Test
  public void testAddItemsAndReview() throws InterruptedException {
    mActivityTestRule.launchActivity(new Intent());
    Thread.sleep(2000);

    // Input email for account created in the setup.sh
    ViewInteraction textInputEditText = onView(withId(R.id.email));
    textInputEditText.perform(click()).perform(replaceText("test@mailinator.com"));
    ViewInteraction appCompatButton = onView(withId(R.id.button_next));
    appCompatButton.perform(click());
    Thread.sleep(2000);

    //Input password
    ViewInteraction textInputEditText4 = onView(withId(R.id.password));
    textInputEditText4.perform(replaceText("password"));
    ViewInteraction appCompatButton2 = onView(withId(R.id.button_done));
    appCompatButton2.perform(click());
    Thread.sleep(2000);

    // Add random items
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    ViewInteraction appCompatTextView = onView(
        allOf(withId(R.id.title), withText("Add Random Items"), childAtPosition(
            childAtPosition(withClassName(is("android.support.v7.view.menu.ListMenuItemView")), 0),
            0), isDisplayed()));
    appCompatTextView.perform(click());
    Thread.sleep(5000);

    // Click on the first restaurant
    ViewInteraction recyclerView = onView(withId(R.id.recycler_restaurants));
    recyclerView.perform(click());
    Thread.sleep(2000);

    // Click add review
    ViewInteraction floatingActionButton = onView(withId(R.id.fab_show_rating_dialog));
    floatingActionButton.perform(click());
    Thread.sleep(2000);

    //Write a review
    ViewInteraction appCompatEditText2 = onView(withId(R.id.restaurant_form_text));
    appCompatEditText2.perform(replaceText("\uD83D\uDE0E\uD83D\uDE00"), closeSoftKeyboard());

    //Submit the review
    ViewInteraction appCompatButton3 = onView(withId(R.id.restaurant_form_button));
    appCompatButton3.perform(click());
    Thread.sleep(5000);


    // Assert that the review exists
    ViewInteraction textView = onView(
        allOf(withId(R.id.rating_item_text), withText("\uD83D\uDE0E\uD83D\uDE00"),
            childAtPosition(childAtPosition(withId(R.id.recycler_ratings), 0), 4), isDisplayed()));
    textView.check(matches(withText("\uD83D\uDE0E\uD83D\uDE00")));
  }

  private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher,
      final int position) {

    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("Child at position " + position + " in parent ");
        parentMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(View view) {
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(
            ((ViewGroup) parent).getChildAt(position));
      }
    };
  }
}
