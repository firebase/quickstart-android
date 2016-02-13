package com.google.firebase.quickstart.firebasestorage;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        EasyPermissions.PermissionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_PERMISSIONS = 102;
    private static final int RC_SIGN_IN = 103;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String[] perms = new String[]{
            Manifest.permission.CAMERA,
    };


    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private GoogleApiClient mGoogleApiClient;
    private Uri mFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));

        // Initialize Firebase Storage and Auth
        mStorage = new FirebaseStorage(getString(R.string.bucket_name));
        mAuth = FirebaseAuth.getAuth();

        // Click listeners
        findViewById(R.id.button_camera).setOnClickListener(this);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        // GoogleApiClient with Sign In
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(getString(R.string.server_client_id))
                                .build())
                .build();

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE && resultCode == RESULT_OK) {
            if (mFileUri != null) {
                uploadFromUri(mFileUri);
            } else {
                Log.w(TAG, "File URI is null");
            }
        }

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void uploadFromUri(Uri uri) {
        Log.d(TAG, "uploadFromUri:" + uri.toString());

        String fileName = uri.getLastPathSegment();
        mStorage.getChild("photos").getChild(fileName).putFile(uri).addCallback(this,
                new UploadTask.Callback() {
                    @Override
                    protected void onCompleted(UploadTask uploadTask) {
                        Log.d(TAG, "uploadFromUri:onCompleted:" + uploadTask.getResultCode());
                        Uri uploadUri = uploadTask.getUploadUri();
                        if (uploadUri != null) {
                            // Display the link in the UI
                            // TODO(samstern): The real link is in the StorageMetadata
                            ((TextView) findViewById(R.id.picture_download_uri))
                                    .setText(uploadUri.toString());
                        }
                    }

                    @Override
                    protected void onFailure(UploadTask uploadTask, int errorCode) {
                        Log.d(TAG, "uploadFromUri:onFailure:" + errorCode);
                        // Show an error message
                        Toast.makeText(MainActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @AfterPermissionGranted(RC_PERMISSIONS)
    private void launchCamera() {
        // Check for camera permissions
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this,
                    "This sample will upload a picture from your Camera",
                    RC_PERMISSIONS, perms);
            return;
        }

        // Create intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Choose file storage location
        File file;
        try {
            file = File.createTempFile(UUID.randomUUID().toString(), ".jpg", getCacheDir());

            mFileUri = Uri.fromFile(file);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create file", e);
            return;
        }

        // Launch intent
        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult:" + result.getStatus());
        if (result.isSuccess() && result.getSignInAccount() != null) {
            String idToken = result.getSignInAccount().getIdToken();
            mAuth.signInWithCredential(
                    GoogleAuthProvider.getCredential(idToken, null))
                    .setResultCallback(new ResultCallback<AuthResult>() {
                        @Override
                        public void onResult(@NonNull AuthResult result) {
                            Log.d(TAG, "onResult:" + result);
                            updateUI(result.getUser());
                        }
                    });
        } else {
            updateUI(null);
        }
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            findViewById(R.id.layout_signin).setVisibility(View.GONE);
            findViewById(R.id.layout_storage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_signin).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_storage).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_camera:
                launchCamera();
                break;
            case R.id.google_sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }
}
