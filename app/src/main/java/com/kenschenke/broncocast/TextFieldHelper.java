package com.kenschenke.broncocast;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class TextFieldHelper {

    public enum ValidContext {
        valid, invalid, neutral
    }

    private EditText editText;
    private TextView textViewLabel;
    private TextView textViewHint;

    private Handler handler;
    private Runnable runnable;

    private int defaultTextColor = 0xff000000;

    public TextFieldHelperCallable callable;

    public TextFieldHelper(EditText e) {
        this(e, null, null);
    }

    public TextFieldHelper(EditText e, TextView label, TextView hint) {
        editText = e;
        textViewLabel = label;
        textViewHint = hint;

        if (textViewLabel != null) {
            defaultTextColor = textViewLabel.getCurrentTextColor();
        }

        handler = new Handler();
        callable = null;

        runnable = new Runnable() {
            @Override
            public void run() {
                callable.onTextFieldChange(editText, editText.getText().toString());
            }
        };

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(runnable);
                if (textViewHint != null) {
                    textViewHint.setText("");
                }
                setValidContext(ValidContext.neutral);
                if (callable != null) {
                    handler.postDelayed(runnable, 3000);
                }
            }
        });
    }

    public void setValidContext(ValidContext context) {
        int color = 0;

        switch (context) {
            case valid: color = 0xff00796b; break;
            case invalid: color = 0xfff44336; break;
            case neutral: color = defaultTextColor; break;
        }

        if (textViewLabel != null) {
            textViewLabel.setTextColor(color);
        }
        if (textViewHint != null) {
            textViewHint.setTextColor(color);
        }
    }

}
