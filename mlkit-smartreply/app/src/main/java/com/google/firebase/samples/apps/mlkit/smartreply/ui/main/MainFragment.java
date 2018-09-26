package com.google.firebase.samples.apps.mlkit.smartreply.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.samples.apps.mlkit.smartreply.R;
import com.google.firebase.samples.apps.mlkit.smartreply.SmartReplier;

public class MainFragment extends Fragment {
    private MainViewModel mViewModel;
    private TextView mRepliesText;
    private TextView mInputText;
    private Button mButton;
    private SmartReplier mSmartReplier = SmartReplier.getInstance();

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRepliesText = view.findViewById(R.id.repliesText);
        mButton = view.findViewById(R.id.button);
        mInputText = view.findViewById(R.id.inputText);
        mButton.setOnClickListener(v -> {
           if (mInputText.getText().length() == 0) {
               mRepliesText.setText(R.string.empty_input_message);
               return;
           }
           String input = mInputText.getText().toString();
           mSmartReplier.generateReplies(input, mViewModel.getSuggestionsLiveData());
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.getSuggestionsLiveData().observe(this, suggestions -> {
            mRepliesText.setText(null);
            for (SmartReplySuggestion suggestion : suggestions) {
                mRepliesText.append("\nConfidence: " + suggestion.getConfidence());
                mRepliesText.append("\nText: " + suggestion.getText());
            }

        });
        mSmartReplier = SmartReplier.getInstance();
    }

}
