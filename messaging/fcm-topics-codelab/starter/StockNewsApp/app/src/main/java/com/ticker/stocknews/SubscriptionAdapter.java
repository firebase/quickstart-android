package com.ticker.stocknews;

import static com.ticker.stocknews.model.StockCategories.STOCK_CATEGORIES;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.ticker.stocknews.model.StockCategory;

/**
 * An adapter for filling a {@link RecyclerView} with stock category subscription / unsubscription toggles.
 */
public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.RecyclerViewViewHolder> {

  private static final String TAG = "SubscriptionAdapter";

  @NonNull
  @Override
  public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_category_item, parent, false);
    return new RecyclerViewViewHolder(rootView);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewViewHolder viewHolder, int position) {
    viewHolder.setData(STOCK_CATEGORIES.get(position));
  }

  @Override
  public int getItemCount() {
    return STOCK_CATEGORIES.size();
  }

  static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {

    private final TextView subscriptionTitleTextView;
    private final SwitchCompat subscriptionSwitch;
    private StockCategory stockCategory;

    public RecyclerViewViewHolder(@NonNull View itemView) {
      super(itemView);
      subscriptionTitleTextView = itemView.findViewById(R.id.subscription_title);
      subscriptionSwitch = itemView.findViewById(R.id.subscription_switch);

      subscriptionSwitch.setOnCheckedChangeListener((view, isChecked) -> {
        // Only perform an operation if the status of the switch has changed, otherwise no action is required.
        if (isChecked == stockCategory.isSubscribed()) {
          return;
        }

        stockCategory.setSubscribed(isChecked);
        if (stockCategory.isSubscribed()) {
          subscribeToStockCategory();
        } else {
          unsubscribeFromStockCategory();
        }
      });
    }

    void subscribeToStockCategory() {
      // Making call to FCM for subscribing to the topic for {@link stockCategory}
      // TODO: Topic subscribing call
    }

    void unsubscribeFromStockCategory() {
      // Making call to FCM for unsubscribing from the topic for {@link stockCategory}
      // TODO: Topic unsubscribing call
    }

    void setData(StockCategory stockCategory) {
      this.stockCategory = stockCategory;
      this.subscriptionTitleTextView.setText(stockCategory.getCategoryName());
      this.subscriptionSwitch.setChecked(stockCategory.isSubscribed());
    }
  }
}
