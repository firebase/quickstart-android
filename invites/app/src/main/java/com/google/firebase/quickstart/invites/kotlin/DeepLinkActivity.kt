package com.google.firebase.quickstart.invites.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.quickstart.invites.R
import kotlinx.android.synthetic.main.deep_link_activity.*

class DeepLinkActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val TAG = DeepLinkActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deep_link_activity)

        // Button click listener
        buttonOk.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        // Check for link in intent
        intent?.let {
            it.data?.let { data ->
                Log.d(TAG, "data:$data")
                deepLinkText.text = getString(R.string.deep_link_fmt, data.toString())
            }
        }
    }


    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonOk) {
            finish()
        }
    }
}