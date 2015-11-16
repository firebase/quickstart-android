/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.database;

import com.google.firebase.quickstart.database.models.Message;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

    /**
     * Our reference to the root of our Firebase database.
     */
    private Firebase mFirebaseRef;

    /**
     * The username of the signed in user, or nil if logged out.
     */
    private String mUsername;

    /**
     * The message send button.
     */
    private ImageButton mSend;

    /**
     * The chat text entry.
     */
    private EditText mTextEdit;

    /**
     * Our message display list.
     */
    private RecyclerView mRecycler;

    /**
     * The layout manager for our view.
     */
    private LinearLayoutManager mLlm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create a reference to a location in our Firebase Database that stores the chat messages.
        //TODO: Fix before release to use google-services.json
        mFirebaseRef = new Firebase("YOUR_FIREBASE_DATABASE_URL");

        mTextEdit = (EditText) findViewById(R.id.text_edit);
        mTextEdit.setOnEditorActionListener(this);

        mUsername = UUID.randomUUID().toString().substring(0, 8);

        mSend = (ImageButton) findViewById(R.id.send_button);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a message object out of the username and the message the user entered
                Message message = new Message("User " + mUsername, mTextEdit.getText().toString());

                // We will only send non-empty messages
                if (message.getText().length() > 0) {
                    // Create a new message in the Firebase Database.
                    mFirebaseRef.child("messages").push().setValue(message);

                    // Clear the existing message, to make it obvious to the user that it was sent
                    // to Firebase.
                    mTextEdit.setText("");
                    Snackbar.make(findViewById(android.R.id.content),
                            "Message sent",
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    mTextEdit.requestFocus();
                }
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
        mLlm = new LinearLayoutManager(this);
        mLlm.setStackFromEnd(true);
        mRecycler.setLayoutManager(mLlm);

        /**
         * Adapter for the view of chat messages from the Firebase Database.
         */
        final FirebaseRecyclerViewAdapter<Message,MessageViewHolder> firebaseAdapter =
                new FirebaseRecyclerViewAdapter<Message,MessageViewHolder> (
                        Message.class,
                        android.R.layout.simple_list_item_2,
                        MessageViewHolder.class,
                        mFirebaseRef.child("messages")) {

            @Override
            public void populateViewHolder(MessageViewHolder viewHolder, Message message, int position) {
                viewHolder.nameText.setText(message.getName());
                viewHolder.messageText.setText(message.getText());
            }
        };
        mRecycler.setAdapter(firebaseAdapter);

        // Display new items if at start of list.
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int max = firebaseAdapter.getItemCount();
                int lastVis = mLlm.findLastCompletelyVisibleItemPosition();
                if (positionStart >= (max - 1) && lastVis == (positionStart - 1)) {
                    mRecycler.scrollToPosition(positionStart);
                }

            }
        });

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            if (v.getText().length() > 0) {
                mSend.performClick();
            } else {
                mTextEdit.requestFocus();
            }
            return true;
        }
        return false;
    }

    /**
     * ViewHolder for use in our RecyclerView.
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView nameText;

        public MessageViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView)itemView.findViewById(android.R.id.text1);
            messageText = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
