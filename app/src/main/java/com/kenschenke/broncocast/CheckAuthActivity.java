package com.kenschenke.broncocast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.GET;

public class CheckAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_auth);

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, urlMaker.getUrl(UrlMaker.URL_ISAUTH), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Intent intent;
                    if (response.getBoolean("IsAuth")) {
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), SignInActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error", error.toString());
                Toast.makeText(getApplicationContext(), "Unable to contact server", Toast.LENGTH_LONG).show();
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
        app.getRequestQueue().add(jsonObjectRequest);
    }

    public void noClicked(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    public void yesClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
