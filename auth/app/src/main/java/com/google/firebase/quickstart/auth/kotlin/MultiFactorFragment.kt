package com.google.firebase.quickstart.auth.kotlin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.FragmentMultiFactorBinding

class MultiFactorFragment : BaseFragment() {
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentMultiFactorBinding? = null
    private val binding: FragmentMultiFactorBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMultiFactorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBar)

        arguments?.let { args ->
            if (args.getBoolean(RESULT_NEEDS_MFA_SIGN_IN)) {
                findNavController().navigate(R.id.action_mfa_to_mfasignin, args)
            }
        }

        // Buttons
        binding.emailSignInButton.setOnClickListener {
            findNavController().navigate(R.id.action_mfa_to_emailpassword)
        }
        binding.signOutButton.setOnClickListener { signOut() }
        binding.verifyEmailButton.setOnClickListener { sendEmailVerification() }
        binding.enrollMfa.setOnClickListener {
            findNavController().navigate(R.id.action_mfa_to_enroll)
        }
        binding.unenrollMfa.setOnClickListener {
            findNavController().navigate(R.id.action_mfa_to_unenroll)
        }
        binding.reloadButton.setOnClickListener { reload() }

        // Initialize Firebase Auth
        auth = Firebase.auth

        showDisclaimer()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun sendEmailVerification() { // Disable button
        binding.verifyEmailButton.isEnabled = false
        // Send verification email
        val user = auth.currentUser!!
        user.sendEmailVerification()
                .addOnCompleteListener(requireActivity()) { task ->
                    // Re-enable button
                    binding.verifyEmailButton.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(context,
                                "Verification email sent to " + user.email,
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(context,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun reload() {
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(auth.currentUser)
                Toast.makeText(context,
                        "Reload successful!",
                        Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "reload", task.exception)
                Toast.makeText(context,
                        "Failed to reload user.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.status.text = getString(R.string.emailpassword_status_fmt,
                    user.email, user.isEmailVerified)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)
            val secondFactors = user.multiFactor.enrolledFactors
            if (secondFactors.isEmpty()) {
                binding.unenrollMfa.visibility = View.GONE
            } else {
                binding.unenrollMfa.visibility = View.VISIBLE
                val sb = StringBuilder("Second Factors: ")
                val delimiter = ", "
                for (x in secondFactors) {
                    sb.append((x as PhoneMultiFactorInfo).phoneNumber + delimiter)
                }
                sb.setLength(sb.length - delimiter.length)
                binding.mfaInfo.text = sb.toString()
            }
            binding.emailSignInButton.visibility = View.GONE
            binding.signedInButtons.visibility = View.VISIBLE
            val reloadVisibility = if (secondFactors.isEmpty()) View.VISIBLE else View.GONE
            binding.reloadButton.visibility = reloadVisibility
            binding.verifyEmailButton.isGone = user.isEmailVerified
            binding.enrollMfa.isGone = !user.isEmailVerified
        } else {
            binding.status.setText(R.string.multi_factor_signed_out)
            binding.detail.text = null
            binding.mfaInfo.text = null
            binding.emailSignInButton.visibility = View.VISIBLE
            binding.signedInButtons.visibility = View.GONE
        }
    }

    private fun showDisclaimer() {
        AlertDialog.Builder(requireContext())
                .setTitle("Warning")
                .setMessage("Multi-factor authentication with SMS is currently only available for " +
                        "Google Cloud Identity Platform projects. For more information see: " +
                        "https://cloud.google.com/identity-platform/docs/android/mfa")
                .setPositiveButton("OK", null)
                .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val RESULT_NEEDS_MFA_SIGN_IN = "RESULT_NEEDS_MFA"
        private const val TAG = "MultiFactor"
    }
}
