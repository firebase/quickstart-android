package com.google.firebase.quickstart.deeplinks.kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.quickstart.deeplinks.R
import com.google.firebase.quickstart.deeplinks.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: DynamicLinksViewModel by viewModels { DynamicLinksViewModel.Factory }

    // [START on_create]
    override fun onCreate(savedInstanceState: Bundle?) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linkSendTextView = binding.linkViewSend
        val linkReceiveTextView = binding.linkViewReceive
        val shortLinkTextView = binding.shortLinkViewSend

        val uriPrefix = getString(R.string.dynamic_links_uri_prefix)

        // Validate that the developer has set the app code.
        viewModel.validateAppCode(uriPrefix)

        // Create a deep link and display it in the UI
        val newDeepLink = viewModel.buildDeepLink(uriPrefix, Uri.parse(DEEP_LINK_URL), 0)
        linkSendTextView.text = newDeepLink.toString()

        // Share button click listener
        binding.buttonShare.setOnClickListener { shareDeepLink(newDeepLink.toString()) }
        // [END_EXCLUDE]

        binding.buttonShareShortLink.setOnClickListener {
            val shortDynamicLink = shortLinkTextView.text
            shareDeepLink(shortDynamicLink.toString())
        }

		binding.buttonGenerateShortLink.setOnClickListener {
            val deepLink = Uri.parse(DEEP_LINK_URL)
            viewModel.buildShortLinkFromParams(uriPrefix, deepLink, 0)
		}

        // [START get_deep_link]
        viewModel.getDynamicLink(intent)
        // [END get_deep_link]

        lifecycleScope.launch {
            viewModel.deepLink.collect { deepLink ->
                if(deepLink.isNotEmpty()){
                    linkReceiveTextView.text = deepLink
                    Snackbar.make(findViewById(android.R.id.content),
                        "Found deep link!", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.shortLink.collect { shortLink ->
                if(shortLink.isNotEmpty()){
                    shortLinkTextView.text = shortLink
                }
            }
        }

        lifecycleScope.launch {
            viewModel.validUriPrefix.collect { flag ->
                if(!flag){
                    AlertDialog.Builder(applicationContext)
                        .setTitle("Invalid Configuration")
                        .setMessage("Please set your Dynamic Links domain in app/build.gradle")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show()
                }
            }
        }


    }
    // [END on_create]

    private fun shareDeepLink(deepLink: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link")
        intent.putExtra(Intent.EXTRA_TEXT, deepLink)

        startActivity(intent)
    }

    companion object {

        private const val TAG = "MainActivity"
        private const val DEEP_LINK_URL = "https://kotlin.example.com/deeplinks"
    }
}
