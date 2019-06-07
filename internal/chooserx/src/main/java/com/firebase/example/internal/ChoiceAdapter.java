package com.firebase.example.internal;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ViewHolder> {

    private final Activity activity;
    private List<Choice> choices;

    public ChoiceAdapter(Activity activity, List<Choice> choices) {
        this.activity = activity;
        this.choices = choices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Choice choice = choices.get(position);
        holder.bind(choice);
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView descText;
        private final Button launchButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.item_title);
            descText = itemView.findViewById(R.id.item_description);
            launchButton = itemView.findViewById(R.id.item_launch_button);
        }

        public void bind(final Choice choice) {
            titleText.setText(choice.title);
            descText.setText(choice.description);
            launchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(choice.launchIntent);
                }
            });
        }
    }

}
