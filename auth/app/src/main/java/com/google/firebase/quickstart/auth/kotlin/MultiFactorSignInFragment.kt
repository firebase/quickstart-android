package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.quickstart.auth.databinding.FragmentMultiFactorSignInBinding
import java.util.concurrent.TimeUnit

/**
 * Fragment that handles MFA sign-in
 */
class MultiFactorSignInFragment : BaseFragment() {

    private var _binding: FragmentMultiFactorSignInBinding? = null
    private val binding: FragmentMultiFactorSignInBinding
        get() = _binding!!

    private lateinit var multiFactorResolver: MultiFactorResolver
    private var lastPhoneAuthCredential: PhoneAuthCredential? = null
    private var lastVerificationId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let { onViewStateRestored(it) }

        // Users are currently limited to having 5 second factors
        val phoneFactorButtonList = listOf(
                binding.phoneFactor1, binding.phoneFactor2, binding.phoneFactor3,
                binding.phoneFactor4, binding.phoneFactor5)
        for (button in phoneFactorButtonList) {
            button.visibility = View.GONE
        }

        binding.finishMfaSignIn.setOnClickListener { onClickFinishSignIn() }
        multiFactorResolver = getResolverFromArguments(requireArguments())

        val multiFactorInfoList = multiFactorResolver.hints
        for (i in multiFactorInfoList.indices) {
            val phoneMultiFactorInfo = multiFactorInfoList[i] as PhoneMultiFactorInfo
            val button = phoneFactorButtonList[i]
            button.visibility = View.VISIBLE
            button.text = phoneMultiFactorInfo.phoneNumber
            button.isClickable = true
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo))
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(KEY_VERIFICATION_ID, lastVerificationId)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { savedState ->
            lastVerificationId = savedState.getString(KEY_VERIFICATION_ID)
        }
    }

    private fun generateFactorOnClickListener(phoneMultiFactorInfo: PhoneMultiFactorInfo): View.OnClickListener {
        return View.OnClickListener {
            PhoneAuthProvider.verifyPhoneNumber(
                    PhoneAuthOptions.newBuilder()
                            .setActivity(requireActivity())
                            .setMultiFactorSession(multiFactorResolver.session)
                            .setMultiFactorHint(phoneMultiFactorInfo)
                            .setCallbacks(generateCallbacks()) // A timeout of 0 disables SMS-auto-retrieval.
                            .setTimeout(0L, TimeUnit.SECONDS)
                            .build())
        }
    }

    private fun generateCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                lastPhoneAuthCredential = phoneAuthCredential
                binding.finishMfaSignIn.performClick()
                Toast.makeText(context, "Verification complete!", Toast.LENGTH_SHORT)
                        .show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                lastVerificationId = verificationId
                binding.finishMfaSignIn.isClickable = true
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun getResolverFromArguments(arguments: Bundle): MultiFactorResolver {
        return arguments.getParcelable(EXTRA_MFA_RESOLVER)!!
    }

    private fun onClickFinishSignIn() {
        if (lastPhoneAuthCredential == null) {
            if (TextUtils.isEmpty(binding.smsCode.text.toString())) {
                Toast.makeText(context, "You need to enter an SMS code.", Toast.LENGTH_SHORT)
                        .show()
                return
            }
            lastPhoneAuthCredential = PhoneAuthProvider.getCredential(
                    lastVerificationId!!, binding.smsCode.text.toString())
        }
        multiFactorResolver
                .resolveSignIn(PhoneMultiFactorGenerator.getAssertion(lastPhoneAuthCredential!!))
                .addOnSuccessListener {
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: " + e.message, Toast.LENGTH_SHORT)
                            .show()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_VERIFICATION_ID = "key_verification_id"
        const val EXTRA_MFA_RESOLVER = "EXTRA_MFA_RESOLVER"
    }
}