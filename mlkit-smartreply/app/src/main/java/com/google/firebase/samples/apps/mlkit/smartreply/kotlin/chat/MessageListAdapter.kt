package com.google.firebase.samples.apps.mlkit.smartreply.kotlin.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.samples.apps.mlkit.smartreply.R
import com.google.firebase.samples.apps.mlkit.smartreply.kotlin.model.Message
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList

internal class MessageListAdapter : RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

    private val messagesList = ArrayList<Message>()

    var emulatingRemoteUser = false
        set(emulatingRemoteUser) {
            field = emulatingRemoteUser
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListAdapter.MessageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false) as ViewGroup
        return MessageViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (
            messagesList[position].isLocalUser && !emulatingRemoteUser ||
            !messagesList[position].isLocalUser && emulatingRemoteUser) {
            R.layout.item_message_local
        } else {
            R.layout.item_message_remote
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    fun setMessages(messages: List<Message>) {
        messagesList.clear()
        messagesList.addAll(messages)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val icon: CircleImageView
        private val text: TextView

        init {
            icon = itemView.findViewById(R.id.messageAuthor)
            text = itemView.findViewById(R.id.messageText)
        }

        fun bind(message: Message) {
            icon.setImageDrawable(message.getIcon(icon.context))
            text.text = message.text
        }
    }
}
