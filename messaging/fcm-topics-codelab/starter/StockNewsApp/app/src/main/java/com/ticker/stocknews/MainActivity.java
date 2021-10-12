package com.ticker.stocknews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

/**
 * Main Activity for StockNews application
 */
public class MainActivity extends AppCompatActivity implements LifecycleOwner {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    RecyclerView recyclerView = findViewById(R.id.rv_main);
    SubscriptionAdapter subscriptionAdapter = new SubscriptionAdapter();
    recyclerView.setAdapter(subscriptionAdapter);

    DividerItemDecoration dividerItemDecoration =
        new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
    recyclerView.addItemDecoration(dividerItemDecoration);
  }
}
