package com.favepc.reader.rfidreaderutility.object;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Bruce_Chiang on 2017/4/5.
 */

public class CustomKeyboardUtil {

    private Context _context;
    private Activity _activity;
    private KeyboardView _keyboardView;
    private Keyboard _keyboard;
    private EditText _editText;


    public CustomKeyboardUtil(Activity act, Context ctx, EditText et, int keyboardXml, int keyboardId) {
        this._context = ctx;
        this._activity = act;
        this._editText = et;

        _keyboard = new Keyboard(_context, keyboardXml);
        _keyboardView = (KeyboardView) act.findViewById(keyboardId);
        _keyboardView.setKeyboard(_keyboard);
        _keyboardView.setEnabled(true);
        _keyboardView.setPreviewEnabled(true);
        _keyboardView.setOnKeyboardActionListener(_keyboardListener);
    }

    public CustomKeyboardUtil(View v, Context ctx, EditText et, int keyboardXml, int keyboardId) {
        this._context = ctx;
        this._editText = et;

        _keyboard = new Keyboard(_context, keyboardXml);
        _keyboardView = (KeyboardView) v.findViewById(keyboardId);
        _keyboardView.setKeyboard(_keyboard);
        _keyboardView.setEnabled(true);
        _keyboardView.setPreviewEnabled(true);
        _keyboardView.setOnKeyboardActionListener(_keyboardListener);
    }

    private KeyboardView.OnKeyboardActionListener _keyboardListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int i) {

        }

        @Override
        public void onRelease(int i) {

        }

        @Override
        public void onKey(int primaryCode, int[] ints) {

            Editable editable = _editText.getText();
            int start = _editText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
            } else if (primaryCode == 57419) { // go left
                if (start > 0) {
                    _editText.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) { // go right
                if (start < _editText.length()) {
                    _editText.setSelection(start + 1);
                }
            } else if (primaryCode == -99) { //do nothing

            }
            else {
                editable.insert(start, Character.toString((char)primaryCode));
            }
        }

        @Override
        public void onText(CharSequence charSequence) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    public void hideKeyboard() {
        int visibility = _keyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            _keyboardView.setVisibility(View.GONE);
        }
    }

    public void showKeyboard() {
        int visibility = _keyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            _keyboardView.setVisibility(View.VISIBLE);
            if (_activity != null)
                _activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    public void showKeyboardAdjust(View v) {
        int visibility = _keyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            _keyboardView.setVisibility(View.VISIBLE);
            if (_activity != null)
                _activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            _activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            int th = _keyboardView.getHeight() + v.getHeight();
            if (th > height) {
                v.setMinimumHeight(v.getHeight() - (th - height));
            }
        }
    }

    public KeyboardView getView() {
        return _keyboardView;
    }
}
