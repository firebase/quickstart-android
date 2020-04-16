/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.firebase.samples.apps.mlkit.translate.java;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.samples.apps.mlkit.translate.databinding.TranslateFragmentBinding;
import com.google.firebase.samples.apps.mlkit.translate.R;
import com.google.firebase.samples.apps.mlkit.translate.java.TranslateViewModel.Language;
import com.google.firebase.samples.apps.mlkit.translate.java.TranslateViewModel.ResultOrError;

import java.util.List;

public class TranslateFragment extends Fragment {

    private TranslateFragmentBinding binding;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = TranslateFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TranslateViewModel viewModel = new ViewModelProvider(this).get(TranslateViewModel.class);

        // Get available language list and set up source and target language spinners
        // with default selections.
        final ArrayAdapter<Language> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, viewModel.getAvailableLanguages());
        binding.sourceLangSelector.setAdapter(adapter);
        binding.targetLangSelector.setAdapter(adapter);
        binding.sourceLangSelector.setSelection(adapter.getPosition(new Language("en")));
        binding.targetLangSelector.setSelection(adapter.getPosition(new Language("es")));
        binding.sourceLangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setProgressText(binding.targetText);
                viewModel.sourceLang.setValue(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.targetText.setText("");
            }
        });
        binding.targetLangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setProgressText(binding.targetText);
                viewModel.targetLang.setValue(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.targetText.setText("");
            }
        });

        binding.buttonSwitchLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressText(binding.targetText);
                int sourceLangPosition = binding.sourceLangSelector.getSelectedItemPosition();
                binding.sourceLangSelector.setSelection(binding.targetLangSelector.getSelectedItemPosition());
                binding.targetLangSelector.setSelection(sourceLangPosition);
            }
        });

        // Set up toggle buttons to delete or download remote models locally.
        binding.buttonSyncSource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Language language = adapter.getItem(binding.sourceLangSelector.getSelectedItemPosition());
                if (isChecked) {
                    viewModel.downloadLanguage(language);
                } else {
                    viewModel.deleteLanguage(language);
                }
            }
        });
        binding.buttonSyncTarget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Language language = adapter.getItem(binding.targetLangSelector.getSelectedItemPosition());
                if (isChecked) {
                    viewModel.downloadLanguage(language);
                } else {
                    viewModel.deleteLanguage(language);
                }
            }
        });

        // Translate input text as it is typed
        binding.sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setProgressText(binding.targetText);
                viewModel.sourceText.postValue(s.toString());
            }
        });
        viewModel.translatedText.observe(getViewLifecycleOwner(), new Observer<ResultOrError>() {
            @Override
            public void onChanged(TranslateViewModel.ResultOrError resultOrError) {
                if (resultOrError.error != null) {
                    binding.sourceText.setError(resultOrError.error.getLocalizedMessage());
                } else {
                    binding.targetText.setText(resultOrError.result);
                }
            }
        });

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> firebaseTranslateRemoteModels) {
                String output = getContext().getString(R.string.downloaded_models_label,
                        firebaseTranslateRemoteModels);
                binding.downloadedModels.setText(output);
                binding.buttonSyncSource.setChecked(firebaseTranslateRemoteModels.contains(
                        adapter.getItem(binding.sourceLangSelector.getSelectedItemPosition()).getCode()));
                binding.buttonSyncTarget.setChecked(firebaseTranslateRemoteModels.contains(
                        adapter.getItem(binding.targetLangSelector.getSelectedItemPosition()).getCode()));
            }
        });
    }

    private void setProgressText(TextView tv) {
        tv.setText(getContext().getString(R.string.translate_progress));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
