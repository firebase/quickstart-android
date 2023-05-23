package com.google.firebase.quickstart.deeplinks.kotlin

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DynamicLinksViewModel(
    private val dynamicLinks: FirebaseDynamicLinks
): ViewModel() {

    private val _deepLink = MutableStateFlow("")
    val deepLink: StateFlow<String> = _deepLink

    private val _shortLink = MutableStateFlow("")
    val shortLink: StateFlow<String> = _shortLink

    private val _validUriPrefix = MutableStateFlow(true)
    val validUriPrefix: StateFlow<Boolean> = _validUriPrefix

    fun getDynamicLink(intent: Intent) {
        viewModelScope.launch {
            try {
                val pendingDynamicLinkData: PendingDynamicLinkData = dynamicLinks
                    .getDynamicLink(intent)
                    .await()

                val deepLink: Uri? = pendingDynamicLinkData.link

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // Display deep link in the UI
                if (deepLink != null) {
                    _deepLink.value = deepLink.toString()
                } else {
                    Log.d(TAG, "getDynamicLink: no link found")
                }
            } catch (e: Exception) {
                Log.w(TAG, "getDynamicLink:onFailure", e)
            }
        }
    }


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
//    @VisibleForTesting
    fun buildDeepLink(uriPrefix: String, deepLink: Uri, minVersion: Int): Uri {
        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // Build the dynamic link
        val link = Firebase.dynamicLinks.dynamicLink {
            domainUriPrefix = uriPrefix
            androidParameters {
                minimumVersion = minVersion
            }
            link = deepLink
        }

        // Return the dynamic link as a URI
        return link.uri
    }

//    @VisibleForTesting
    fun buildShortLinkFromParams(uriPrefix: String, deepLink: Uri, minVersion: Int) {
        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link

        try {
            viewModelScope.launch {
                val shortDynamicLinks = Firebase.dynamicLinks.shortLinkAsync {
                    link = deepLink
                    domainUriPrefix = uriPrefix
                    androidParameters {
                        minimumVersion = minVersion
                    }
                }.await()

                val shortLinks = shortDynamicLinks.shortLink
                // val flowChartLink = shortDynamicLinks.previewLink

                _shortLink.value = shortLinks.toString()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun validateAppCode(uriPrefix: String) {
        if (uriPrefix.contains("YOUR_APP")) {
            _validUriPrefix.value = false
        }
    }

    companion object {
        const val TAG = "DynamicLinksViewModel"

        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get Remote Config instance.
                val dynamicLinks = Firebase.dynamicLinks
                return DynamicLinksViewModel(dynamicLinks) as T
            }
        }
    }
}