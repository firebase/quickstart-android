package com.google.firebase.example.fireeats;

import android.content.Intent;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import java.util.concurrent.ExecutionException;
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
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest @RunWith(AndroidJUnit4.class) public class MainActivityTest3 {

  @Before
  public void setup() throws ExecutionException, InterruptedException {
    Task t = FirebaseAuth.getInstance().signInWithEmailAndPassword("test@gmail.com", "123456");
    Tasks.await(t);
  }

  @Rule public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class, false, false);


  @Test public void mainActivityTest3() {
    mActivityTestRule.launchActivity(new Intent());

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction addItems = onView(withId(R.id.menu_add_items));
    addItems.perform(click());

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //
    //ViewInteraction appCompatTextView = onView(
    //    allOf(withId(R.id.title), withText("Add Random Items"), childAtPosition(
    //        childAtPosition(withClassName(is("android.support.v7.view.menu.ListMenuItemView")), 0),
    //        0), isDisplayed()));
    //appCompatTextView.perform(click());
    //
    //ViewInteraction cardView = onView(allOf(withId(R.id.filter_bar), childAtPosition(
    //    allOf(withId(R.id.filter_bar_container),
    //        childAtPosition(withClassName(is("android.widget.RelativeLayout")), 1)), 0),
    //    isDisplayed()));
    //cardView.perform(click());
    //
    //ViewInteraction appCompatSpinner = onView(allOf(withId(R.id.spinner_category),
    //    childAtPosition(childAtPosition(withId(R.id.filters_form), 1), 1), isDisplayed()));
    //appCompatSpinner.perform(click());
    //
    //DataInteraction appCompatTextView2 = onData(anything()).inAdapterView(
    //    childAtPosition(withClassName(is("android.widget.PopupWindow$PopupBackgroundView")), 0))
    //    .atPosition(6);
    //appCompatTextView2.perform(click());
    //
    //ViewInteraction appCompatButton3 = onView(allOf(withId(R.id.button_search), withText("Apply"),
    //    childAtPosition(childAtPosition(withId(R.id.filters_form), 5), 2), isDisplayed()));
    //appCompatButton3.perform(click());
    //
    //ViewInteraction textView = onView(
    //    allOf(withId(R.id.restaurant_item_category), withText("Indian"),
    //        childAtPosition(childAtPosition(withId(R.id.recycler_restaurants), 0), 5),
    //        isDisplayed()));
    //textView.check(matches(withText("Indian")));
    //
    //ViewInteraction recyclerView = onView(allOf(withId(R.id.recycler_restaurants),
    //    childAtPosition(withClassName(is("android.widget.RelativeLayout")), 2)));
    //recyclerView.perform(actionOnItemAtPosition(0, click()));
    //
    //// Added a sleep statement to match the app's execution delay.
    //// The recommended way to handle such scenarios is to use Espresso idling resources:
    //// https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    //try {
    //  Thread.sleep(5000);
    //} catch (InterruptedException e) {
    //  e.printStackTrace();
    //}
    //
    //ViewInteraction recyclerView2 = onView(allOf(withId(R.id.recycler_restaurants),
    //    childAtPosition(withClassName(is("android.widget.RelativeLayout")), 2)));
    //recyclerView2.perform(actionOnItemAtPosition(0, click()));
    //
    //// Added a sleep statement to match the app's execution delay.
    //// The recommended way to handle such scenarios is to use Espresso idling resources:
    //// https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    //try {
    //  Thread.sleep(5000);
    //} catch (InterruptedException e) {
    //  e.printStackTrace();
    //}
    //
    //ViewInteraction floatingActionButton = onView(allOf(withId(R.id.fab_show_rating_dialog),
    //    childAtPosition(childAtPosition(withId(android.R.id.content), 0), 1), isDisplayed()));
    //floatingActionButton.perform(click());
    //
    //ViewInteraction appCompatEditText = onView(allOf(withId(R.id.restaurant_form_text),
    //    childAtPosition(childAtPosition(withId(android.R.id.content), 0), 2), isDisplayed()));
    //appCompatEditText.perform(click());
    //
    //ViewInteraction appCompatEditText2 = onView(allOf(withId(R.id.restaurant_form_text),
    //    childAtPosition(childAtPosition(withId(android.R.id.content), 0), 2), isDisplayed()));
    //appCompatEditText2.perform(replaceText("fabtastic"), closeSoftKeyboard());
    //
    //ViewInteraction appCompatButton4 = onView(
    //    allOf(withId(R.id.restaurant_form_button), withText("Submit"),
    //        childAtPosition(childAtPosition(withClassName(is("android.widget.LinearLayout")), 3),
    //            2), isDisplayed()));
    //appCompatButton4.perform(click());
    //
    //ViewInteraction textView2 = onView(allOf(withId(R.id.rating_item_text), withText("fabtastic"),
    //    childAtPosition(childAtPosition(withId(R.id.recycler_ratings), 0), 4), isDisplayed()));
    //textView2.check(matches(withText("fabtastic")));
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
