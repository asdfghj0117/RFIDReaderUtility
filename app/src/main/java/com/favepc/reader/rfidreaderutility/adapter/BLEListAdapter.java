package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.BLEDevice;

import java.util.ArrayList;

/**
 * Created by Bruce_Chiang on 2017/3/13.
 */

public class BLEListAdapter extends ArrayAdapter<BLEDevice> {

    private int mResourceId;

    public BLEListAdapter(Context context, int resource, ArrayList<BLEDevice> obj) {
        super(context, resource, obj);
        this.mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RelativeLayout _layout;
        BLEDevice _bleDev = getItem(position);

        if (convertView == null)
        {
            _layout = new RelativeLayout(getContext());
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(mResourceId, _layout, true);
        }
        else
        {
            _layout = (RelativeLayout)convertView;
        }

        ImageView ivImage = (ImageView) _layout.findViewById(R.id.adapter_ble_image);
        TextView tvName = (TextView) _layout.findViewById(R.id.adapter_ble_name);
        TextView tvAddress = (TextView) _layout.findViewById(R.id.adapter_ble_address);
        TextView tvRssi = (TextView) _layout.findViewById(R.id.adapter_ble_rssi);

        if (_bleDev.getName() != null && _bleDev.getName().equals("READER"))
            _bleDev.setImage(R.mipmap.ic_phone_android_black_48dp);
        else
            _bleDev.setImage(R.mipmap.ic_launcher);

        ivImage.setImageResource(_bleDev.getImage());
        tvName.setText(_bleDev.getName());
        tvAddress.setText(_bleDev.getAddress());
        tvRssi.setText(_bleDev.getRSSI());

        return _layout;
    }
}
