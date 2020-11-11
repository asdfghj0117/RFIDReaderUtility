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
import androidx.annotation.Nullable;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.NETDevice;

import java.util.List;

/**
 * Created by Bruce_Chiang on 2017/11/2.
 */

public class NETListAdapter extends ArrayAdapter<NETDevice> {

    private int mResourceId;

    public NETListAdapter(Context context, int resource, List<NETDevice> objects) {
        super(context, resource, objects);
        this.mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RelativeLayout _layout;
        NETDevice _netDev = getItem(position);

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

        ImageView ivImage = (ImageView) _layout.findViewById(R.id.adapter_net_image);
        TextView tvDeviceName = (TextView) _layout.findViewById(R.id.adapter_net_deviceName);
        TextView tvDeviceAddress = (TextView) _layout.findViewById(R.id.adapter_net_deviceIP);
        TextView tvDeviceMAC = (TextView) _layout.findViewById(R.id.adapter_net_deviceMAC);
        TextView tvDevicePort = (TextView) _layout.findViewById(R.id.adapter_net_devicePort);

        switch(_netDev.getRSSI()) {
            case 1: _netDev.setImage(R.drawable.ic_wifi_64x1); break;
            case 2: _netDev.setImage(R.drawable.ic_wifi_64x2); break;
            case 3: _netDev.setImage(R.drawable.ic_wifi_64x3); break;
            case 4: _netDev.setImage(R.drawable.ic_wifi_64x4); break;
        }

        ivImage.setImageResource(_netDev.getImage());
        tvDeviceName.setText(_netDev.getName());
        tvDeviceAddress.setText(_netDev.getAddress());
        tvDeviceMAC.setText(_netDev.getMAC());
        tvDevicePort.setText(_netDev.getPort());

        return _layout;
    }
}
