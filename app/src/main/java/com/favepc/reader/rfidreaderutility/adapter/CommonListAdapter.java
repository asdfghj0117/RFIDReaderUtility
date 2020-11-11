package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.Common;

import java.util.ArrayList;

/**
 * Created by Bruce_Chiang on 2017/3/27.
 */

public class CommonListAdapter extends ArrayAdapter<Common> {

    private int mResourceId;

    public CommonListAdapter(Context context, int resource, ArrayList<Common> objects) {
        super(context, resource, objects);
        this.mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout _layout;
        Common _common = getItem(position);

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

        TextView _tvTx = (TextView) _layout.findViewById(R.id.adapter_common_tx);
        TextView _tvRx = (TextView) _layout.findViewById(R.id.adapter_common_rx);
        TextView _tvData = (TextView) _layout.findViewById(R.id.adapter_common_data);
        TextView _tvTime = (TextView) _layout.findViewById(R.id.adapter_common_time);

        if (_common.Title()) {
            _tvTx.setVisibility(View.INVISIBLE);
            _tvRx.setVisibility(View.VISIBLE);
        }
        else {
            _tvTx.setVisibility(View.VISIBLE);
            _tvRx.setVisibility(View.INVISIBLE);
        }
        _tvData.setText(_common.Data());
        _tvTime.setText(_common.Time());

        return _layout;
    }
}
