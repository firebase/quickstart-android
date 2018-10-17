package com.google.samples.quickstart.appindexing.kotlin

import android.content.Intent
import android.net.Uri
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.samples.quickstart.appindexing.java.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    var rule = ActivityTestRule(MainActivity::class.java, true, false /* launchActivity */)

    @Test
    fun testDeepLink() {
        // Create intent to MainActivity
        val link = "http://www.example.com/kotlin_articles/test"
        val linkIntent = Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(link))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        rule.launchActivity(linkIntent)

        onView(withText(link))
                .check(matches(isDisplayed()))
    }
}
