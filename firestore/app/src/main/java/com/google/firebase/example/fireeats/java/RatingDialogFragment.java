package com.google.firebase.example.fireeats.java;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.DialogRatingBinding;
import com.google.firebase.example.fireeats.java.model.Rating;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Dialog Fragment containing rating form.
 */
public class RatingDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "RatingDialog";

    private DialogRatingBinding mBinding;
    
    interface RatingListener {

        void onRating(Rating rating);

    }

    private RatingListener mRatingListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DialogRatingBinding.inflate(inflater, container, false);

        mBinding.restaurantFormButton.setOnClickListener(this);
        mBinding.restaurantFormCancel.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            mRatingListener = (RatingListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void onSubmitClicked(View view) {
        Rating rating = new Rating(
                FirebaseAuth.getInstance().getCurrentUser(),
                mBinding.restaurantFormRating.getRating(),
                mBinding.restaurantFormText.getText().toString());

        if (mRatingListener != null) {
            mRatingListener.onRating(rating);
        }

        dismiss();
    }

    private void onCancelClicked(View view) {
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurantFormButton:
                onSubmitClicked(v);
                break;
            case R.id.restaurantFormCancel:
                onCancelClicked(v);
                break;
        }
    }

}
