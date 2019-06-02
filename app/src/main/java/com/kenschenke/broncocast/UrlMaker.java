package com.kenschenke.broncocast;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

public class UrlMaker {
    private static UrlMaker urlInstance = null;
    private String server;

    public static String URL_ISAUTH = "auth/isauth";
    public static String URL_SIGNIN = "auth/login";
    public static String URL_LOGOUT = "logout?applogout";
    public static String URL_REGISTER = "api/register";

    private UrlMaker(SharedPreferences sharedPreferences) {
        server = sharedPreferences.getString("server", "dev.broncocast.org");
    }

    public static UrlMaker getInstance(ContextWrapper contextWrapper) {
        if (urlInstance == null) {
            urlInstance = new UrlMaker(contextWrapper.getSharedPreferences("com.kenschenke.broncocast", Context.MODE_PRIVATE));
        }

        return urlInstance;
    }

    public String getUrl(String url) {
        return "https://" + server + "/" + url;
    }
}
