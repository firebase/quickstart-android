package com.google.firebase.quickstart.database.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public TextView nameText;

    public MessageViewHolder(View itemView) {
        super(itemView);
        nameText = (TextView)itemView.findViewById(android.R.id.text1);
        messageText = (TextView) itemView.findViewById(android.R.id.text2);
    }
}
