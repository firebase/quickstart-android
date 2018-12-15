package com.google.firebase.samples.apps.mlkit.smartreply.ui.chat;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.samples.apps.mlkit.smartreply.R;
import com.google.firebase.samples.apps.mlkit.smartreply.model.Message;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private Context context;
    List<Message> messageList = new ArrayList<>();
    boolean emulatingRemoteUser = false;

    public MessageListAdapter(Context context) {
        super();
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageListAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v =
                (ViewGroup)
                        LayoutInflater.from(parent.getContext())
                                .inflate(viewType, parent, false);
        v.setOnClickListener(
                view -> {
                });
        return new MessageViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.icon.setImageDrawable(message.getIcon(context));
        // Note we rely on the image holder library CircleImageView to make a deep copy.
        holder.text.setText(message.text);
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isLocalUser && !emulatingRemoteUser
                || !messageList.get(position).isLocalUser && emulatingRemoteUser) {
            return R.layout.item_message_local;
        } else {
            return R.layout.item_message_remote;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView icon;
        public TextView text;

        MessageViewHolder(View itemView) {
            super(itemView);
            icon = (CircleImageView) itemView.findViewById(R.id.messageAuthor);
            text = (TextView) itemView.findViewById(R.id.messageText);
        }
    }
}
