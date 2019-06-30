package com.kenschenke.broncocast;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Locale;

public class PhoneMask implements TextWatcher {

    private static final int MAX_LENGTH = 10;
    private static final int MIN_LENGTH = 3;

    private String updatedText;
    private boolean editing;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (text.toString().equals(updatedText) || editing) {
            return;
        }

        String digits = text.toString().replaceAll("[^0-9]", "");
        int length = digits.length();

        if (length <= MIN_LENGTH) {
            updatedText = digits;
            return;
        }

        if (length > MAX_LENGTH) {
            digits = digits.substring(0, MAX_LENGTH);
        }

        if (length <= 6) {
            String areaCode = digits.substring(0, 3);
            String exchange = digits.substring(3);

            updatedText = String.format(Locale.US, "(%s) %s", areaCode, exchange);
        } else {
            String areaCode = digits.substring(0, 3);
            String exchange = digits.substring(3, 6);
            String number = digits.substring(6);

            updatedText = String.format(Locale.US, "(%s) %s-%s", areaCode, exchange, number);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing) {
            return;
        }

        editing = true;

        s.clear();
        s.insert(0, updatedText);

        editing = false;
    }
}
