package com.google.firebase.samples.apps.mlkit.smartreply.kotlin.chat

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion
import com.google.firebase.samples.apps.mlkit.smartreply.kotlin.model.Message
import java.util.ArrayList
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val REMOTE_USER_ID = UUID.randomUUID().toString()

    private val suggestions = MediatorLiveData<List<SmartReplySuggestion>>()
    private val messageList = MutableLiveData<MutableList<Message>>()
    private val emulatingRemoteUser = MutableLiveData<Boolean>()

    val messages: LiveData<MutableList<Message>>
        get() = messageList

    init {
        initSuggestionsGenerator()
        emulatingRemoteUser.postValue(false)
    }

    fun getSuggestions(): LiveData<List<SmartReplySuggestion>> {
        return suggestions
    }

    fun getEmulatingRemoteUser(): LiveData<Boolean> {
        return emulatingRemoteUser
    }

    internal fun setMessages(messages: MutableList<Message>) {
        clearSuggestions()
        messageList.postValue(messages)
    }

    internal fun switchUser() {
        clearSuggestions()
        val value = emulatingRemoteUser.value!!
        emulatingRemoteUser.postValue(!value)
    }

    private fun clearSuggestions() {
        suggestions.postValue(ArrayList())
    }

    internal fun addMessage(message: String) {
        var list: MutableList<Message>? = messageList.getValue()
        if (list == null) {
            list = ArrayList()
        }
        val value = emulatingRemoteUser.value!!
        list.add(Message(message, !value, System.currentTimeMillis()))
        clearSuggestions()
        messageList.postValue(list)
    }

    private fun initSuggestionsGenerator() {
        suggestions.addSource(emulatingRemoteUser, Observer { isEmulatingRemoteUser ->
            val list = messageList.value
            if (list == null || list.isEmpty()) {
                return@Observer
            }

            generateReplies(list, isEmulatingRemoteUser!!)
                    .addOnSuccessListener { result -> suggestions.postValue(result) }
        })

        suggestions.addSource(messageList, Observer { list ->
            val isEmulatingRemoteUser = emulatingRemoteUser.value
            if (isEmulatingRemoteUser == null || list!!.isEmpty()) {
                return@Observer
            }

            generateReplies(list, isEmulatingRemoteUser).addOnSuccessListener { result ->
                suggestions.postValue(result)
            }
        })
    }

    private fun generateReplies(
        messages: List<Message>,
        isEmulatingRemoteUser: Boolean
    ): Task<List<SmartReplySuggestion>> {
        val lastMessage = messages[messages.size - 1]

        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        if (lastMessage.isLocalUser && !isEmulatingRemoteUser || !lastMessage.isLocalUser && isEmulatingRemoteUser) {
            return Tasks.forException(Exception("Not running smart reply!"))
        }

        val chatHistory = ArrayList<FirebaseTextMessage>()
        for (message in messages) {
            if (message.isLocalUser && !isEmulatingRemoteUser || !message.isLocalUser && isEmulatingRemoteUser) {
                chatHistory.add(FirebaseTextMessage.createForLocalUser(message.text,
                        message.timestamp))
            } else {
                chatHistory.add(FirebaseTextMessage.createForRemoteUser(message.text,
                        message.timestamp, REMOTE_USER_ID))
            }
        }

        return FirebaseNaturalLanguage.getInstance().smartReply.suggestReplies(chatHistory)
                .continueWith { task -> task.result!!.suggestions }
    }
}
