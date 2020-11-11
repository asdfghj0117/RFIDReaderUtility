package com.favepc.reader.rfidreaderutility.adapter;


import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.UCode8;
import com.favepc.reader.rfidreaderutility.object.UCode8Mode;

import java.util.ArrayList;

/**
 * Brand identifier(BI) adapter for UCode8 data
 */
public class UCode8ListAdapter extends ArrayAdapter<UCode8> {

    private int mResourceId;
    private UCode8Mode mMode = UCode8Mode.BI_MODE1;

    /**
     * Public constructor
     * @param context The current context. This value cannot be null.
     * @param resource The resource ID for a layout file containing a TextView to use when instantiating views.
     * @param objects The UCode8 array objects to represent in the ListView. This value cannot be null.
     * @param mode The mode for three kinds of show view.
     */
    public UCode8ListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UCode8> objects, UCode8Mode mode) {
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

        TextView tvTx = _layout.findViewById(R.id.adapter_uCode8_tx);
        TextView tvRx = _layout.findViewById(R.id.adapter_uCode8_rx);
        TextView tvData =  _layout.findViewById(R.id.adapter_uCode8_data);

        LinearLayout llPC = _layout.findViewById(R.id.adapter_uCode8_black_pc);
        LinearLayout llEPC = _layout.findViewById(R.id.adapter_uCode8_black_epc);
        LinearLayout llRN16 = _layout.findViewById(R.id.adapter_uCode8_black_rn16);
        LinearLayout llTID = _layout.findViewById(R.id.adapter_uCode8_black_tid);;
        TextView tvPC =  _layout.findViewById(R.id.adapter_uCode8_pc);
        TextView tvEPC =  _layout.findViewById(R.id.adapter_uCode8_cpc);
        TextView tvRN16 = _layout.findViewById(R.id.adapter_uCode8_rn16);
        TextView tvTID = _layout.findViewById(R.id.adapter_uCode8_tid);;


        //TX
        if (!_uCode8.Title()) {
            tvTx.setVisibility(View.VISIBLE);
            tvRx.setVisibility(View.INVISIBLE);
            tvData.setVisibility(View.VISIBLE);
            tvData.setText(_uCode8.Data());

            llPC.setVisibility(View.GONE);
            llEPC.setVisibility(View.GONE);
            llRN16.setVisibility(View.GONE);
            llTID.setVisibility(View.GONE);
            tvPC.setVisibility(View.GONE);
            tvEPC.setVisibility(View.GONE);
            tvRN16.setVisibility(View.GONE);
            tvTID.setVisibility(View.GONE);
        }
        //RX
        else {
            tvTx.setVisibility(View.INVISIBLE);
            tvRx.setVisibility(View.VISIBLE);

            if (_uCode8.Data() == null) {
                tvData.setVisibility(View.GONE);

                llPC.setVisibility(View.VISIBLE);
                llEPC.setVisibility(View.VISIBLE);
                tvPC.setVisibility(View.VISIBLE);
                tvEPC.setVisibility(View.VISIBLE);

                tvPC.setText(_uCode8.PC());

                switch (mMode) {
                    case BI_MODE1:
                        llRN16.setVisibility(View.VISIBLE);
                        tvRN16.setVisibility(View.VISIBLE);
                        llTID.setVisibility(View.GONE);
                        tvTID.setVisibility(View.GONE);
                        tvEPC.setText(_uCode8.EPC());
                        tvRN16.setText(_uCode8.RN16());
                        break;
                    case BI_MODE2:
                        llRN16.setVisibility(View.GONE);
                        tvRN16.setVisibility(View.GONE);
                        llTID.setVisibility(View.GONE);
                        tvTID.setVisibility(View.GONE);
                        SpannableString text = new SpannableString(_uCode8.EPC().toUpperCase());
                        text.setSpan(new ForegroundColorSpan(Color.RED), _uCode8.EPC().length() - 4, _uCode8.EPC().length() , 0);
                        tvEPC.setText(text);
                        break;
                    case EPC_TID:
                        llRN16.setVisibility(View.VISIBLE);
                        llTID.setVisibility(View.VISIBLE);
                        tvRN16.setVisibility(View.VISIBLE);
                        tvTID.setVisibility(View.VISIBLE);
                        tvEPC.setText(_uCode8.EPC());
                        tvRN16.setText(_uCode8.RN16());
                        tvTID.setText(_uCode8.TID());
                        break;
                }
            }
            else {
                tvData.setVisibility(View.VISIBLE);
                tvData.setText(_uCode8.Data());
                llPC.setVisibility(View.GONE);
                llEPC.setVisibility(View.GONE);
                llTID.setVisibility(View.GONE);
                llRN16.setVisibility(View.GONE);
                tvPC.setVisibility(View.GONE);
                tvEPC.setVisibility(View.GONE);
                tvRN16.setVisibility(View.GONE);
                tvTID.setVisibility(View.GONE);
            }
        }


        return _layout;
    }
}
