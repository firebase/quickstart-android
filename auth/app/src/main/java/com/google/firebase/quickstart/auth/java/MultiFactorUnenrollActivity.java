package com.google.firebase.quickstart.auth.java;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.ActivityMultiFactorSignInBinding;

import java.util.ArrayList;
import java.util.List;

public class MultiFactorUnenrollActivity extends BaseActivity {

    private ActivityMultiFactorSignInBinding mBinding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMultiFactorSignInBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

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
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo));
        }
    }

    private View.OnClickListener generateFactorOnClickListener(PhoneMultiFactorInfo phoneMultiFactorInfo) {
        return new View.OnClickListener() {
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
                                            Toast.makeText(MultiFactorUnenrollActivity.this,
                                                    "Successfully unenrolled!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(MultiFactorUnenrollActivity.this,
                                                    "Unable to unenroll second factor. " + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
            }
        };
    }
}
