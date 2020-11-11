package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.favepc.reader.rfidreaderutility.R;

/**
 * Created by Bruce_Chiang on 2017/4/7.
 */

public class HorizontalPickerView extends View {

    public interface OnValueChangeListener {
        void onValueChange(float value);
    }

    private OnValueChangeListener mListener;
    private static final int ITEM_DIVIDER = 40;
    private static final int TEXT_SIZE = 18;
    private static final int ITEM_MAX_HEIGHT = 10;
    //private static final int VALUE_MIN = -2;
    //private static final int VALUE_MAX = 29;
    private static final float MAX_TEXT_ALPHA = 255;
    private static final float MIN_TEXT_ALPHA = 120;
    public static final float MARGIN_ALPHA = 2.8f;

    private Context mContext;
    private Scroller mScroller;
    private float mDensity;
    private int mMinVelocity;
    private int mCurrentValue = -2, mMinValue = -2, mMaxValue = 18, mLineDivider = ITEM_DIVIDER;
    private int mLastX, mMove;
    private int mWidth, mHeight;
    private float mMaxTextSize = 40;
    private float mMinTextSize = 20;
    private VelocityTracker mVelocityTracker;

    public HorizontalPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mScroller = new Scroller(getContext());
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        this.mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        setBackground(drawBackground(context));
    }

    public void initViewParam(int currentValue, int maxValue) {

        this.mLineDivider = ITEM_DIVIDER;//?
        this.mMinValue = currentValue;
        this.mCurrentValue = currentValue;
        this.mMaxValue = maxValue;

        invalidate();
        this.mLastX = 0;
        this.mMove = 0;
        notifyValueChange();
    }

    public void setValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    public float getValue() {
        return mCurrentValue;
    }

    public void setValue(int currentValue) {
        this.mCurrentValue = currentValue;
        invalidate();
        this.mLastX = 0;
        this.mMove = 0;
        notifyValueChange();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        this.mMaxTextSize = mHeight / 1.5f;
        this.mMinTextSize = mMaxTextSize / 2f;
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScaleLine(canvas);
        drawMiddleRect(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int xPosition = (int) event.getX();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove += (mLastX - xPosition);
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker(event);
                return false;
            // break;
            default:
                break;
        }
        mLastX = xPosition;
        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) { // over
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove += (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }

    private GradientDrawable drawBackground(Context context) {

        int colors[] = { ContextCompat.getColor(context, R.color.colorGray400),
                        ContextCompat.getColor(context, R.color.colorGray100),
                        ContextCompat.getColor(context, R.color.colorGray400)};
        setPadding((int)(1 * mDensity), (int)(1 * mDensity), (int)(1 * mDensity), 0);
        GradientDrawable bgDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bgDrawable.setCornerRadius(2 * mDensity);
        bgDrawable.setStroke((int)(1 * mDensity), ContextCompat.getColor(context, R.color.colorGray500));
        return bgDrawable;
    }

    private void drawMiddleRect(Canvas canvas) {
        int indexWidth = 5;
        canvas.save();
        Paint redPaint = new Paint();
        redPaint.setStrokeWidth(indexWidth);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeCap(Paint.Cap.ROUND);
        redPaint.setColor(ContextCompat.getColor(this.mContext, R.color.colorGray700));
        //canvas.drawRect((mWidth / 2) - 40, indexWidth, (mWidth / 2) + 40, mHeight - indexWidth, redPaint);
        /**
         * Draw the specified round-rect using the specified paint. The roundrect will be filled or
         * framed based on the Style in the paint.
         *
         * @param rx The x-radius of the oval used to round the corners
         * @param ry The y-radius of the oval used to round the corners
         * @param paint The paint used to draw the roundRect
         */

        //Call requires API level 21 (current min is 19)
        //canvas.drawRoundRect((mWidth / 2) - 50, indexWidth, (mWidth / 2) + 50, mHeight - indexWidth,
        //        2, 2, redPaint);
        //instead in below
        RectF rectf= new RectF((mWidth / 2) - 50, indexWidth, (mWidth / 2) + 50, mHeight - indexWidth);
        canvas.drawRoundRect(rectf, 2, 2, redPaint);
        canvas.restore();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            mListener.onValueChange(mCurrentValue);
        }
    }

    private float countLeftStart(int value, float xPosition, float textWidth) {
        float xp = 0f;
        if (value < 0) {
            xp = xPosition - (float)(textWidth * 1.5 / 2);
        }
        else if (value < 10) {
            xp = xPosition - (textWidth * 1 / 2);
        }
        else {
            xp = xPosition - (textWidth * 2 / 2);
        }
        return xp;
    }

    private float parabola(float zero, float x)
    {
        double d = Math.pow(x / zero, 2);
        float f = (float) (1 - d);
        return f < 0 ? 0 : f;

    }

    private void drawScaleLine(Canvas canvas) {
        canvas.save();

        int width = mWidth, drawCount = 0;
        float xPosition = (width / 2 - mMove);

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.BLACK);

        //middle text
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        float mid_scale = parabola(mWidth / 4.0f, mMove);
        float mid_size = (mMaxTextSize - mMinTextSize) * mid_scale + mMinTextSize;
        textPaint.setTextSize(mid_size);//TEXT_SIZE * mDensity

        textPaint.setAlpha((int) ((MAX_TEXT_ALPHA - MIN_TEXT_ALPHA) * mid_scale + MIN_TEXT_ALPHA));
        float mid_textWidth = Layout.getDesiredWidth("0", textPaint);
        /**
         * Draw the text, with origin at (x,y), using the specified paint. The origin is interpreted
         * based on the Align setting in the paint.
         *
         * @param text The text to be drawn
         * @param x The x-coordinate of the origin of the text being drawn
         * @param y The y-coordinate of the baseline of the text being drawn
         * @param paint The paint used for the text (e.g. color, size, style)
         */
        canvas.drawText(String.valueOf(mCurrentValue), countLeftStart(mCurrentValue, xPosition, mid_textWidth), getHeight() - mid_textWidth + 10, textPaint);

        //other text

        for (int i = 1; drawCount <= 4 * width; i++) {
            //right side
            float iDensity = i * mLineDivider * mDensity;
            xPosition = (width / 2 - mMove) + iDensity;
            if (xPosition + getPaddingRight() < mWidth) {
                canvas.drawLine(xPosition, getPaddingTop(), xPosition, mDensity * ITEM_MAX_HEIGHT, linePaint);
                if (mCurrentValue + i <= mMaxValue) {
                    float rd = (float) (MARGIN_ALPHA * mMinTextSize * i * mDensity - 1 * mMove);
                    float scalerd = parabola(mWidth / 3f, rd);
                    float sizerd = (mMaxTextSize - mMinTextSize) * scalerd + mMinTextSize;
                    TextPaint rtextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                    rtextPaint.setTextSize(sizerd);
                    rtextPaint.setAlpha((int) ((MAX_TEXT_ALPHA - MIN_TEXT_ALPHA) * scalerd + MIN_TEXT_ALPHA));
                    float rtextWidth = Layout.getDesiredWidth("0", rtextPaint);
                    canvas.drawText(String.valueOf(mCurrentValue + i), countLeftStart(mCurrentValue + i, xPosition, rtextWidth), getHeight() - rtextWidth + 5, rtextPaint);
                }
            }

            //left side

            xPosition = (width / 2 - mMove) - iDensity;
            if (xPosition > getPaddingLeft()) {
                canvas.drawLine(xPosition, getPaddingTop(), xPosition, mDensity * ITEM_MAX_HEIGHT, linePaint);
                if (mCurrentValue - i >= mMinValue) {
                    float ld = (float) (MARGIN_ALPHA * mMinTextSize *  i * mDensity + 1 * mMove);
                    float scaleld = parabola(mWidth / 3f, ld);
                    float sizeld = (mMaxTextSize - mMinTextSize) * scaleld + mMinTextSize;
                    TextPaint ltextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                    ltextPaint.setTextSize(sizeld);
                    ltextPaint.setAlpha((int) ((MAX_TEXT_ALPHA - MIN_TEXT_ALPHA) * scaleld + MIN_TEXT_ALPHA));
                    float ltextWidth = Layout.getDesiredWidth("0", ltextPaint);
                    canvas.drawText(String.valueOf(mCurrentValue - i), countLeftStart(mCurrentValue - i, xPosition, ltextWidth), getHeight()  - ltextWidth + 5, ltextPaint);
                }
            }
            drawCount += 2 * mLineDivider * mDensity;
        }
        canvas.restore();
    }



    private void changeMoveAndValue() {
        int tValue = (int) (mMove / (mLineDivider * mDensity));
        if (Math.abs(tValue) > 0) {
            mCurrentValue += tValue;
            mMove -= tValue * mLineDivider * mDensity;
            if (mCurrentValue <= mMinValue || mCurrentValue > mMaxValue) {
                mCurrentValue = mCurrentValue <= mMinValue ? mMinValue : mMaxValue;
                mMove = 0;
                mScroller.forceFinished(true);
            }
            notifyValueChange();
        }
        postInvalidate();
    }

    private void countMoveEnd() {
        int roundMove = Math.round(mMove / (mLineDivider * mDensity));
        mCurrentValue = mCurrentValue + roundMove;
        mCurrentValue = mCurrentValue <= mMinValue ? mMinValue : mCurrentValue;
        mCurrentValue = mCurrentValue > mMaxValue ? mMaxValue : mCurrentValue;
        mLastX = 0;
        mMove = 0;
        notifyValueChange();
        postInvalidate();
    }

    private void countVelocityTracker(MotionEvent event) {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }

}
