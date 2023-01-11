package com.google.firebase.quickstart.auth.java;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.MultiFactorSession;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;
import com.google.firebase.auth.PhoneMultiFactorGenerator;
import com.google.firebase.quickstart.auth.databinding.FragmentPhoneAuthBinding;

import java.util.concurrent.TimeUnit;

/**
 * Fragment that allows the user to enroll second factors.
 */
public class MultiFactorEnrollFragment extends BaseFragment {

    private static final String TAG = "MfaEnrollFragment";

    private FragmentPhoneAuthBinding mBinding;

    private String mCodeVerificationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPhoneAuthBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.titleText.setText("SMS as a Second Factor");
        mBinding.status.setVisibility(View.GONE);
        mBinding.detail.setVisibility(View.GONE);
        mBinding.buttonStartVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVerifyPhoneNumber();
            }
        });
        mBinding.buttonVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSignInWithPhoneNumber();
            }
        });
    }

    private void onClickVerifyPhoneNumber() {
        String phoneNumber = mBinding.fieldPhoneNumber.getText().toString();

        OnVerificationStateChangedCallbacks callbacks =
                new OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Instant-validation has been disabled (see requireSmsValidation below).
                        // Auto-retrieval has also been disabled (timeout is set to 0).
                        // This should never be triggered.
                        throw new RuntimeException(
                                "onVerificationCompleted() triggered with instant-validation and auto-retrieval disabled.");
                    }

                    @Override
                    public void onCodeSent(
                            final String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent:" + verificationId);
                        Toast.makeText( getContext(), "SMS code has been sent", Toast.LENGTH_SHORT)
                                .show();

                        mCodeVerificationId = verificationId;
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.w(TAG, "onVerificationFailed ", e);
                        Toast.makeText(getContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                };

        FirebaseAuth.getInstance()
                .getCurrentUser()
                .getMultiFactor()
                .getSession()
                .addOnCompleteListener(
                        new OnCompleteListener<MultiFactorSession>() {
                            @Override
                            public void onComplete(@NonNull Task<MultiFactorSession> task) {
                                if (task.isSuccessful()) {
                                    PhoneAuthOptions phoneAuthOptions =
                                            PhoneAuthOptions.newBuilder()
                                                    .setActivity(requireActivity())
                                                    .setPhoneNumber(phoneNumber)
                                                    // A timeout of 0 disables SMS-auto-retrieval.
                                                    .setTimeout(0L, TimeUnit.SECONDS)
                                                    .setMultiFactorSession(task.getResult())
                                                    .setCallbacks(callbacks)
                                                    // Disable instant-validation.
                                                    .requireSmsValidation(true)
                                                    .build();

                                    PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
                                } else {
                                    Toast.makeText(getContext(),
                                            "Failed to get session: " + task.getException(), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
    }

    private void onClickSignInWithPhoneNumber() {
        String smsCode = mBinding.fieldVerificationCode.getText().toString();
        if (TextUtils.isEmpty(smsCode)) {
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mCodeVerificationId, smsCode);
        enrollWithPhoneAuthCredential(credential);
    }

    private void enrollWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance()
                .getCurrentUser()
                .getMultiFactor()
                .enroll(PhoneMultiFactorGenerator.getAssertion(credential), /* displayName= */ null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "MFA enrollment was successful",
                                Toast.LENGTH_LONG)
                                .show();

                        NavHostFragment.findNavController(MultiFactorEnrollFragment.this)
                                .popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MFA failure", e);
                        Toast.makeText(getContext(),
                                "MFA enrollment was unsuccessful. " + e,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
