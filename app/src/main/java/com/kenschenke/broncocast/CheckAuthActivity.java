package com.kenschenke.broncocast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.GET;

public class CheckAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_auth);

        getFcmToken();
    }

    private void checkAuth() {
        BroncoCastApplication app = (BroncoCastApplication) getApplication();

        UrlMaker urlMaker = UrlMaker.getInstance(this);
        String url = urlMaker.getUrl(UrlMaker.URL_ISAUTH);
        if (!app.FcmToken.isEmpty()) {
            try {
                url += "?DeviceToken=" + URLEncoder.encode(app.FcmToken, "UTF-8");
                url += "&DeviceType=FCM_ANDROID";
            } catch (UnsupportedEncodingException e) {

            }
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, url, null, new Response.Listener<JSONObject>() {
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

        app.getRequestQueue().add(jsonObjectRequest);
    }

    private void getFcmToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            BroncoCastApplication app = (BroncoCastApplication) getApplication();
                            app.FcmToken = task.getResult().getToken();
                        }

                        checkAuth();
                    }
                });
    }
}
