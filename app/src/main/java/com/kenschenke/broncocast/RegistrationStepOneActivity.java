package com.kenschenke.broncocast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationStepOneActivity extends AppCompatActivity implements TextFieldHelperCallable {

    private Pattern emailPattern;

    private EditText editTextEmail, editTextPassword;
    private TextView textViewEmail, textViewPassword, textViewEmailHelp, textViewPasswordHelp;
    private TextFieldHelper emailHelper, passwordHelper;

    private boolean showPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step_one);

        emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");

        editTextEmail = findViewById(R.id.editTextEmail);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewEmailHelp = findViewById(R.id.textViewEmailHelp);

        editTextPassword = findViewById(R.id.editTextPassword);
        textViewPassword = findViewById(R.id.textViewPassword);
        textViewPasswordHelp = findViewById(R.id.textViewPasswordHelp);

        emailHelper = new TextFieldHelper(editTextEmail, textViewEmail, textViewEmailHelp);
        emailHelper.callable = this;

        passwordHelper = new TextFieldHelper(editTextPassword, textViewPassword, textViewPasswordHelp);
        passwordHelper.callable = this;

        textViewEmailHelp.setText("");
        textViewPasswordHelp.setText("");

        setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void nextClicked(View view) {
        if (!isEmailValid()) {
            Toast.makeText(this, "Email address is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid()) {
            Toast.makeText(this, "Password is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RegistrationStepTwoActivity.class);
        intent.putExtra("email", editTextEmail.getText().toString());
        intent.putExtra("password", editTextPassword.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onTextFieldChange(EditText editText, String value) {
        if (editText == editTextEmail) {
            isEmailValid();
        } else if (editText == editTextPassword) {
            isPasswordValid();
        }
    }

    private boolean isEmailValid() {
        String value = editTextEmail.getText().toString();
        Matcher matcher = emailPattern.matcher(value);
        if (!matcher.matches()) {
            emailHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewEmailHelp.setText("Invalid email address");
            return false;
        }

        return true;
    }

    private boolean isPasswordValid() {
        String value = editTextPassword.getText().toString();
        if (value.length() < 5) {
            passwordHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewPasswordHelp.setText("Password is too short");
            return false;
        }

        return true;
    }

    public void showPasswordClicked(View view) {
        showPassword = !showPassword;

        if (showPassword) {
            editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }
}
