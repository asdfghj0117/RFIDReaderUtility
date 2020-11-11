package com.favepc.reader.rfidreaderutility.object;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.favepc.reader.rfidreaderutility.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Bruce_Chiang on 2017/11/17.
 */

public class CustomKeyboardManager implements  View.OnTouchListener {//View.OnTouchListener,OnFocusChangeListener

    private static final String TAG = "CustomKeyboardManager";

    private Context mContext;
    private FrameLayout mKeyboardViewContainer;
    private FrameLayout.LayoutParams mKeyboardViewLayoutParams;
    private ViewGroup mRootView;
    private View mShowUnderView;
    private View etFocusScavenger;
    private KeyboardView mCustomKeyboardView;
    private int mKeyboardHeight;
    private int mViewMoveHeight = 0;
    private CustomBaseKeyboard.CustomKeyStyle defaultCustomKeyStyle = new CustomBaseKeyboard.SimpleCustomKeyStyle();

    private boolean mIsShow = false;

    public CustomKeyboardManager(Activity activity) {
        mContext = activity;
        mRootView = (ViewGroup) (activity.getWindow().getDecorView().findViewById(android.R.id.content));//android.R.id.content

        mKeyboardViewContainer = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.adapter_keyboard, null);
        mCustomKeyboardView = (KeyboardView) mKeyboardViewContainer.findViewById(R.id.keyboard_view);
        etFocusScavenger = mKeyboardViewContainer.findViewById(R.id.et_focus_scavenger);
        hideSystemSoftKeyboard((EditText) etFocusScavenger);

        mKeyboardViewLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mKeyboardViewLayoutParams.gravity = Gravity.BOTTOM;
    }

    public CustomKeyboardManager(Dialog dialog) {
        mContext = dialog.getContext();
        mRootView = (ViewGroup) (dialog.getWindow().getDecorView().findViewById(android.R.id.content));//android.R.id.content
        mKeyboardViewContainer = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.adapter_keyboard, null);
        mCustomKeyboardView = (KeyboardView) mKeyboardViewContainer.findViewById(R.id.keyboard_view);
        etFocusScavenger = mKeyboardViewContainer.findViewById(R.id.et_focus_scavenger);
        hideSystemSoftKeyboard((EditText) etFocusScavenger);

        mKeyboardViewLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mKeyboardViewLayoutParams.gravity = Gravity.BOTTOM;
    }

    /*@Override
    public void onFocusChange(View view, boolean b) {
        if (view instanceof EditText) {
            EditText attachEditText = (EditText) view;
            if (b) {
                showSoftKeyboard(attachEditText);
            } else {
                hideSoftKeyboard(attachEditText);
            }
        }
    }*/

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view instanceof EditText && motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
            EditText attachEditText = (EditText) view;
            showSoftKeyboard(attachEditText);

        }
        return false;
    }

    /**
     *
     * @param view
     */
    public void setShowUnderView(View view) {
        mShowUnderView = view;
    }

    /**
     *
     * @param view
     */
    public void showSoftKeyboard(EditText view) {
        CustomBaseKeyboard _keyboard = getKeyboard(view);
        if (null == _keyboard) {
            Log.e(TAG, "The EditText no bind CustomBaseKeyboard!");
            return;
        }
        _keyboard.setCurEditText(view);

        _keyboard.setNextFocusView(etFocusScavenger);

        mCustomKeyboardView.setKeyboard(_keyboard);
        mCustomKeyboardView.setOnKeyboardActionListener(_keyboard);

        if (!mIsShow) {
            mIsShow = true;
            mCustomKeyboardView.setEnabled(true);
            mCustomKeyboardView.setPreviewEnabled(true);
            int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mCustomKeyboardView.measure(width, height);
            mKeyboardHeight = mCustomKeyboardView.getMeasuredHeight();

            mRootView.addView(mKeyboardViewContainer, mKeyboardViewLayoutParams);
            mKeyboardViewContainer.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.down2up));

            setShowUnderView(view);

            mViewMoveHeight = getMoveHeight(view);
            if (mViewMoveHeight > 0) {
                mRootView.getChildAt(0).scrollBy(0, mViewMoveHeight);
            } else {
                mViewMoveHeight = 0;
            }
        }
        view.setTag(R.id.keyboard_view_move_height, mViewMoveHeight);
    }


    /**
     *
     * @param view
     */
    public void hideSoftKeyboard(EditText view) {
        int moveHeight = 0;
        Object tag = view.getTag(R.id.keyboard_view_move_height);
        if (null != tag) moveHeight = (int) tag;
        if (moveHeight > 0) {
            mRootView.getChildAt(0).scrollBy(0, -1 * moveHeight);
            view.setTag(R.id.keyboard_view_move_height, 0);
        }

        mRootView.removeView(mKeyboardViewContainer);

        mKeyboardViewContainer.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.up2hide));

        mIsShow = false;
        mViewMoveHeight = 0;

    }


    /**
     *
     * @param editText
     */
    public static void hideSystemSoftKeyboard(EditText editText) {
        if (Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        }
        else{
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param editText
     * @param keyboard
     */
    public void attachTo(EditText editText, CustomBaseKeyboard keyboard) {
        hideSystemSoftKeyboard(editText);
        editText.setTag(R.id.edittext_bind_keyboard, keyboard);
        if (null == keyboard.getCustomKeyStyle())
            keyboard.setCustomKeyStyle(defaultCustomKeyStyle);
        //editText.setOnFocusChangeListener(this);
        editText.setOnTouchListener(this);
    }

    private CustomBaseKeyboard getKeyboard(View view) {
        Object tag = view.getTag(R.id.edittext_bind_keyboard);
        if (null != tag && tag instanceof CustomBaseKeyboard) {
            return (CustomBaseKeyboard) tag;
        }
        return null;
    }


    private int getMoveHeight(View view) {
        Rect rect = new Rect();
        mRootView.getWindowVisibleDisplayFrame(rect);

        int[] vLocation = new int[2];
        view.getLocationOnScreen(vLocation);
        int keyboardTop = vLocation[1] + view.getHeight() + view.getPaddingBottom() + view.getPaddingTop();
        if (keyboardTop - mKeyboardHeight < 0) {
            return 0;
        }
        if (null != mShowUnderView) {
            int[] underVLocation = new int[2];
            mShowUnderView.getLocationOnScreen(underVLocation);
            keyboardTop = underVLocation[1] + mShowUnderView.getHeight() + mShowUnderView.getPaddingBottom() + mShowUnderView.getPaddingTop();
        }

        int moveHeight = keyboardTop + mKeyboardHeight - rect.bottom;
        return moveHeight > 0 ? moveHeight : 0;
    }


}
