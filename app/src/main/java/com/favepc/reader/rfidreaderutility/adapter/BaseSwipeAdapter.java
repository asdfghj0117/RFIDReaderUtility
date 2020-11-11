package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.favepc.reader.rfidreaderutility.implments.SwipeItemMangerImpl;
import com.favepc.reader.rfidreaderutility.interfaces.SwipeAdapterInterface;
import com.favepc.reader.rfidreaderutility.interfaces.SwipeItemMangerInterface;
import com.favepc.reader.rfidreaderutility.object.Attributes;
import com.favepc.reader.rfidreaderutility.object.Customize;
import com.favepc.reader.rfidreaderutility.object.SwipeLayout;

import java.util.List;

public abstract class BaseSwipeAdapter extends ArrayAdapter<Customize> implements SwipeItemMangerInterface, SwipeAdapterInterface {

    private int mResourceId;
    protected SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);

    public BaseSwipeAdapter(@NonNull Context context, int resource, @NonNull List<Customize> objects) {
        super(context, resource, objects);
        this.mResourceId = resource;
    }

    /**
     * return the {@link com.favepc.reader.rfidreaderutility.object.SwipeLayout} resource id, int the view item.
     * @param position
     * @return
     */
    public abstract int getSwipeLayoutResourceId(int position);

    /**
     * generate a new view item.
     * Never bind SwipeListener or fill values here, every item has a chance to fill value or bind
     * listeners in fillValues.
     * to fill it in {@code fillValues} method.
     * @param position
     * @param parent
     * @return
     */
    public abstract View generateView(int position, ViewGroup parent);

    /**
     * fill values or bind listeners to the view.
     * @param position
     * @param convertView
     */
    public abstract void fillValues(int position, View convertView);

    @Override
    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        //View v = convertView;
        if(convertView == null){
            convertView = generateView(position, parent);
        }
        mItemManger.bind(convertView, position);
        fillValues(position, convertView);
        return convertView;
    }

    @Override
    public void openItem(int position) {
        mItemManger.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManger.closeItem(position);
    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {
        mItemManger.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManger.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManger.getOpenItems();
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return mItemManger.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {
        mItemManger.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManger.isOpen(position);
    }

    @Override
    public Attributes.Mode getMode() {
        return mItemManger.getMode();
    }

    @Override
    public void setMode(Attributes.Mode mode) {
        mItemManger.setMode(mode);
    }
}
