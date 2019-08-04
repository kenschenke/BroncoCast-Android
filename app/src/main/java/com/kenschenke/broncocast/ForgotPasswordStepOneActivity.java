package com.kenschenke.broncocast;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.volley.Request.Method.POST;

public class ForgotPasswordStepOneActivity extends AppCompatActivity implements TextFieldHelperCallable {

    private Pattern emailPattern;

    private TextView textViewEmail, textViewEmailHelp;
    private TextView textViewPhone, textViewPhoneHelp;
    private EditText editTextEmail, editTextPhone;
    private TextFieldHelper emailHelper, phoneHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_step_one);

        setTitle("Forgot Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");

        textViewEmail = findViewById(R.id.textViewEmail);
        textViewEmailHelp = findViewById(R.id.textViewEmailHelp);
        editTextEmail = findViewById(R.id.editTextEmail);
        emailHelper = new TextFieldHelper(editTextEmail, textViewEmail, textViewEmailHelp);
        emailHelper.callable = this;

        textViewPhone = findViewById(R.id.textViewPhone);
        textViewPhoneHelp = findViewById(R.id.textViewPhoneHelp);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.addTextChangedListener(new PhoneMask());
        phoneHelper = new TextFieldHelper(editTextPhone, textViewPhone, textViewPhoneHelp);
        phoneHelper.callable = this;

        textViewEmailHelp.setText("");
        textViewPhoneHelp.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void findAccountClicked(View view) {
        final String email = editTextEmail.getText().toString().trim();
        final String phone = getPhoneDigitsOnly();

        if (!email.isEmpty() && !isEmailValid()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty() && !isPhoneValid()) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !phone.isEmpty()) {
            Toast.makeText(this, "Enter phone or email (but not both)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "Either a phone or email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        final Activity thisActivity = this;

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_RECOVER_SEND), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(thisActivity, ForgotPasswordStepTwoActivity.class);
                    // Send the value to step two in case the user wants the code re-sent
                    intent.putExtra("email", email);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
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
                params.put("Contact", email.isEmpty() ? phone : email);

                return params;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }

    private boolean isEmailValid() {
        String value = editTextEmail.getText().toString().trim();
        Matcher matcher = emailPattern.matcher(value);
        if (!value.isEmpty() && !matcher.matches()) {
            emailHelper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewEmailHelp.setText("Invalid email address");
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

    @Override
    public void onTextFieldChange(EditText editText, String value) {
        if (editText == editTextEmail) {
            isEmailValid();
        } else if (editText == editTextPhone) {
            isPhoneValid();
        }
    }
}
