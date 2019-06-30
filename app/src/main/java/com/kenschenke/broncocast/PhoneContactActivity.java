package com.kenschenke.broncocast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

public class PhoneContactActivity extends AppCompatActivity implements TextFieldHelperCallable {

    private TextView textViewPhone, textViewPhoneHelp;
    private EditText editTextPhone;
    private TextFieldHelper helper;

    private boolean isNew;

    private String originalPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contact);

        isNew = getIntent().getBooleanExtra("IsNew", false);

        textViewPhone = findViewById(R.id.textViewPhone);
        textViewPhoneHelp = findViewById(R.id.textViewPhoneHelp);
        editTextPhone = findViewById(R.id.editTextPhone);
        helper = new TextFieldHelper(editTextPhone, textViewPhone, textViewPhoneHelp);
        helper.callable = this;

        editTextPhone.addTextChangedListener(new PhoneMask());

        setTitle("Edit Phone Number Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        originalPhone = getIntent().getStringExtra("Contact");
        editTextPhone.setText(originalPhone);

        if (isNew) {
            findViewById(R.id.buttonDelete).setVisibility(View.INVISIBLE);
            findViewById(R.id.buttonTest).setVisibility(View.INVISIBLE);
        }
    }

    private boolean isPhoneValid() {
        String value = editTextPhone.getText().toString().replaceAll("[^0-9]", "");
        if (value.length() != 10) {
            helper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewPhoneHelp.setText("Phone number must be 10 digits");
            return false;
        }

        return true;
    }

    public void deleteClicked(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteContact();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this phone number?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();

    }

    private void deleteContact() {
        UrlMaker urlMaker = UrlMaker.getInstance(this);
        String url = urlMaker.getUrl(UrlMaker.URL_CONTACTS);
        url += "/" + getIntent().getStringExtra("ContactId");
        final Activity thisActivity = this;

        StringRequest stringRequest = new StringRequest(DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(thisActivity, jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    setResult(Activity.RESULT_OK);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(thisActivity, "Unable to delete phone number", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }

    public void saveClicked(View view) {
        if (!isPhoneValid()) {
            return;
        }

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        String url = urlMaker.getUrl(UrlMaker.URL_CONTACTS);
        if (!isNew) {
            url += "/" + getIntent().getStringExtra("ContactId");
        }

        final Activity thisActivity = this;

        StringRequest stringRequest = new StringRequest(isNew ? POST : PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(thisActivity, jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    setResult(Activity.RESULT_OK);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(thisActivity, "Unable to save phone number", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("Key", editTextPhone.getText().toString().replaceAll("[^0-9]", ""));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }

    public void sendTestClicked(View view) {
        String value = editTextPhone.getText().toString().trim();

        if (!value.equals(originalPhone)) {
            Toast.makeText(this, "You must save the phone number first.", Toast.LENGTH_LONG).show();
            return;
        }

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        String url = urlMaker.getUrl(UrlMaker.URL_CONTACTS);
        url += "/test/" + getIntent().getStringExtra("ContactId");
        final Activity thisActivity = this;

        StringRequest stringRequest = new StringRequest(PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(thisActivity, jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(thisActivity, "Test text message sent", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(thisActivity, "Unable to send test text message", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }
        };

        BroncoCastApplication app = (BroncoCastApplication) getApplication();
        app.getRequestQueue().add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onTextFieldChange(EditText editText, String value) {
        isPhoneValid();
    }

}
