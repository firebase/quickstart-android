package com.google.firebase.quickstart.auth.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.quickstart.auth.databinding.FragmentMultiFactorSignInBinding;

import java.util.ArrayList;
import java.util.List;

public class MultiFactorUnenrollFragment extends BaseFragment {

    private FragmentMultiFactorSignInBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMultiFactorSignInBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.smsCode.setVisibility(View.GONE);
        mBinding.finishMfaSignIn.setVisibility(View.GONE);

        List<Button> phoneFactorButtonList = new ArrayList<>();
        phoneFactorButtonList.add(mBinding.phoneFactor1);
        phoneFactorButtonList.add(mBinding.phoneFactor2);
        phoneFactorButtonList.add(mBinding.phoneFactor3);
        phoneFactorButtonList.add(mBinding.phoneFactor4);
        phoneFactorButtonList.add(mBinding.phoneFactor5);

        for (Button button : phoneFactorButtonList) {
            button.setVisibility(View.GONE);
        }

        List<MultiFactorInfo> multiFactorInfoList =
                FirebaseAuth.getInstance().getCurrentUser().getMultiFactor().getEnrolledFactors();

        for (int i = 0; i < multiFactorInfoList.size(); ++i) {
            PhoneMultiFactorInfo phoneMultiFactorInfo = (PhoneMultiFactorInfo) multiFactorInfoList.get(i);
            Button button = phoneFactorButtonList.get(i);
            button.setVisibility(View.VISIBLE);
            button.setText(phoneMultiFactorInfo.getPhoneNumber());
            button.setClickable(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getMultiFactor()
                            .unenroll(phoneMultiFactorInfo)
                            .addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(),
                                                        "Successfully unenrolled!", Toast.LENGTH_SHORT).show();
                                                NavHostFragment.findNavController(MultiFactorUnenrollFragment.this)
                                                        .popBackStack();
                                            } else {
                                                Toast.makeText(getContext(),
                                                        "Unable to unenroll second factor. " + task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
