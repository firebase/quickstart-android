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
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity demonstrates the Android SDK for Callable Functions.
 *
 * For more information, see the documentation for Cloud Functions for Firebase:
 * https://firebase.google.com/docs/functions/
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;

    // Add number views
    private EditText mFirstNumberField;
    private EditText mSecondNumberField;
    private EditText mAddResultField;
    private Button mCalculateButton;

    // Add message views
    private EditText mMessageInputField;
    private EditText mMessageOutputField;
    private Button mAddMessageButton;
    private Button mSignInButton;

    // [START define_functions_instance]
    private FirebaseFunctions mFunctions;
    // [END define_functions_instance]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirstNumberField = findViewById(R.id.fieldFirstNumber);
        mSecondNumberField = findViewById(R.id.fieldSecondNumber);
        mAddResultField = findViewById(R.id.fieldAddResult);
        mCalculateButton = findViewById(R.id.buttonCalculate);
        mCalculateButton.setOnClickListener(this);

        mMessageInputField = findViewById(R.id.fieldMessageInput);
        mMessageOutputField = findViewById(R.id.fieldMessageOutput);
        mAddMessageButton = findViewById(R.id.buttonAddMessage);
        mSignInButton = findViewById(R.id.buttonSignIn);
        mAddMessageButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);

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
            firstNumber = Integer.parseInt(mFirstNumberField.getText().toString());
            secondNumber = Integer.parseInt(mSecondNumberField.getText().toString());
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
                        mAddResultField.setText(String.valueOf(result));
                        // [END_EXCLUDE]
                    }
                });
        // [END call_add_numbers]
    }

    private void onAddMessageClicked() {
        String inputMessage = mMessageInputField.getText().toString();

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
                        mMessageOutputField.setText(result);
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
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                showSnackbar("Signed in.");
            } else {
                showSnackbar("Error signing in.");

                IdpResponse response = IdpResponse.fromResultIntent(data);
                Log.w(TAG, "signIn", response.getError());
            }
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
