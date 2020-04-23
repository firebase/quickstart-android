package com.google.firebase.samples.apps.mlkit.smartreply.java.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.samples.apps.mlkit.smartreply.R;
import com.google.firebase.samples.apps.mlkit.smartreply.databinding.ChatFragmentBinding;
import com.google.firebase.samples.apps.mlkit.smartreply.java.model.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatFragment extends Fragment implements ReplyChipAdapter.ClickListener {

    private ChatViewModel mViewModel;
    private MessageListAdapter mChatAdapter;
    private ReplyChipAdapter mChipAdapter;
    private ChatFragmentBinding binding;

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
        binding = ChatFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Set up recycler view for chat messages
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatHistory.setLayoutManager(layoutManager);
        mChatAdapter = new MessageListAdapter();

        // Set up recycler view for smart replies
        LinearLayoutManager chipManager = new LinearLayoutManager(getContext());
        chipManager.setOrientation(RecyclerView.HORIZONTAL);
        mChipAdapter = new ReplyChipAdapter(this);
        binding.smartRepliesRecycler.setLayoutManager(chipManager);
        binding.smartRepliesRecycler.setAdapter(mChipAdapter);

        binding.chatHistory.setAdapter(mChatAdapter);
        binding.chatHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm =
                        (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        binding.switchEmulatedUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatAdapter.setEmulatingRemoteUser(!mChatAdapter.getEmulatingRemoteUser());
                mViewModel.switchUser();
            }
        });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = binding.inputText.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    return;
                }

                mViewModel.addMessage(input);
                binding.inputText.setText("");
            }
        });

        mViewModel.getSuggestions().observe(getViewLifecycleOwner(), new Observer<List<SmartReplySuggestion>>() {
            @Override
            public void onChanged(List<SmartReplySuggestion> suggestions) {
               mChipAdapter.setSuggestions(suggestions);
            }
        });

        mViewModel.getMessages().observe(getViewLifecycleOwner(), new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                mChatAdapter.setMessages(messages);
                if (mChatAdapter.getItemCount() > 0) {
                    binding.chatHistory.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
                }
            }
        });

        mViewModel.getEmulatingRemoteUser().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEmulatingRemoteUser) {
                if (isEmulatingRemoteUser) {
                    binding.switchText.setText(R.string.chatting_as_red);
                    binding.switchText.setTextColor(getResources().getColor(R.color.red));
                } else {
                    binding.switchText.setText(R.string.chatting_as_blue);
                    binding.switchText.setTextColor(getResources().getColor(R.color.blue));
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
        binding.inputText.setText(chipText);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
