package com.google.firebase.samples.apps.mlkit.languageid.java;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.languageid.IdentifiedLanguage;
import com.google.firebase.samples.apps.mlkit.languageid.R;
import com.google.firebase.samples.apps.mlkit.languageid.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonIdLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = binding.inputText.getText().toString();
                if (input.isEmpty()) {
                    return;
                }
                binding.inputText.getText().clear();
                identifyLanguage(input);
            }
        });

        binding.buttonIdAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = binding.inputText.getText().toString();
                if (input.isEmpty()) {
                    return;
                }
                binding.inputText.getText().clear();
                identifyPossibleLanguages(input);
            }
        });
    }

    private void identifyPossibleLanguages(final String inputText) {
        FirebaseLanguageIdentification languageIdentification =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentification
                .identifyPossibleLanguages(inputText)
                .addOnSuccessListener(
                        this,
                        new OnSuccessListener<List<IdentifiedLanguage>>() {
                            @Override
                            public void onSuccess(List<IdentifiedLanguage> identifiedLanguages) {
                                List<String> detectedLanguages =
                                        new ArrayList<>(identifiedLanguages.size());
                                for (IdentifiedLanguage language : identifiedLanguages) {
                                    detectedLanguages.add(
                                            String.format(
                                                    Locale.US,
                                                    "%s (%3f)",
                                                    language.getLanguageCode(),
                                                    language.getConfidence())
                                    );
                                }
                                binding.outputText.append(
                                        String.format(
                                                Locale.US,
                                                "\n%s - [%s]",
                                                inputText,
                                                TextUtils.join(", ", detectedLanguages)));
                            }
                        })
                .addOnFailureListener(
                        this,
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Language identification error", e);
                                Toast.makeText(
                                        MainActivity.this, R.string.language_id_error,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private void identifyLanguage(final String inputText) {
        FirebaseLanguageIdentification languageIdentification =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentification
                .identifyLanguage(inputText)
                .addOnSuccessListener(
                        this,
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                binding.outputText.append(
                                        String.format(
                                                Locale.US,
                                                "\n%s - %s",
                                                inputText,
                                                s));
                            }
                        })
                .addOnFailureListener(
                        this,
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Language identification error", e);
                                Toast.makeText(
                                        MainActivity.this, R.string.language_id_error,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }
}
