package com.google.firebase.samples.apps.mlkit.smartreply.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.samples.apps.mlkit.smartreply.R;
import com.google.firebase.samples.apps.mlkit.smartreply.model.Message;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {
    private ChatViewModel mViewModel;
    private TextView mInputText;
    private MessageListAdapter mChatAdapter;
    private ViewGroup mSmartRepliesContainer;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton switchEmulatedUserButton = view.findViewById(R.id.switchEmulatedUser);
        RecyclerView mChatRecycler = view.findViewById(R.id.chatHistory);
        Button mButton = view.findViewById(R.id.button);
        mSmartRepliesContainer = view.findViewById(R.id.smartRepliesContainer);
        mInputText = view.findViewById(R.id.inputText);
        TextView emulatedUserText = view.findViewById(R.id.switchText);

        // Set up recycler view for chat messages
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mChatRecycler.setLayoutManager(layoutManager);
        mChatAdapter = new MessageListAdapter(requireContext());
        mChatRecycler.setAdapter(mChatAdapter);
        mChatRecycler.setOnTouchListener((v, event) -> {
            InputMethodManager imm =
                    (InputMethodManager) ChatFragment.this.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    view.getWindowToken(), 0);
            return false;
        });

        switchEmulatedUserButton.setOnClickListener(v -> {
            mChatAdapter.emulatingRemoteUser = !mChatAdapter.emulatingRemoteUser;
            mViewModel.switchUser();
            mChatAdapter.notifyDataSetChanged();
        });
        mButton.setOnClickListener(v -> {
            if (mInputText.getText().length() == 0) {
                return;
            }
            String input = mInputText.getText().toString();
            mViewModel.addMessage(input);
            mInputText.setText("");
        });

        mViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        mViewModel.suggestions.observe(this, suggestions -> {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSmartRepliesContainer.removeAllViews();
            for (SmartReplySuggestion suggestion : suggestions) {
                View smartReplyChip = inflater.inflate(R.layout.smart_reply_chip, null);
                TextView textView = smartReplyChip.findViewById(R.id.smartReplyText);
                textView.setText(suggestion.getText());
                smartReplyChip.setOnClickListener(v -> mInputText.setText(suggestion.getText()));
                mSmartRepliesContainer.addView(smartReplyChip, -1,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }

        });
        mViewModel.messageList.observe(this, messages -> {
            mChatAdapter.messageList = messages;
            mChatAdapter.notifyDataSetChanged();
            if (mChatAdapter.getItemCount() > 0) {
                mChatRecycler.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        });
        mViewModel.emulatingRemoteUser.observe(this, isEmulatingRemoteUser -> {
            if (isEmulatingRemoteUser) {
                emulatedUserText.setText("Chatting as Red User");
                emulatedUserText.setTextColor(getResources().getColor(R.color.red));
            } else {
                emulatedUserText.setText("Chatting as Blue User");
                emulatedUserText.setTextColor(getResources().getColor(R.color.blue));
            }
        });
        ArrayList<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Hello. How are you?", false, System.currentTimeMillis()));
        mViewModel.setMessages(messageList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_fragment_actions, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.generateHistoryBasic:
                generateChatHistoryBasic();
                return true;
            case R.id.generateHistorySensitive:
                generateChatHistoryWithSensitiveContent();
                return true;
            case R.id.clearHistory:
                mViewModel.setMessages(new ArrayList());
                return true;
        }
        return false;
    }

    private void generateChatHistoryBasic() {
        ArrayList<Message> messageList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, -1);
        messageList.add(new Message("Hello", true, calendar.getTimeInMillis()));
        calendar.add(Calendar.MINUTE, 10);
        messageList.add(new Message("Hey", false, calendar.getTimeInMillis()));
        mViewModel.setMessages(messageList);
    }

    private void generateChatHistoryWithSensitiveContent() {
        ArrayList<Message> messageList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, -1);
        messageList.add(new Message("Hi", false, calendar.getTimeInMillis()));
        calendar.add(Calendar.MINUTE, 10);
        messageList.add(
                new Message("How are you?", true, calendar.getTimeInMillis()));
        calendar.add(Calendar.MINUTE, 10);
        messageList.add(
                new Message("My cat died", false, calendar.getTimeInMillis()));
        mViewModel.setMessages(messageList);
    }
}
