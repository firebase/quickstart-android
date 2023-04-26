package com.google.firebase.quickstart.deeplinks.kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.component1
import com.google.firebase.dynamiclinks.ktx.component2
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.deeplinks.R
import com.google.firebase.quickstart.deeplinks.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // [START on_create]
    override fun onCreate(savedInstanceState: Bundle?) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linkSendTextView = binding.linkViewSend
        val linkReceiveTextView = binding.linkViewReceive

        // Validate that the developer has set the app code.
        validateAppCode()

        // Create a deep link and display it in the UI
        val newDeepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0)
        linkSendTextView.text = newDeepLink.toString()

        // Share button click listener
        binding.buttonShare.setOnClickListener { shareDeepLink(newDeepLink.toString()) }
        // [END_EXCLUDE]

        binding.buttonShareShortLink.setOnClickListener {
            val shortLinkTextView = findViewById<TextView>(R.id.shortLinkViewSend)
            val shortDynamicLink = shortLinkTextView.text
            shareDeepLink(shortDynamicLink.toString())
        }

        binding.buttonGenerateShortLink.setOnClickListener {
            val deepLink = Uri.parse(DEEP_LINK_URL)
            buildShortLinkFromParams(deepLink, 0)
        }

        // [START get_deep_link]
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // [START_EXCLUDE]
                // Display deep link in the UI
                if (deepLink != null) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Found deep link!",
                        Snackbar.LENGTH_LONG,
                    ).show()

                    linkReceiveTextView.text = deepLink.toString()
                } else {
                    Log.d(TAG, "getDynamicLink: no link found")
                }
                // [END_EXCLUDE]
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
        // [END get_deep_link]
    }
    // [END on_create]

    /**
     * Build a Firebase Dynamic Link.
     * https://firebase.google.com/docs/dynamic-links/android/create#create-a-dynamic-link-from-parameters
     *
     * @param deepLink the deep link your app will open. This link must be a valid URL and use the
     * HTTP or HTTPS scheme.
     * @param minVersion the `versionCode` of the minimum version of your app that can open
     * the deep link. If the installed app is an older version, the user is taken
     * to the Play store to upgrade the app. Pass 0 if you do not
     * require a minimum version.
     * @return a [Uri] representing a properly formed deep link.
     */
    @VisibleForTesting
    fun buildDeepLink(deepLink: Uri, minVersion: Int): Uri {
        val uriPrefix = getString(R.string.dynamic_links_uri_prefix)

        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // [START build_dynamic_link]
        // Build the dynamic link
        val link = Firebase.dynamicLinks.dynamicLink {
            domainUriPrefix = uriPrefix
            androidParameters {
                minimumVersion = minVersion
            }
            link = deepLink
        }
        // [END build_dynamic_link]

        // Return the dynamic link as a URI
        return link.uri
    }

    @VisibleForTesting
    fun buildShortLinkFromParams(deepLink: Uri, minVersion: Int) {
        val uriPrefix = getString(R.string.dynamic_links_uri_prefix)

        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        Firebase.dynamicLinks.shortLinkAsync {
            link = deepLink
            domainUriPrefix = uriPrefix
            androidParameters {
                minimumVersion = minVersion
            }
        }.addOnSuccessListener { (shortLink, flowchartLink) ->
            val shortLinkTextView = findViewById<TextView>(R.id.shortLinkViewSend)
            shortLinkTextView.text = shortLink.toString()
        }.addOnFailureListener(this) { e ->
            Log.e(TAG, e.toString())
        }
    }

    private fun shareDeepLink(deepLink: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link")
        intent.putExtra(Intent.EXTRA_TEXT, deepLink)

        startActivity(intent)
    }

    private fun validateAppCode() {
        val uriPrefix = getString(R.string.dynamic_links_uri_prefix)
        if (uriPrefix.contains("YOUR_APP")) {
            AlertDialog.Builder(this)
                .setTitle("Invalid Configuration")
                .setMessage("Please set your Dynamic Links domain in app/build.gradle")
                .setPositiveButton(android.R.string.ok, null)
                .create().show()
        }
    }

    companion object {

        private const val TAG = "MainActivity"
        private const val DEEP_LINK_URL = "https://kotlin.example.com/deeplinks"
    }
}
