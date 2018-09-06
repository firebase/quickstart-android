package com.google.firebase.fiamquickstart;

import android.content.Intent;

import com.firebase.example.internal.BaseEntryChoiceActivity;
import com.firebase.example.internal.Choice;

import java.util.Arrays;
import java.util.List;

public class EntryChoiceActivity extends BaseEntryChoiceActivity {

    @Override
    protected List<Choice> getChoices() {
        return Arrays.asList(
                new Choice(
                        "Java",
                        "Run the Firebase In App Messaging quickstart written in Java.",
                        new Intent(this, MainActivity.class)),
                new Choice(
                        "Kotlin",
                        "Run the Firebase In App Messaging quickstart written in Kotlin.",
                        new Intent(this, KotlinMainActivity.class))
        );
    }

}
