package com.firebase.example.internal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseEntryChoiceActivity extends AppCompatActivity {

    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_choice);

        mRecycler = findViewById(R.id.choices_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(new ChoiceAdapter(this, getChoices()));
    }

    protected abstract List<Choice> getChoices();
}
