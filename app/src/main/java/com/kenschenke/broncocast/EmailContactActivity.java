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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

public class EmailContactActivity extends AppCompatActivity implements TextFieldHelperCallable {

    private Pattern emailPattern;

    private TextView textViewEmail, textViewEmailHelp;
    private EditText editTextEmail;
    private TextFieldHelper helper;

    private boolean isNew;

    private String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_contact);

        isNew = getIntent().getBooleanExtra("IsNew", false);

        textViewEmail = findViewById(R.id.textViewEmail);
        textViewEmailHelp = findViewById(R.id.textViewEmailHelp);
        editTextEmail = findViewById(R.id.editTextEmail);
        helper = new TextFieldHelper(editTextEmail, textViewEmail, textViewEmailHelp);
        helper.callable = this;

        setTitle("Edit Email Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        originalEmail = getIntent().getStringExtra("Contact");
        editTextEmail.setText(originalEmail);

        emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");

        if (isNew) {
            findViewById(R.id.buttonDelete).setVisibility(View.INVISIBLE);
            findViewById(R.id.buttonTest).setVisibility(View.INVISIBLE);
        }
    }

    private boolean isEmailValid() {
        String value = editTextEmail.getText().toString();
        Matcher matcher = emailPattern.matcher(value);
        if (!matcher.matches()) {
            helper.setValidContext(TextFieldHelper.ValidContext.invalid);
            textViewEmailHelp.setText("Invalid email address");
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
        builder.setMessage("Delete this email address?")
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
                Toast.makeText(thisActivity, "Unable to delete email", Toast.LENGTH_LONG).show();
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
        if (!isEmailValid()) {
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
                Toast.makeText(thisActivity, "Unable to save email address", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("Key", editTextEmail.getText().toString().trim());

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
        String value = editTextEmail.getText().toString().trim();

        if (!value.equals(originalEmail)) {
            Toast.makeText(this, "You must save the email first.", Toast.LENGTH_LONG).show();
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

                    Toast.makeText(thisActivity, "Test email sent", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(thisActivity, "Unable to send test email", Toast.LENGTH_LONG).show();
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
        isEmailValid();
    }

}
