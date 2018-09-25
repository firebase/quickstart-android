package com.google.firebase.quickstart.invites.kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.appinvite.FirebaseAppInvite
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.quickstart.invites.R
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_INVITE = 0
    }

    // [START on_create]
    override fun onCreate(savedInstanceState: Bundle?) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Invite button click listener
        inviteButton.setOnClickListener(this)
        // [END_EXCLUDE]

        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener(this, OnSuccessListener { data ->
                    if (data == null) {
                        Log.d(TAG, "getInvitation: no data")
                        return@OnSuccessListener
                    }

                    // Get the deep link
                    val deepLink = data.link

                    // Extract invite
                    val invite = FirebaseAppInvite.getInvitation(data)

                    val invitationId = invite.invitationId

                    // Handle the deep link
                    // [START_EXCLUDE]
                    Log.d(TAG, "deepLink:$deepLink")
                    deepLink?.let {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setPackage(packageName)
                        intent.data = it

                        startActivity(intent)
                    }
                    // [END_EXCLUDE]
                })
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }
    // [END on_create]

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        showMessage(getString(R.string.google_play_services_error))
    }

    /**
     * User has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    // [START on_invite_clicked]
    private fun onInviteClicked() {
        val intent = AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link_kotlin)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build()
        startActivityForResult(intent, REQUEST_INVITE)
    }
    // [END on_invite_clicked]

    // [START on_activity_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                for (id in ids) {
                    Log.d(TAG, "onActivityResult: sent invitation $id")
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // [START_EXCLUDE]
                showMessage(getString(R.string.send_failed))
                // [END_EXCLUDE]
            }
        }
    }
    // [END on_activity_result]

    private fun showMessage(msg: String) {
        Snackbar.make(snackbarLayout, msg, Snackbar.LENGTH_SHORT).show()
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.inviteButton) {
            onInviteClicked()
        }
    }
}