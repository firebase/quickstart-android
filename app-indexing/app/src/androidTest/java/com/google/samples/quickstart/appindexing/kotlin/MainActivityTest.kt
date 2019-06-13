package com.google.samples.quickstart.appindexing.kotlin

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
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
