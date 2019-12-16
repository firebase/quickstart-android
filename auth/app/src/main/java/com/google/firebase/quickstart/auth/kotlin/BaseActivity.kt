package com.google.firebase.quickstart.auth.kotlin

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    fun setProgressBar(resId: Int) {
        progressBar = findViewById(resId)
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    public override fun onStop() {
        super.onStop()
        hideProgressBar()
    }
}
