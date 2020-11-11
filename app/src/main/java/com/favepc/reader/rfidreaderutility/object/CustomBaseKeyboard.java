package com.favepc.reader.rfidreaderutility.object;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.XmlRes;

/**
 * Created by Bruce_Chiang on 2017/11/17.
 */

public abstract class CustomBaseKeyboard extends Keyboard implements KeyboardView.OnKeyboardActionListener {

    protected EditText etCurrent;
    protected View nextFocusView;
    protected CustomKeyStyle customKeyStyle;

    public CustomBaseKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public CustomBaseKeyboard(Context context, @XmlRes int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public CustomBaseKeyboard(Context context, @XmlRes int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
    }

    public CustomBaseKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    protected int getKeyCode(int resId) {
        if (null != etCurrent) {
            return etCurrent.getContext().getResources().getInteger(resId);
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public void setCurEditText(EditText etCurrent) {
        this.etCurrent = etCurrent;
    }

    public EditText getCurEditText() {
        return etCurrent;
    }

    public void setNextFocusView(View view) {
        this.nextFocusView = view;
    }

    public CustomKeyStyle getCustomKeyStyle() {
        return customKeyStyle;
    }

    public void setCustomKeyStyle(CustomKeyStyle customKeyStyle) { this.customKeyStyle = customKeyStyle; }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (null != etCurrent && etCurrent.hasFocus() && !handleSpecialKey(etCurrent, primaryCode)) {
            Editable editable = etCurrent.getText();
            int start = etCurrent.getSelectionStart();

            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                hideKeyboard(etCurrent);
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
            } else if (primaryCode == 57419) { // go left
                if (start > 0) {
                    etCurrent.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) { // go right
                if (start < etCurrent.length()) {
                    etCurrent.setSelection(start + 1);
                }
            } else if (primaryCode == -99) { //do nothing

            }
            else {
                editable.insert(start, Character.toString((char)primaryCode));
            }
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

    public abstract void hideKeyboard(EditText etCurrent); /*{
        if (null != nextFocusView) nextFocusView.requestFocus();
    }*/

    public abstract boolean handleSpecialKey(EditText etCurrent, int primaryCode);


    public interface CustomKeyStyle {
        Drawable getKeyBackground(Key key, EditText etCur);

        Float getKeyTextSize(Key key, EditText etCur);

        Integer getKeyTextColor(Key key, EditText etCur);

        CharSequence getKeyLabel(Key key, EditText etCur);
    }

    public static class SimpleCustomKeyStyle implements CustomKeyStyle {

        @Override
        public Drawable getKeyBackground(Key key, EditText etCur) {
            return key.iconPreview;
        }

        @Override
        public Float getKeyTextSize(Key key, EditText etCur) {
            return null;
        }

        @Override
        public Integer getKeyTextColor(Key key, EditText etCur) {
            return null;
        }

        @Override
        public CharSequence getKeyLabel(Key key, EditText etCur) {
            return key.label;
        }

        protected int getKeyCode(Context context, int resId) {
            if (null != context) {
                return context.getResources().getInteger(resId);
            } else {
                return Integer.MIN_VALUE;
            }
        }

        protected Drawable getDrawable(Context context, int resId) {
            if (null != context) {
                return context.getDrawable(resId);
                //return context.getResources().getDrawable(resId);
            }
            return null;
        }
    }
}
