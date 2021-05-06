package com.google.samples.quickstart.crash.java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED;

/**
 * The following are samples of custom keys that may be useful to record via Crashlytics prior to a Crash.
 * <p>
 * These utility methods are not meant to be comprehensive, but they are illustrative of the types of things you
 * could track using custom keys in Crashlytics.
 */
public class CustomKeySamples {


    private final Context context;

    // Lazily instantiated when a listener is set up.
    private ConnectivityManager.NetworkCallback callback = null;

    public CustomKeySamples(Context context) {
        this.context = context;
    }

    /**
     * Set a subset of custom keys simultaneously.
     */
    public void setSampleCustomKeys() {
        FirebaseCrashlytics.getInstance().setCustomKeys(new CustomKeysAndValues.Builder()
                .putString("Locale", getLocale())
                .putFloat("Screen Density", getDensity())
                .putString("Google Play Services Availability", getGooglePlayServicesAvailability())
                .putString("Os Version", getOsVersion())
                .putString("Install Source", getInstallSource())
                .putString("Preferred ABI", getPreferredAbi()).build());
    }

    /**
     * Update network state and add a hook to update network state going forward.
     *
     * Note: This code is executed above API level N.
     */
    public void updateAndTrackNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkCapabilities networkCapabilities = connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (networkCapabilities != null) {
                updateNetworkCapabilityCustomKeys(networkCapabilities);
            }
            synchronized(this) {
                if (callback == null) {
                    // Set up a callback to match our best-practices around custom keys being up-to-date
                    callback = new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                            updateNetworkCapabilityCustomKeys(networkCapabilities);
                        }
                    };
                    connectivityManager.registerDefaultNetworkCallback(callback);
                }
            }
        }
    }

    /**
     * Remove the handler for the network state.
     *
     * Note: This code is executed above API level N.
     */
    public void stopTrackingNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        synchronized(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && callback != null) {
                connectivityManager.unregisterNetworkCallback(callback);
                callback = null;
            }
        }
    }

    private void updateNetworkCapabilityCustomKeys(NetworkCapabilities networkCapabilities) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FirebaseCrashlytics.getInstance().setCustomKeys(new CustomKeysAndValues.Builder()
                    .putInt("Network Bandwidth", networkCapabilities.getLinkDownstreamBandwidthKbps())
                    .putInt("Network Upstream", networkCapabilities.getLinkUpstreamBandwidthKbps())
                    .putBoolean("Network Metered", networkCapabilities.hasCapability(NET_CAPABILITY_NOT_METERED))
                    // This key is long and not as easy to filter by.
                    .putString("Network Capabilities", networkCapabilities.toString()).build());
        }
    }

    /**
     * @see {@link com.google.samples.quickstart.crash.java.CustomKeySamples#updateAndTrackNetworkState},
     * which does not require READ_PHONE_STATE
     * and returns more useful information about bandwidth, metering, and capabilities.
     *
     * Supressed deprecation warning because that code path is only used below API Level N.
     */
    @Deprecated
    @SuppressLint("MissingPermission")
    public void addPhoneStateRequiredNetworkKeys() {
        TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int networkType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkType = telephonyManager.getDataNetworkType();
        } else {
            networkType = telephonyManager.getNetworkType();
        }
        String simOperatorId = telephonyManager.getSimOperator();
        FirebaseCrashlytics.getInstance().setCustomKey("Network Type", networkType);
        FirebaseCrashlytics.getInstance().setCustomKey("Sim Operator", simOperatorId);
    }

    /**
     * Retrieve the locale information for the app.
     *
     * Supressed deprecation warning because that code path is only used below API Level N.
     */
    @SuppressWarnings("deprecation")
    public String getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context
                    .getResources()
                    .getConfiguration()
                    .getLocales().get(0).toString();
        }

        return context
                .getResources()
                .getConfiguration().locale.toString();
    }

    /**
     * Retrieve the screen density information for the app.
     */
    public float getDensity() {
        return context
                .getResources()
                .getDisplayMetrics()
                .density;
    }

    /**
     * Retrieve the locale information for the app.
     */
    public String getGooglePlayServicesAvailability() {
        return GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(context) == 0 ? "Unavailable" : "Available";
    }

    /**
     * Return the underlying kernel version of the Android device.
     */
    public String getOsVersion() {
        String osVersion = System.getProperty("os.version");
        return osVersion != null ? osVersion : "Unknown";
    }

    /**
     * Retrieve the preferred ABI of the device. Some devices can support
     * multiple ABIs and the first one returned in the preferred one.
     *
     * Supressed deprecation warning because that code path is only used below Lollipop.
     */
    @SuppressWarnings("deprecation")
    public String getPreferredAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS[0];
        }

        return Build.CPU_ABI;
    }

    /**
     * Retrieve the install source and return it as a string.
     *
     * Supressed deprecation warning because that code path is only used below API level R.
     */
    public String getInstallSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                InstallSourceInfo info = context
                        .getPackageManager()
                        .getInstallSourceInfo(context.getPackageName());

                String originating = info.getOriginatingPackageName() == null ? "None" :
                        info.getOriginatingPackageName();
                String installing = info.getInstallingPackageName() == null ? "None" : info.getInstallingPackageName();
                String initiating = info.getInitiatingPackageName() == null ? "None" : info.getInitiatingPackageName();

                // This returns all three of the install source, originating source, and initiating
                // source.
                return "Originating: " + originating +
                        ", Installing: " + installing +
                        ", Initiating: " + initiating;
            } catch (PackageManager.NameNotFoundException e) {
                return "Unknown";
            }
        }

        String installerPackageName = context
                .getPackageManager()
                .getInstallerPackageName(context.getPackageName());

        return installerPackageName == null ? "None" : installerPackageName;
    }

    /**
     * Add a focus listener that updates a custom key when this view gains the focus.
     **/
    public void focusListener(View view, boolean hasFocus) {
        if (hasFocus) {
            FirebaseCrashlytics.getInstance().setCustomKey("view_focus", view.getId());
        }
    }
}
