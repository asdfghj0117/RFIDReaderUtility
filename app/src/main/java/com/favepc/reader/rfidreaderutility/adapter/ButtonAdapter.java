package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by Bruce_Chiang on 2017/4/11.
 */

public class ButtonAdapter extends androidx.appcompat.widget.AppCompatEditText {

    public ButtonAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        if (error == null) {
            setCompoundDrawables(null, null, icon, null);
        }
        else
            super.setError(error, icon);
    }
}
