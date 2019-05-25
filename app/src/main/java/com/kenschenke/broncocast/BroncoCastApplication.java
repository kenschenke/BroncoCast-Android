package com.kenschenke.broncocast;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class BroncoCastApplication extends Application {

    private RequestQueue requestQueue = null;

    BroncoCastApplication() {
        super();
    }

    RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        return requestQueue;
    }

    

}
