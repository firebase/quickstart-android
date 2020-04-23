package com.google.samples.quickstart.functions.kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.samples.quickstart.functions.R
import com.google.samples.quickstart.functions.databinding.ActivityMainBinding

/**
 * This activity demonstrates the Android SDK for Callable Functions.
 *
 * For more information, see the documentation for Cloud Functions for Firebase:
 * https://firebase.google.com/docs/functions/
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    // [START define_functions_instance]
    private lateinit var functions: FirebaseFunctions
    // [END define_functions_instance]

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonCalculate.setOnClickListener(this@MainActivity)
            buttonAddMessage.setOnClickListener(this@MainActivity)
            buttonSignIn.setOnClickListener(this@MainActivity)
        }

        // [START initialize_functions_instance]
        functions = Firebase.functions
        // [END initialize_functions_instance]
    }

    // [START function_add_numbers]
    private fun addNumbers(a: Int, b: Int): Task<Int> {
        // Create the arguments to the callable function, which are two integers
        val data = hashMapOf(
            "firstNumber" to a,
            "secondNumber" to b
        )

        // Call the function and extract the operation from the result
        return functions
                .getHttpsCallable("addNumbers")
                .call(data)
                .continueWith { task ->
                    // This continuation runs on either success or failure, but if the task
                    // has failed then task.result will throw an Exception which will be
                    // propagated down.
                    val result = task.result?.data as Map<String, Any>
                    result["operationResult"] as Int
                }
    }
    // [END function_add_numbers]

    // [START function_add_message]
    private fun addMessage(text: String): Task<String> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "text" to text,
            "push" to true
        )

        return functions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith { task ->
                    // This continuation runs on either success or failure, but if the task
                    // has failed then result will throw an Exception which will be
                    // propagated down.
                    val result = task.result?.data as String
                    result
                }
    }
    // [END function_add_message]

    private fun onCalculateClicked() {
        val firstNumber: Int
        val secondNumber: Int

        hideKeyboard()

        try {
            firstNumber = Integer.parseInt(binding.fieldFirstNumber.text.toString())
            secondNumber = Integer.parseInt(binding.fieldSecondNumber.text.toString())
        } catch (e: NumberFormatException) {
            showSnackbar("Please enter two numbers.")
            return
        }

        // [START call_add_numbers]
        addNumbers(firstNumber, secondNumber)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {

                            // Function error code, will be INTERNAL if the failure
                            // was not handled properly in the function call.
                            val code = e.code

                            // Arbitrary error details passed back from the function,
                            // usually a Map<String, Any>.
                            val details = e.details
                        }

                        // [START_EXCLUDE]
                        Log.w(TAG, "addNumbers:onFailure", e)
                        showSnackbar("An error occurred.")
                        return@addOnCompleteListener
                        // [END_EXCLUDE]
                    }

                    // [START_EXCLUDE]
                    val result = task.result
                    binding.fieldAddResult.setText(result.toString())
                    // [END_EXCLUDE]
                }
        // [END call_add_numbers]
    }

    private fun onAddMessageClicked() {
        val inputMessage = binding.fieldMessageInput.text.toString()

        if (TextUtils.isEmpty(inputMessage)) {
            showSnackbar("Please enter a message.")
            return
        }

        // [START call_add_message]
        addMessage(inputMessage)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }

                        // [START_EXCLUDE]
                        Log.w(TAG, "addMessage:onFailure", e)
                        showSnackbar("An error occurred.")
                        return@OnCompleteListener
                        // [END_EXCLUDE]
                    }

                    // [START_EXCLUDE]
                    val result = task.result
                    binding.fieldMessageOutput.setText(result)
                    // [END_EXCLUDE]
                })
        // [END call_add_message]
    }

    private fun onSignInClicked() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            showSnackbar("Signed in.")
            return
        }

        signIn()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun signIn() {
        val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build()

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                showSnackbar("Signed in.")
            } else {
                showSnackbar("Error signing in.")

                val response = IdpResponse.fromResultIntent(data)
                Log.w(TAG, "signIn", response?.error)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonCalculate -> onCalculateClicked()
            R.id.buttonAddMessage -> onAddMessageClicked()
            R.id.buttonSignIn -> onSignInClicked()
        }
    }

    companion object {

        private const val TAG = "MainActivity"

        private const val RC_SIGN_IN = 9001
    }
}
