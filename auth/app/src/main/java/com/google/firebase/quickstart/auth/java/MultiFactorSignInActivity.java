package com.google.firebase.quickstart.auth.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.ActivityMultiFactorSignInBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;

/**
 * Activity that handles MFA sign-in
 */
public class MultiFactorSignInActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String KEY_VERIFICATION_ID = "key_verification_id";
    public static final String EXTRA_MFA_RESOLVER = "EXTRA_MFA_RESOLVER";

    private ActivityMultiFactorSignInBinding mBinding;

    private MultiFactorResolver mMultiFactorResolver;
    private PhoneAuthCredential mPhoneAuthCredential;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMultiFactorSignInBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        List<Button> phoneFactorButtonList = new ArrayList<>();
        phoneFactorButtonList.add(mBinding.phoneFactor1);
        phoneFactorButtonList.add(mBinding.phoneFactor2);
        phoneFactorButtonList.add(mBinding.phoneFactor3);
        phoneFactorButtonList.add(mBinding.phoneFactor4);
        phoneFactorButtonList.add(mBinding.phoneFactor5);

        for (Button button : phoneFactorButtonList) {
            button.setVisibility(View.GONE);
        }

        mBinding.finishMfaSignIn.setOnClickListener(this);

        mMultiFactorResolver = retrieveResolverFromIntent(getIntent());
        List<MultiFactorInfo> multiFactorInfoList = mMultiFactorResolver.getHints();

        for (int i = 0; i < multiFactorInfoList.size(); ++i) {
            PhoneMultiFactorInfo phoneMultiFactorInfo = (PhoneMultiFactorInfo) multiFactorInfoList.get(i);
            Button button = phoneFactorButtonList.get(i);
            button.setVisibility(View.VISIBLE);
            button.setText(phoneMultiFactorInfo.getPhoneNumber());
            button.setClickable(true);
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_VERIFICATION_ID, mVerificationId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationId = savedInstanceState.getString(KEY_VERIFICATION_ID);
    }

    private View.OnClickListener generateFactorOnClickListener(PhoneMultiFactorInfo phoneMultiFactorInfo) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthProvider.verifyPhoneNumber(
                        PhoneAuthOptions.newBuilder()
                                .setActivity(MultiFactorSignInActivity.this)
                                .setMultiFactorSession(mMultiFactorResolver.getSession())
                                .setMultiFactorHint(phoneMultiFactorInfo)
                                .setCallbacks(generateCallbacks())
                                // A timeout of 0 disables SMS-auto-retrieval.
                                .setTimeout(0L, TimeUnit.SECONDS)
                                .build());
            }
        };
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks generateCallbacks() {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                MultiFactorSignInActivity.this.mPhoneAuthCredential = phoneAuthCredential;
                mBinding.finishMfaSignIn.performClick();
                Toast.makeText(
                        MultiFactorSignInActivity.this, "Verification complete!", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                MultiFactorSignInActivity.this.mVerificationId = verificationId;
                mBinding.finishMfaSignIn.setClickable(true);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(
                        MultiFactorSignInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        };
    }

    private MultiFactorResolver retrieveResolverFromIntent(Intent intent) {
        return intent.getParcelableExtra(EXTRA_MFA_RESOLVER);
    }

    private void onClickFinishSignIn() {
        if (mPhoneAuthCredential == null) {
            if (isEmpty(mBinding.smsCode.getText().toString())) {
                Toast.makeText(
                        MultiFactorSignInActivity.this, "You need to enter an SMS code.", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            mPhoneAuthCredential =
                    PhoneAuthProvider.getCredential(
                            mVerificationId, mBinding.smsCode.getText().toString());
        }
        mMultiFactorResolver
                .resolveSignIn(PhoneMultiFactorGenerator.getAssertion(mPhoneAuthCredential))
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        MultiFactorSignInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finishMfaSignIn) {
            onClickFinishSignIn();
        }
    }
}
