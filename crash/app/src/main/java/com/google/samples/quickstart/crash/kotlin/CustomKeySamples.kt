package com.google.samples.quickstart.crash.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.internal.synchronized

/**
 * The following are samples of custom keys that may be useful to record via Crashlytics prior to a Crash.
 *
 * These utility methods are not meant to be comprehensive, but they are illustrative of the types of things you
 * could track using custom keys in Crashlytics.
 */
class CustomKeySamples(private val context: Context, private var callback: NetworkCallback? = null) {

    /**
     * Set a subset of custom keys simultaneously.
     */
    fun setSampleCustomKeys() {
        Firebase.crashlytics.setCustomKeys {
            key("Locale", locale)
            key("Screen Density", density)
            key("Google Play Services Availability", googlePlayServicesAvailability)
            key("Os Version", osVersion)
            key("Install Source", installSource)
            key("Preferred ABI", preferredAbi)
        }
    }

    /**
     * Update network state and add a hook to update network state going forward.
     *
     * Note: This code is executed above API level N.
     */
    fun updateAndTrackNetworkState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager
                    .getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?.let { updateNetworkCapabilityCustomKeys(it) }

            kotlin.synchronized(this) {
                if (callback == null) {
                    // Set up a callback to match our best-practices around custom keys being up-to-date
                    val newCallback: NetworkCallback = object : NetworkCallback() {
                        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                            updateNetworkCapabilityCustomKeys(networkCapabilities)
                        }
                    }
                    callback = newCallback
                    connectivityManager.registerDefaultNetworkCallback(newCallback)
                }
            }
        }
    }

    /**
     * Remove the handler for the network state.
     *
     * Note: This code is executed above API level N.
     */
    fun stopTrackingNetworkState() {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val oldCallback = callback
        kotlin.synchronized(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && oldCallback != null) {
                connectivityManager.unregisterNetworkCallback(oldCallback)
                callback = null
            }
        }
    }

    private fun updateNetworkCapabilityCustomKeys(networkCapabilities: NetworkCapabilities) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Firebase.crashlytics.setCustomKeys {
                key("Network Bandwidth", networkCapabilities.linkDownstreamBandwidthKbps)
                key("Network Upstream", networkCapabilities.linkUpstreamBandwidthKbps)
                key("Network Metered", networkCapabilities
                        .hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
                // This key is long and not as easy to filter by.
                key("Network Capabilities", networkCapabilities.toString())
            }
        }
    }

    /**
     * @see {@link com.google.samples.quickstart.crash.java.CustomKeySamples.updateAndTrackNetworkState}, which does not require READ_PHONE_STATE
     * and returns more useful information about bandwidth, metering, and capabilities.
     *
     * Supressed deprecation warning because that code path is only used below API Level N.
     *
     * Supressed Lint warning because READ_PHONE_STATE is a high priority permission and
     * we don't want to enforce needing it for this code example.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Prefer updateAndTrackNetworkState, which does not require READ_PHONE_STATE")
    @SuppressLint("MissingPermission")
    fun addPhoneStateRequiredNetworkKeys() {
        val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val networkType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            telephonyManager.dataNetworkType
        } else {
            telephonyManager.networkType
        }

        Firebase.crashlytics.setCustomKeys {
            key("Network Type", networkType)
            key("Sim Operator", telephonyManager.simOperator)
        }
    }

    /**
     * Retrieve the locale information for the app.
     *
     * Supressed deprecation warning because that code path is only used below API Level N.
     */
    @Suppress("DEPRECATION")
    val locale: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context
                    .resources
                    .configuration
                    .locales[0].toString()
        } else {
            context
                    .resources
                    .configuration.locale.toString()
        }

    /**
     * Retrieve the screen density information for the app.
     */
    val density: Float
        get() = context
                .resources
                .displayMetrics.density

    /**
     * Retrieve the locale information for the app.
     */
    val googlePlayServicesAvailability: String
        get() = if (GoogleApiAvailability
                        .getInstance()
                        .isGooglePlayServicesAvailable(context) == 0) "Unavailable" else "Available"

    /**
     * Return the underlying kernel version of the Android device.
     */
    val osVersion: String
        get() = System.getProperty("os.version") ?: "Unknown"

    /**
     * Retrieve the preferred ABI of the device. Some devices can support
     * multiple ABIs and the first one returned in the preferred one.
     *
     * Supressed deprecation warning because that code path is only used below Lollipop.
     */
    @Suppress("DEPRECATION")
    val preferredAbi: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS[0]
        } else Build.CPU_ABI

    /**
     * Retrieve the install source and return it as a string.
     *
     * Supressed deprecation warning because that code path is only used below API level R.
     */
    @Suppress("DEPRECATION")
    val installSource: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val info = context
                        .packageManager
                        .getInstallSourceInfo(context.packageName)

                // This returns all three of the install source, originating source, and initiating
                // source.
                "Originating: ${info.originatingPackageName ?: "None"}, " +
                        "Installing: ${info.installingPackageName ?: "None"}, " +
                        "Initiating: ${info.initiatingPackageName ?: "None"}"
            } catch (e: PackageManager.NameNotFoundException) {
                "Unknown"
            }
        } else {
            context.packageManager.getInstallerPackageName(context.packageName) ?: "None"
        }

    /**
     * Add a focus listener that updates a custom key when this view gains the focus.
     */
    fun focusListener(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            Firebase.crashlytics.setCustomKey("view_focus", view.id)
        }
    }
}
