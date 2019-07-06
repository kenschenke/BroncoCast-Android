package com.kenschenke.broncocast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class NetworkUtil {

    public static void signInUser(final Activity activity, final String username, final String password) {
        final BroncoCastApplication app = (BroncoCastApplication) activity.getApplication();

        UrlMaker urlMaker = UrlMaker.getInstance(activity);
        StringRequest stringRequest = new StringRequest(POST, urlMaker.getUrl(UrlMaker.URL_SIGNIN), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("Success")) {
                        Toast.makeText(activity.getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity.getApplicationContext(), "Unable to sign in", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                SharedPreferences prefs = activity.getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                headers.put("cookie", prefs.getString("AuthCookie", ""));
                return headers;
            }

            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("_username", username);
                params.put("_password", password);
                params.put("_remember_me", "on");
                params.put("applogin","true");

                if (!app.FcmToken.isEmpty()) {
                    params.put("DeviceToken", app.FcmToken);
                    params.put("DeviceType", "FCM_ANDROID");
                }

                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String rememberMeCookie = response.headers.get("Set-Cookie");
                SharedPreferences prefs = activity.getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE);
                prefs.edit().putString("AuthCookie", rememberMeCookie).apply();
                return super.parseNetworkResponse(response);
            }
        };

        app.getRequestQueue().add(stringRequest);
    }

}
