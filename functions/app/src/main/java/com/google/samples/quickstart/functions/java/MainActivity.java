/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.quickstart.functions.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.samples.quickstart.functions.R;
import com.google.samples.quickstart.functions.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity demonstrates the Android SDK for Callable Functions.
 * <p>
 * For more information, see the documentation for Cloud Functions for Firebase:
 * https://firebase.google.com/docs/functions/
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    // [START define_functions_instance]
    private FirebaseFunctions mFunctions;
    // [END define_functions_instance]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCalculate.setOnClickListener(this);
        binding.buttonAddMessage.setOnClickListener(this);
        binding.buttonSignIn.setOnClickListener(this);

        // [START initialize_functions_instance]
        mFunctions = FirebaseFunctions.getInstance();
        // [END initialize_functions_instance]
    }

    // [START function_add_numbers]
    private Task<Integer> addNumbers(int a, int b) {
        // Create the arguments to the callable function, which are two integers
        Map<String, Object> data = new HashMap<>();
        data.put("firstNumber", a);
        data.put("secondNumber", b);

        // Call the function and extract the operation from the result
        return mFunctions
                .getHttpsCallable("addNumbers")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Integer>() {
                    @Override
                    public Integer then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (Integer) result.get("operationResult");
                    }
                });
    }
    // [END function_add_numbers]

    // [START function_add_message]
    private Task<String> addMessage(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("push", true);

        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
    // [END function_add_message]

    private void onCalculateClicked() {
        int firstNumber;
        int secondNumber;

        hideKeyboard();

        try {
            firstNumber = Integer.parseInt(binding.fieldFirstNumber.getText().toString());
            secondNumber = Integer.parseInt(binding.fieldSecondNumber.getText().toString());
        } catch (NumberFormatException e) {
            showSnackbar("Please enter two numbers.");
            return;
        }

        // [START call_add_numbers]
        addNumbers(firstNumber, secondNumber)
                .addOnCompleteListener(new OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(@NonNull Task<Integer> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;

                                // Function error code, will be INTERNAL if the failure
                                // was not handled properly in the function call.
                                FirebaseFunctionsException.Code code = ffe.getCode();

                                // Arbitrary error details passed back from the function,
                                // usually a Map<String, Object>.
                                Object details = ffe.getDetails();
                            }

                            // [START_EXCLUDE]
                            Log.w(TAG, "addNumbers:onFailure", e);
                            showSnackbar("An error occurred.");
                            return;
                            // [END_EXCLUDE]
                        }

                        // [START_EXCLUDE]
                        Integer result = task.getResult();
                        binding.fieldAddResult.setText(String.valueOf(result));
                        // [END_EXCLUDE]
                    }
                });
        // [END call_add_numbers]
    }

    private void onAddMessageClicked() {
        String inputMessage = binding.fieldMessageInput.getText().toString();

        if (TextUtils.isEmpty(inputMessage)) {
            showSnackbar("Please enter a message.");
            return;
        }

        // [START call_add_message]
        addMessage(inputMessage)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                            }

                            // [START_EXCLUDE]
                            Log.w(TAG, "addMessage:onFailure", e);
                            showSnackbar("An error occurred.");
                            return;
                            // [END_EXCLUDE]
                        }

                        // [START_EXCLUDE]
                        String result = task.getResult();
                        binding.fieldMessageOutput.setText(result);
                        // [END_EXCLUDE]
                    }
                });
        // [END call_add_message]
    }

    private void onSignInClicked() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            showSnackbar("Signed in.");
            return;
        }

        signIn();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void signIn() {
        ActivityResultLauncher<Intent> signInLauncher =
                registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            showSnackbar("Signed in.");
        } else {
            showSnackbar("Error signing in.");

            IdpResponse response = result.getIdpResponse();
            Log.w(TAG, "signIn", response.getError());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCalculate:
                onCalculateClicked();
                break;
            case R.id.buttonAddMessage:
                onAddMessageClicked();
                break;
            case R.id.buttonSignIn:
                onSignInClicked();
                break;
        }
    }
}
