package com.google.firebase.quickstart.auth.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.quickstart.auth.databinding.FragmentPasskeysAuthBinding;

public class PasskeysAuthFragment extends BaseFragment {

    private static final String TAG = "PasskeysAuthFragment";

    private FirebaseAuth mAuth;

    private FragmentPasskeysAuthBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        mBinding = FragmentPasskeysAuthBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setProgressBar(mBinding.progressBar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentFirebaseUser) {
        if (currentFirebaseUser != null) {
            String uid = "Uid: " + currentFirebaseUser.getUid();
            mBinding.passkeyUid.setText(uid);
            String credentialId = "CredentialIds: ";

//            TODO(parijatbhatt): Uncomment after adding the EAP Auth SDK
//            for (PasskeyInfo passkeyInfo : currentFirebaseUser.getEnrolledPasskeys()) {
//                credentialId += passkeyInfo.getCredentialId() + "\n";
//            }
            mBinding.passkeyCredentialIds.setText(credentialId);
        } else {
            mBinding.passkeyUid.setText("");
            mBinding.passkeyCredentialIds.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
