package com.kenschenke.broncocast;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.POST;

public class ForgotPasswordStepTwoActivity extends AppCompatActivity {

    private EditText editTextResetCode, editTextNewPassword;
    private boolean showPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_step_two);

        setTitle("Forgot Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextResetCode = findViewById(R.id.editTextResetCode);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void showPasswordClicked(View view) {
        showPassword = !showPassword;

        if (showPassword) {
            editTextNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editTextNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void resetPasswordClicked(View view) {
        final String resetCode = editTextResetCode.getText().toString().trim();
        final String newPassword = editTextNewPassword.getText().toString().trim();

        if (resetCode.isEmpty()) {
            Toast.makeText(this, "Reset code required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();

        final String email = intent.getStringExtra("email");
        final String phone = intent.getStringExtra("phone");
        final Activity thisActivity = this;

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_RECOVER_SAVE), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(thisActivity, "Your password has been reset", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(thisActivity, SignInActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable to recover account", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("Contact", email.isEmpty() ? email : phone);
                params.put("Code", resetCode);
                params.put("Password", newPassword);

                return params;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }
}
