package com.google.firebase.quickstart.database.kotlin

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

open class BaseActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    fun setProgressBar(resId: Int) {
        progressBar = findViewById(resId)
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }
}
