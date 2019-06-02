package com.kenschenke.broncocast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class SignInActivity extends AppCompatActivity {

    private Button btnSignIn, btnRegister, btnForgotPassword;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private boolean showPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.buttonRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextNewPassword);
    }

    public void forgotPasswordClicked(View view) {
        Intent intent = new Intent(this, ForgotPasswordStepOneActivity.class);
        startActivity(intent);
    }

    public void showPasswordClicked(View view) {
        showPassword = !showPassword;

        if (showPassword) {
            editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void registerClicked(View view) {
        Intent intent = new Intent(this, RegistrationStepOneActivity.class);
        startActivity(intent);
    }

    public void signInClicked(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        NetworkUtil.signInUser(this, editTextUsername.getText().toString(), editTextPassword.getText().toString());
    }
}
