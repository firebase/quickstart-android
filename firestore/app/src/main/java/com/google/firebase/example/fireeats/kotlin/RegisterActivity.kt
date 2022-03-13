package com.example.megworld.Activity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.megworld.Firestore.firestoreClass
import com.example.megworld.R
import com.example.megworld.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setupActionBar()

        tv_login.setOnClickListener {
            onBackPressed()
        }
        btn_register.setOnClickListener{
            registerUser()
        }
    }
    private fun setupActionBar(){
        setSupportActionBar(toolbar_register_activity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_register_activity.setNavigationOnClickListener {onBackPressed()}
    }
/**   function's to  validate each field **/
    private fun validateRegisterDetails(): Boolean{

        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim{it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_your_first_name), true)
                false
        }
            TextUtils.isEmpty(et_last_name.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_your_last_name),true
                )
                false
            }
            TextUtils.isEmpty(et_email.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_your_email_id),true
                )
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_your_password),true
                )
                false
            }
            TextUtils.isEmpty(et_confirm_password.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_your_confirm_password),true
                )
                false
            }
            et_password.text.toString().trim{it <= ' '} != et_confirm_password.text.toString().trim{it <= ' '} -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),true
                )
                false
            }
            !cb_terms_and_conditions.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition),true)
                false
            }
            else -> {
                //showErrorSnackBar("your Details are valid", false)
                true}
        }

    }

private fun registerUser(){
    if (validateRegisterDetails()){
        showProgressDialog(resources.getString(R.string.Please_wait))
        val email: String = et_email.text.toString().trim{it <=' '}
        val password:String = et_password.text.toString().trim{it <=' '}
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val user = User(
                            firebaseUser.uid,
                            et_first_name.text.toString().trim{it <=' '},
                            et_last_name.text.toString().trim{it <=' '},
                            et_email.text.toString().trim{it <=' '}
                        )
                        firestoreClass().registerUser(this@RegisterActivity, user)

//                        FirebaseAuth.getInstance().signOut()
//                        finish()
                    }else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                })
        }
    }
    fun  userRegistrationSuccess(){
        hideProgressDialog()
        Toast.makeText(this@RegisterActivity,
        resources.getText(R.string.registration_success),
        Toast.LENGTH_SHORT).show()
    }
}
