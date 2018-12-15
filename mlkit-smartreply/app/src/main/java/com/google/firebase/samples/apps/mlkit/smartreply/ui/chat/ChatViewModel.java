package com.google.firebase.samples.apps.mlkit.smartreply.ui.chat;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.common.collect.EvictingQueue;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.samples.apps.mlkit.smartreply.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatViewModel extends ViewModel {
    MediatorLiveData<List<SmartReplySuggestion>> suggestions = new MediatorLiveData<>();
    MutableLiveData<List<Message>> messageList = new MutableLiveData<>();
    MutableLiveData<Boolean> emulatingRemoteUser = new MutableLiveData<>();
    private final String REMOTE_USER_ID = UUID.randomUUID().toString();
    private final int MAX_NUMBER_OF_MESSAGES = 10;

    public ChatViewModel() {
        initSuggestionsGenerator();
        emulatingRemoteUser.postValue(false);
    }

    void setMessages(List messages) {
        clearSuggestions();
        messageList.postValue(messages);
    }

    void switchUser() {
        clearSuggestions();
        emulatingRemoteUser.postValue(!emulatingRemoteUser.getValue());
    }

    private void clearSuggestions() {
        suggestions.postValue(new ArrayList<>());
    }

    void addMessage(String message) {
        List<Message> list = messageList.getValue();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(new Message(message, !emulatingRemoteUser.getValue(), System.currentTimeMillis()));
        clearSuggestions();
        messageList.postValue(list);
    }

    private void initSuggestionsGenerator() {
        suggestions.addSource(emulatingRemoteUser, isEmulatingRemoteUser -> {
            List<Message> list = messageList.getValue();
            if (list == null || list.isEmpty()) {
                return;
            }
            generateReplies(list, isEmulatingRemoteUser).addOnSuccessListener(result -> suggestions.postValue(result));
        });
        suggestions.addSource(messageList, list -> {
            Boolean isEmulatingRemoteUser = emulatingRemoteUser.getValue();
            if (isEmulatingRemoteUser == null || list.isEmpty()) {
                return;
            }
            generateReplies(list, isEmulatingRemoteUser).addOnSuccessListener(result -> suggestions.postValue(result));
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private Task<List<SmartReplySuggestion>> generateReplies(List<Message> messages,
                                                             boolean isEmulatingRemoteUser) {
        Message lastMessage = messages.get(messages.size() - 1);
        TaskCompletionSource<List<SmartReplySuggestion>> source = new TaskCompletionSource<>();
        Task<List<SmartReplySuggestion>> result = source.getTask();
        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        if (lastMessage.isLocalUser && !isEmulatingRemoteUser || !lastMessage.isLocalUser && isEmulatingRemoteUser) {
            source.setException(new Exception("Not running smart reply"));
        } else {
            List<FirebaseTextMessage> chatHistory =
                    new ArrayList<>(MAX_NUMBER_OF_MESSAGES);
            for (Message message : messages) {
                if (message.isLocalUser && !isEmulatingRemoteUser || !message.isLocalUser && isEmulatingRemoteUser) {
                    chatHistory.add(FirebaseTextMessage.createForLocalUser(message.text,
                            message.timestamp));
                } else {
                    chatHistory.add(FirebaseTextMessage.createForRemoteUser(message.text,
                            message.timestamp, REMOTE_USER_ID));
                }
            }
            FirebaseNaturalLanguage.getInstance().getSmartReply().suggestReplies(chatHistory)
                    .addOnSuccessListener(smartReplySuggestionResult -> source.setResult(smartReplySuggestionResult.getSuggestions()))
                    .addOnFailureListener(source::setException);
        }
        return result;
    }
}
