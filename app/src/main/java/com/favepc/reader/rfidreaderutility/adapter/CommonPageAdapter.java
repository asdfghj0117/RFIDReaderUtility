package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

;

/**
 * Created by Bruce_Chiang on 2017/3/27.
 */

public class CommonPageAdapter extends PagerAdapter {
    private Context mContext;
    private List<View> mListViews;
    private int mPosition;
    private int mCurrentPosition = -1;

    public CommonPageAdapter(Context context, List<View> listViews, int position) {
        mContext = context;
        mListViews = listViews;
        mPosition = position;
    }

    @Override
    public int getCount() {
        return this.mListViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        ((ViewPager) container).removeView(mListViews.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(mListViews.get(position));
        return mListViews.get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            View fragment = (View) object;
            WrapContentViewPager pager = (WrapContentViewPager) container;
            if (fragment != null ) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment);
            }
        }
    }
}

