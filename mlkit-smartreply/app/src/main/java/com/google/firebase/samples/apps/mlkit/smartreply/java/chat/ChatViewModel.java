package com.google.firebase.samples.apps.mlkit.smartreply.java.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
import com.google.firebase.samples.apps.mlkit.smartreply.java.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatViewModel extends ViewModel {

    private final String REMOTE_USER_ID = UUID.randomUUID().toString();

    private MediatorLiveData<List<SmartReplySuggestion>> suggestions = new MediatorLiveData<>();
    private MutableLiveData<List<Message>> messageList = new MutableLiveData<>();
    private MutableLiveData<Boolean> emulatingRemoteUser = new MutableLiveData<>();

    public ChatViewModel() {
        initSuggestionsGenerator();
        emulatingRemoteUser.postValue(false);
    }

    public LiveData<List<SmartReplySuggestion>> getSuggestions() {
        return suggestions;
    }

    public LiveData<List<Message>> getMessages() {
        return messageList;
    }

    public LiveData<Boolean> getEmulatingRemoteUser() {
        return emulatingRemoteUser;
    }

    void setMessages(List<Message> messages) {
        clearSuggestions();
        messageList.postValue(messages);
    }

    void switchUser() {
        clearSuggestions();
        emulatingRemoteUser.postValue(!emulatingRemoteUser.getValue());
    }

    private void clearSuggestions() {
        suggestions.postValue(new ArrayList<SmartReplySuggestion>());
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
        suggestions.addSource(emulatingRemoteUser, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEmulatingRemoteUser) {
                List<Message> list = messageList.getValue();
                if (list == null || list.isEmpty()) {
                    return;
                }

                generateReplies(list, isEmulatingRemoteUser)
                        .addOnSuccessListener(new OnSuccessListener<List<SmartReplySuggestion>>() {
                            @Override
                            public void onSuccess(List<SmartReplySuggestion> result) {
                                suggestions.postValue(result);
                            }
                        });
            }
        });

        suggestions.addSource(messageList, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> list) {
                Boolean isEmulatingRemoteUser = emulatingRemoteUser.getValue();
                if (isEmulatingRemoteUser == null || list.isEmpty()) {
                    return;
                }

                generateReplies(list, isEmulatingRemoteUser).addOnSuccessListener(new OnSuccessListener<List<SmartReplySuggestion>>() {
                    @Override
                    public void onSuccess(List<SmartReplySuggestion> result) {
                        suggestions.postValue(result);
                    }
                });
            }
        });
    }

    private Task<List<SmartReplySuggestion>> generateReplies(List<Message> messages,
                                                             boolean isEmulatingRemoteUser) {
        Message lastMessage = messages.get(messages.size() - 1);

        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        if (lastMessage.isLocalUser && !isEmulatingRemoteUser || !lastMessage.isLocalUser && isEmulatingRemoteUser) {
            return Tasks.forException(new Exception("Not running smart reply!"));
        }

        List<FirebaseTextMessage> chatHistory = new ArrayList<>();
        for (Message message : messages) {
            if (message.isLocalUser && !isEmulatingRemoteUser || !message.isLocalUser && isEmulatingRemoteUser) {
                chatHistory.add(FirebaseTextMessage.createForLocalUser(message.text,
                        message.timestamp));
            } else {
                chatHistory.add(FirebaseTextMessage.createForRemoteUser(message.text,
                        message.timestamp, REMOTE_USER_ID));
            }
        }

        return FirebaseNaturalLanguage.getInstance().getSmartReply().suggestReplies(chatHistory)
                .continueWith(new Continuation<SmartReplySuggestionResult, List<SmartReplySuggestion>>() {
                    @Override
                    public List<SmartReplySuggestion> then(@NonNull Task<SmartReplySuggestionResult> task) {
                        return task.getResult().getSuggestions();
                    }
                });
    }
}
