package com.google.samples.quickstart.admobexample

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.samples.quickstart.admobexample.java.MainActivity
import com.google.samples.quickstart.admobexample.kotlin.KotlinMainActivity

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase In App Messaging quickstart written in Java.",
                        Intent(this, MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Firebase In App Messaging quickstart written in Kotlin.",
                        Intent(this, KotlinMainActivity::class.java))
        )
    }

}
