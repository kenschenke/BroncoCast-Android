package com.kenschenke.broncocast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class BroadcastDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_detail);

        setTitle("Broadcast Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView);
        Intent intent = getIntent();
        textView.setText(intent.getStringExtra("Sent"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
