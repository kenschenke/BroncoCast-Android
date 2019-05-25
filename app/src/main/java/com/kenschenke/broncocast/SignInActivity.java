package com.kenschenke.broncocast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.POST;

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
        btnSignIn.setText("Signing In");
        btnSignIn.setEnabled(false);
        btnRegister.setEnabled(false);
        btnForgotPassword.setEnabled(false);

        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_SIGNIN), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable to sign in", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("_username", editTextUsername.getText().toString());
                params.put("_password", editTextPassword.getText().toString());
                params.put("_remember_me", "on");
                params.put("applogin","true");

                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String rememberMeCookie = response.headers.get("Set-Cookie");
                SharedPreferences prefs = getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                prefs.edit().putString("AuthCookie", rememberMeCookie).apply();
                return super.parseNetworkResponse(response);
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }
}
