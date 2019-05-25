package com.kenschenke.broncocast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ForgotPasswordStepTwoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_step_two);

        setTitle("Forgot Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
