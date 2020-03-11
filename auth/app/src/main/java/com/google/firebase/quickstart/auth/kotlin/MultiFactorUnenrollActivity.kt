package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.java.BaseActivity
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.finishMfaSignIn
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor1
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor2
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor3
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor4
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor5
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.smsCode

class MultiFactorUnenrollActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_factor_sign_in)

        smsCode.visibility = View.GONE
        finishMfaSignIn.visibility = View.GONE

        // Users are currently limited to having 5 second factors
        val phoneFactorButtonList = listOf(
                phoneFactor1, phoneFactor2, phoneFactor3, phoneFactor4, phoneFactor5)
        for (button in phoneFactorButtonList) {
            button.visibility = View.GONE
        }

        val multiFactorInfoList = FirebaseAuth.getInstance().currentUser!!.multiFactor.enrolledFactors
        for (i in multiFactorInfoList.indices) {
            val phoneMultiFactorInfo = multiFactorInfoList[i] as PhoneMultiFactorInfo
            val button = phoneFactorButtonList[i]
            button.visibility = View.VISIBLE
            button.text = phoneMultiFactorInfo.phoneNumber
            button.isClickable = true
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo))
        }
    }

    private fun generateFactorOnClickListener(phoneMultiFactorInfo: PhoneMultiFactorInfo): View.OnClickListener {
        return View.OnClickListener {
            FirebaseAuth.getInstance()
                    .currentUser
                    ?.multiFactor
                    ?.unenroll(phoneMultiFactorInfo)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MultiFactorUnenrollActivity,
                                    "Successfully unenrolled!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@MultiFactorUnenrollActivity,
                                    "Unable to unenroll second factor. " + task.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }
}
