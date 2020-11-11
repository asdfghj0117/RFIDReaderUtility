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
import com.favepc.reader.rfidreaderutility.object.DemoUR;

import java.util.ArrayList;

/**
 * Created by Bruce_Chiang on 2017/4/11.
 */

public class DemoURListAdapter extends ArrayAdapter<DemoUR> {

    private int mResourceId;

    public DemoURListAdapter(Context context, int resource, ArrayList<DemoUR> objects) {
        super(context, resource, objects);
        this.mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout _layout;
        DemoUR _demoUR = getItem(position);

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

        TextView tvPC = (TextView) _layout.findViewById(R.id.adapter_demoUR_pc);
        TextView tvEPC = (TextView) _layout.findViewById(R.id.adapter_demoUR_cpc);
        TextView tvCRC16 = (TextView) _layout.findViewById(R.id.adapter_demoUR_crc16);
        TextView tvCount = (TextView) _layout.findViewById(R.id.adapter_demoUR_count);
        TextView tvPercentage = (TextView) _layout.findViewById(R.id.adapter_demoUR_percentage);
        TextView tvR = (TextView) _layout.findViewById(R.id.adapter_demoUR_r);

        tvPC.setText(_demoUR.PC());
        tvEPC.setText(_demoUR.EPC());
        tvCRC16.setText(_demoUR.CRC16());
        tvCount.setText(_demoUR.Count());
        tvPercentage.setText(_demoUR.Percentage());
        tvR.setText(_demoUR.MemRead());

        return _layout;
    }
}
