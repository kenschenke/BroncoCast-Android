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
    public static String URL_BROADCASTS = "api/broadcasts";
    public static String URL_RECOVER_SEND = "api/recover/send";
    public static String URL_RECOVER_SAVE = "api/recover/save";
    public static String URL_PROFILE = "api/profile";
    public static String URL_CONTACTS = "api/contacts";

    private UrlMaker(SharedPreferences sharedPreferences) {
        server = sharedPreferences.getString("server", "www.broncocast.org");
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
