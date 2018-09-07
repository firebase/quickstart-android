package com.google.firebase.quickstart.firebasestorage.kotlin

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.firebasestorage.R
import com.google.firebase.quickstart.firebasestorage.java.MyDownloadService
import com.google.firebase.quickstart.firebasestorage.java.MyUploadService
import java.util.*

/**
 * Activity to upload and download photos from Firebase Storage.
 *
 * See [MyUploadService] for upload example.
 * See [MyDownloadService] for download example.
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mBroadcastReceiver: BroadcastReceiver? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null

    private var mDownloadUrl: Uri? = null
    private var mFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Click listeners
        findViewById<View>(R.id.button_camera).setOnClickListener(this)
        findViewById<View>(R.id.button_sign_in).setOnClickListener(this)
        findViewById<View>(R.id.button_download).setOnClickListener(this)

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI)
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL)
        }
        onNewIntent(intent)

        // Local broadcast receiver
        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive:$intent")
                hideProgressDialog()

                when (intent.action) {
                    MyDownloadService.DOWNLOAD_COMPLETED -> {
                        // Get number of bytes downloaded
                        val numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0)

                        // Alert success
                        showMessageDialog(getString(R.string.success), String.format(Locale.getDefault(),
                                "%d bytes downloaded from %s",
                                numBytes,
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)))
                    }
                    MyDownloadService.DOWNLOAD_ERROR ->
                        // Alert failure
                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)))
                    MyUploadService.UPLOAD_COMPLETED, MyUploadService.UPLOAD_ERROR -> onUploadResultIntent(intent)
                }
            }
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent)
        }

    }

    public override fun onStart() {
        super.onStart()
        updateUI(mAuth!!.currentUser)

        // Register receiver for uploads and downloads
        val manager = LocalBroadcastManager.getInstance(this)
        manager.registerReceiver(mBroadcastReceiver!!, MyDownloadService.getIntentFilter())
        manager.registerReceiver(mBroadcastReceiver!!, MyUploadService.getIntentFilter())
    }

    public override fun onStop() {
        super.onStop()

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver!!)
    }

    public override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putParcelable(KEY_FILE_URI, mFileUri)
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.d(TAG, "onActivityResult:$requestCode:$resultCode:$data")
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                mFileUri = data.data

                if (mFileUri != null) {
                    uploadFromUri(mFileUri!!)
                } else {
                    Log.w(TAG, "File URI is null")
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFromUri(fileUri: Uri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString())

        // Save the File URI
        mFileUri = fileUri

        // Clear the last download, if any
        updateUI(mAuth!!.currentUser)
        mDownloadUrl = null

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(Intent(this, MyUploadService::class.java)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD))

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading))
    }

    private fun beginDownload() {
        // Get path
        val path = "photos/" + mFileUri!!.lastPathSegment

        // Kick off MyDownloadService to download the file
        val intent = Intent(this, MyDownloadService::class.java)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD)
        startService(intent)

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_downloading))
    }

    private fun launchCamera() {
        Log.d(TAG, "launchCamera")

        // Pick an image from storage
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, RC_TAKE_PICTURE)
    }

    private fun signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        showProgressDialog(getString(R.string.progress_auth))
        mAuth!!.signInAnonymously()
                .addOnSuccessListener(this) { authResult ->
                    Log.d(TAG, "signInAnonymously:SUCCESS")
                    hideProgressDialog()
                    updateUI(authResult.user)
                }
                .addOnFailureListener(this) { exception ->
                    Log.e(TAG, "signInAnonymously:FAILURE", exception)
                    hideProgressDialog()
                    updateUI(null)
                }
    }

    private fun onUploadResultIntent(intent: Intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL)
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI)

        updateUI(mAuth!!.currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        // Signed in or Signed out
        if (user != null) {
            findViewById<View>(R.id.layout_signin).visibility = View.GONE
            findViewById<View>(R.id.layout_storage).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.layout_signin).visibility = View.VISIBLE
            findViewById<View>(R.id.layout_storage).visibility = View.GONE
        }

        // Download URL and Download button
        if (mDownloadUrl != null) {
            (findViewById<View>(R.id.picture_download_uri) as TextView).text = mDownloadUrl!!.toString()
            findViewById<View>(R.id.layout_download).visibility = View.VISIBLE
        } else {
            (findViewById<View>(R.id.picture_download_uri) as TextView).text = null
            findViewById<View>(R.id.layout_download).visibility = View.GONE
        }
    }

    private fun showMessageDialog(title: String, message: String) {
        val ad = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create()
        ad.show()
    }

    private fun showProgressDialog(caption: String) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.setMessage(caption)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            updateUI(null)
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.button_camera) {
            launchCamera()
        } else if (i == R.id.button_sign_in) {
            signInAnonymously()
        } else if (i == R.id.button_download) {
            beginDownload()
        }
    }

    companion object {

        private val TAG = "Storage#MainActivity"

        private val RC_TAKE_PICTURE = 101

        private val KEY_FILE_URI = "key_file_uri"
        private val KEY_DOWNLOAD_URL = "key_download_url"
    }
}
