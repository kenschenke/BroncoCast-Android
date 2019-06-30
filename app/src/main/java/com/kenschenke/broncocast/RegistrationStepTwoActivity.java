package com.kenschenke.broncocast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

// TODO
// Send request to server
// If success, go to main activity

public class RegistrationStepTwoActivity extends AppCompatActivity implements TextFieldHelperCallable {

    private EditText editTextName, editTextPhone, editTextInviteCode;
    private TextView textViewName, textViewNameHelp, textViewPhone, textViewPhoneHelp;
    private TextFieldHelper nameHelper, phoneHelper;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step_two);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextInviteCode = findViewById(R.id.editTextInviteCode);
        textViewName = findViewById(R.id.textViewName);
        textViewNameHelp = findViewById(R.id.textViewNameHelp);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewPhoneHelp = findViewById(R.id.textViewPhoneHelp);
        buttonRegister = findViewById(R.id.buttonRegister);

        editTextPhone.addTextChangedListener(new PhoneMask());

        nameHelper = new TextFieldHelper(editTextName, textViewName, textViewNameHelp);
        nameHelper.callable = this;
        phoneHelper = new TextFieldHelper(editTextPhone, textViewPhone, textViewPhoneHelp);
        phoneHelper.callable = this;

        textViewNameHelp.setText("");
        textViewPhoneHelp.setText("");

        setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onTextFieldChange(EditText editText, String value) {
        if (editText == editTextName) {
            isNameValid();
        } else if (editText == editTextPhone) {
            isPhoneValid();
        }
    }

    public void registerClicked(View view) {
        if (!isNameValid()) {
            Toast.makeText(this, "Full name required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPhoneValid()) {
            Toast.makeText(this, "Valid phone required", Toast.LENGTH_SHORT).show();
            return;
        }

        String inviteCode = editTextInviteCode.getText().toString();

        if (inviteCode.isEmpty()) {
            Toast.makeText(this, "Invite code required", Toast.LENGTH_SHORT).show();
            return;
        }

        registerUser(inviteCode);
    }

    public void registerUser(final String inviteCode) {
        Intent intent = getIntent();

        final String name = editTextName.getText().toString();
        final String phone = getPhoneDigitsOnly();
        final String email = intent.getStringExtra("email");
        final String password = intent.getStringExtra("password");
        final Activity thisActivity = this;

        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        buttonRegister.setEnabled(false);

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_REGISTER), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                buttonRegister.setEnabled(true);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    NetworkUtil.signInUser(thisActivity, email, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable to register", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("Name", name);
                params.put("Email", email);
                params.put("Password", password);
                params.put("Phone", phone);
                params.put("OrgTag", inviteCode);

                return params;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }

    private boolean isNameValid() {
        String value = editTextName.getText().toString();
        if (value.length() < 8) {
            nameHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewNameHelp.setText("Please enter your full name");
            return false;
        }

        return true;
    }

    private String getPhoneDigitsOnly() {
        String value = editTextPhone.getText().toString();
        return value.replaceAll("[^0-9]", "");
    }

    private boolean isPhoneValid() {
        String phone = getPhoneDigitsOnly();

        if (!phone.isEmpty() && phone.length() != 10) {
            phoneHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewPhoneHelp.setText("Phone number must be 10 digits");
            return false;
        }

        return true;
    }
}
