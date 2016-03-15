package com.google.firebase.quickstart.database;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.FirebaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Message;

import java.util.Map;

/**
 * Activity to demonstrate basic data querying. To start this Activity, run:
 * <code>adb shell am start -n com.google.firebase.quickstart.database/.QueryActivity</code>
 *
 * Use {@link MainActivity} to populate the Message data.
 */
public class QueryActivity extends AppCompatActivity {

    private static final String TAG = "QueryActivity";

    private DatabaseReference mFirebaseRef;
    private DatabaseReference mMessagesRef;
    private Query mMessagesQuery;

    private ValueEventListener mMessagesListener;
    private ChildEventListener mMessagesQueryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions.Builder(getString(R.string.google_api_key)).build());

        // Get a reference to the Firebase Database
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();

        // [START basic_listen]
        // Get a reference to Messages and attach a listener
        mMessagesRef = mFirebaseRef.child("messages");
        mMessagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.

                // Get the data as Message objects
                GenericTypeIndicator<Map<String, Message>> type =
                        new GenericTypeIndicator<Map<String, Message>>() {};
                Map<String,Message> messages = dataSnapshot.getValue(type);

                // Use the messages
                // [START_EXCLUDE]
                Log.d(TAG, "onDataChange:numMessages:" + messages.size());
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "messages:onCancelled:" + firebaseError.getMessage());
            }
        };
        mMessagesRef.addValueEventListener(mMessagesListener);
        // [END basic_listen]

        // [START basic_query]
        // Get a list of messages ordered by text
        mMessagesQuery = mFirebaseRef.child("messages").orderByChild("text");
        mMessagesQueryListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String key) {
                // New child matching the query. When this listener is initially attached
                // this method will be called once for each existing matching child.
                Log.d(TAG, "added:" + dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String key) {
                Log.d(TAG, "changed:" + dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "removed:" + dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String key) {
                Log.d(TAG, "moved:" + dataSnapshot.getValue(Message.class));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "messagesQuery:onCancelled:" + firebaseError.getMessage());
            }
        };
        mMessagesQuery.addChildEventListener(mMessagesQueryListener);
        // [END basic_query]
    }

    @Override
    public void onStop() {
        super.onStop();

        // Clean up value listener
        // [START clean_basic_listen]
        mMessagesRef.removeEventListener(mMessagesListener);
        // [END clean_basic_listen]

        // Clean up query listener
        // [START clean_basic_query]
        mMessagesQuery.removeEventListener(mMessagesQueryListener);
        // [END clean_basic_query]
    }
}
