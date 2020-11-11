package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.UCode8;
import com.favepc.reader.rfidreaderutility.object.UCode8Mode;

import java.util.ArrayList;

public class UCode8ListTxRxAdapter extends ArrayAdapter<UCode8> {
    private int mResourceId;
    private UCode8Mode mMode = UCode8Mode.BI_MODE1;

    public UCode8ListTxRxAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UCode8> objects, UCode8Mode mode) {
        super(context, resource, objects);
        this.mResourceId = resource;
        this.mMode = mode;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        RelativeLayout _layout;
        UCode8 _uCode8 = getItem(position);

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

        TextView tvPC = (TextView) _layout.findViewById(R.id.adapter_uCode8_pc);
        TextView tvEPC = (TextView) _layout.findViewById(R.id.adapter_uCode8_cpc);
        tvPC.setText(_uCode8.PC());
        //tvEPC.setText(_uCode8.EPC());

        switch (mMode) {
            case BI_MODE1:
                tvEPC.setText(_uCode8.EPC());
                TextView tvRN16 = (TextView) _layout.findViewById(R.id.adapter_uCode8_rn16);
                tvRN16.setText(_uCode8.RN16());
                break;
            case BI_MODE2:
                SpannableString text = new SpannableString(_uCode8.EPC().toUpperCase());
                text.setSpan(new ForegroundColorSpan(Color.RED), _uCode8.EPC().length() - 4, _uCode8.EPC().length() , 0);
                tvEPC.setText(text);
                break;
            case EPC_TID:
                tvEPC.setText(_uCode8.EPC());
                TextView tvRN16_3 = (TextView) _layout.findViewById(R.id.adapter_uCode8_rn16);
                tvRN16_3.setText(_uCode8.RN16());
                TextView tvTID = (TextView) _layout.findViewById(R.id.adapter_uCode8_tid);
                tvTID.setText(_uCode8.TID());
                break;
        }

        return _layout;
    }
}
