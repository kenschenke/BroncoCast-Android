package com.kenschenke.broncocast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class BroadcastDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_detail);

        setTitle("Broadcast Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        TextView textViewSentBy = findViewById(R.id.textViewSentBy);
        textViewSentBy.setText(intent.getStringExtra("SentBy"));

        TextView textViewDelivered = findViewById(R.id.textViewDelivered);
        textViewDelivered.setText(intent.getStringExtra("Delivered"));

        TextView textViewShortMsg = findViewById(R.id.textViewShortMsg);
        textViewShortMsg.setText(intent.getStringExtra("ShortMsg"));

        TextView textViewLongMsg = findViewById(R.id.textViewLongMsg);
        String longMsg = intent.getStringExtra("LongMsg");
        if (longMsg.isEmpty()) {
            findViewById(R.id.textViewLongMsgLabel).setVisibility(View.INVISIBLE);
            textViewLongMsg.setVisibility(View.INVISIBLE);
        } else {
            textViewLongMsg.setText(longMsg);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
