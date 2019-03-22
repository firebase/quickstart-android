package com.google.firebase.samples.apps.mlkit.smartreply.java.chat;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.samples.apps.mlkit.smartreply.R;
import com.google.firebase.samples.apps.mlkit.smartreply.java.model.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatFragment extends Fragment implements ReplyChipAdapter.ClickListener {

    private ChatViewModel mViewModel;
    private TextView mInputText;
    private Button mSendButton;
    private Button mSwitchUserButton;

    private RecyclerView mChatRecycler;
    private MessageListAdapter mChatAdapter;

    private RecyclerView mSmartRepliesRecyler;
    private ReplyChipAdapter mChipAdapter;

    private TextView mEmulatedUserText;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        mChatRecycler = view.findViewById(R.id.chatHistory);
        mEmulatedUserText = view.findViewById(R.id.switchText);
        mSmartRepliesRecyler = view.findViewById(R.id.smartRepliesRecycler);
        mInputText = view.findViewById(R.id.inputText);
        mSendButton = view.findViewById(R.id.button);
        mSwitchUserButton = view.findViewById(R.id.switchEmulatedUser);

        // Set up recycler view for chat messages
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mChatRecycler.setLayoutManager(layoutManager);
        mChatAdapter = new MessageListAdapter();

        // Set up recycler view for smart replies
        LinearLayoutManager chipManager = new LinearLayoutManager(getContext());
        chipManager.setOrientation(RecyclerView.HORIZONTAL);
        mChipAdapter = new ReplyChipAdapter(this);
        mSmartRepliesRecyler.setLayoutManager(chipManager);
        mSmartRepliesRecyler.setAdapter(mChipAdapter);

        mChatRecycler.setAdapter(mChatAdapter);
        mChatRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm =
                        (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        mSwitchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatAdapter.setEmulatingRemoteUser(!mChatAdapter.getEmulatingRemoteUser());
                mViewModel.switchUser();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mInputText.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    return;
                }

                mViewModel.addMessage(input);
                mInputText.setText("");
            }
        });

        mViewModel.getSuggestions().observe(this, new Observer<List<SmartReplySuggestion>>() {
            @Override
            public void onChanged(List<SmartReplySuggestion> suggestions) {
               mChipAdapter.setSuggestions(suggestions);
            }
        });

        mViewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                mChatAdapter.setMessages(messages);
                if (mChatAdapter.getItemCount() > 0) {
                    mChatRecycler.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
                }
            }
        });

        mViewModel.getEmulatingRemoteUser().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEmulatingRemoteUser) {
                if (isEmulatingRemoteUser) {
                    mEmulatedUserText.setText(R.string.chatting_as_red);
                    mEmulatedUserText.setTextColor(getResources().getColor(R.color.red));
                } else {
                    mEmulatedUserText.setText(R.string.chatting_as_blue);
                    mEmulatedUserText.setTextColor(getResources().getColor(R.color.blue));
                }
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
                mViewModel.setMessages(new ArrayList<Message>());
                return true;
        }
        return false;
    }

    @Override
    public void onChipClick(@NonNull String chipText) {
        mInputText.setText(chipText);
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
        messageList.add(new Message("How are you?", true, calendar.getTimeInMillis()));

        calendar.add(Calendar.MINUTE, 10);
        messageList.add(new Message("My cat died", false, calendar.getTimeInMillis()));

        mViewModel.setMessages(messageList);
    }
}
