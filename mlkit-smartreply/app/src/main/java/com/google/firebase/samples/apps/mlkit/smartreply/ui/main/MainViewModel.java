package com.google.firebase.samples.apps.mlkit.smartreply.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;

import java.util.List;

class MainViewModel extends ViewModel {
    private MutableLiveData<List<SmartReplySuggestion>> suggestions;

    MutableLiveData<List<SmartReplySuggestion>> getSuggestionsLiveData() {
        if (suggestions == null) {
            suggestions = new MutableLiveData<>();
        }
        return suggestions;
    }
}
