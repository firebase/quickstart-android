package com.google.firebase.example.fireeats.java.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of {@link Rating}.
 */
public class RatingAdapter extends FirestoreAdapter<RatingAdapter.ViewHolder> {

    public RatingAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(Rating.class));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);

        TextView nameView;
        MaterialRatingBar ratingBar;
        TextView textView;
        TextView dateView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.ratingItemName);
            ratingBar = itemView.findViewById(R.id.ratingItemRating);
            textView = itemView.findViewById(R.id.ratingItemText);
            dateView = itemView.findViewById(R.id.ratingItemDate);
        }

        public void bind(Rating rating) {
            nameView.setText(rating.getUserName());
            ratingBar.setRating((float) rating.getRating());
            textView.setText(rating.getText());

            if (rating.getTimestamp() != null) {
                dateView.setText(FORMAT.format(rating.getTimestamp()));
            }
        }
    }

}
