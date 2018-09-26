package com.google.firebase.samples.apps.mlkit.smartreply;

import android.arch.lifecycle.MutableLiveData;

import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;

import java.util.List;

public class SmartReplier {
    private static SmartReplier instance;

    private SmartReplier() {

    }

    public static SmartReplier getInstance() {
        if (instance == null) {
            instance = new SmartReplier();
        }
        return instance;
    }

    public void generateReplies(String inputString,
                                MutableLiveData<List<SmartReplySuggestion>> out) {
        FirebaseSmartReply replyGenerator = FirebaseNaturalLanguage.getInstance().getSmartReply();
        replyGenerator.suggest(inputString)
                .addOnSuccessListener(smartReplySuggestionResult ->
                        out.setValue(smartReplySuggestionResult.getSuggestions())
                )
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
