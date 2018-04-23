package com.google.firebase.example.fireeats;

import android.content.Intent;
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest @RunWith(AndroidJUnit4.class) public class MainActivityTest2 {
  @Before
  public void setup() throws ExecutionException, InterruptedException {
    Task t = FirebaseAuth.getInstance().signInWithEmailAndPassword("test@gmail.com", "123456");
    Tasks.await(t);
  }

  @Rule public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class, false, false);

  @Test public void mainActivityTest2() {
    mActivityTestRule.launchActivity(new Intent());
    ViewInteraction appCompatButton = onView(allOf(withId(R.id.button_next), withText("Next"),
        childAtPosition(childAtPosition(withClassName(is("android.widget.ScrollView")), 0), 1)));
    appCompatButton.perform(scrollTo(), click());

    ViewInteraction appCompatButton2 = onView(allOf(withId(R.id.button_next), withText("Next"),
        childAtPosition(childAtPosition(withClassName(is("android.widget.ScrollView")), 0), 1)));
    appCompatButton2.perform(scrollTo(), click());
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
