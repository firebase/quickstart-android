package com.droidmentor.mlkitbarcodescan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.droidmentor.mlkitbarcodescan.BarcodeScanner.BarcodeScannerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {
    String TAG = "HomeActivity";

    @BindView(R.id.rvContactsList) RecyclerView rvContactsList;
    @BindView(R.id.fabAdd) Button fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.fabAdd)
    public void onViewClicked() {
        Intent barcodeScanner=new Intent(this, BarcodeScannerActivity.class);
        startActivity(barcodeScanner);
    }
}
