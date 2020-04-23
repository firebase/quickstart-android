package com.google.firebase.example.fireeats.java.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.example.fireeats.databinding.ItemRatingBinding;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * RecyclerView adapter for a list of {@link Rating}.
 */
public class RatingAdapter extends FirestoreAdapter<RatingAdapter.ViewHolder> {

    public RatingAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRatingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(Rating.class));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);

        private ItemRatingBinding binding;

        public ViewHolder(ItemRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Rating rating) {
            binding.ratingItemName.setText(rating.getUserName());
            binding.ratingItemRating.setRating((float) rating.getRating());
            binding.ratingItemText.setText(rating.getText());

            if (rating.getTimestamp() != null) {
                binding.ratingItemDate.setText(FORMAT.format(rating.getTimestamp()));
            }
        }
    }

}
