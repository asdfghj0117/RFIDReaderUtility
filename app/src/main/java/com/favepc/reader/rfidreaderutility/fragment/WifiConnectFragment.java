package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.CustomBaseKeyboard;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.service.NetService;

/**
 * Created by Bruce_Chiang on 2017/12/13.
 */

public class WifiConnectFragment extends DialogFragment {
    private EditText mEtAddress, mEtPort;
    private Button mCancel, mConnect;
    private Bundle mBundleDaata;
    private Context	mContext;
    private AppContext mAppContext;
    private View mWiFiDialogView = null;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;

    public WifiConnectFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public WifiConnectFragment(Bundle _Bundle, Context _Context) {
        mBundleDaata = _Bundle;
        mContext = _Context;
        this.mAppContext = (AppContext) _Context.getApplicationContext();
        //this.mCustomKeyboardManager = this.mAppContext.getKeyboard();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (mWiFiDialogView == null) {

            //---set the title for the dialog
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            //mContext = getDialog().getContext();

            this.mCustomKeyboardManager = new CustomKeyboardManager(getDialog());
            //custom keyboard select
            HexKeyboard = new CustomBaseKeyboard(mContext, R.xml.keyboard_digit) {
                @Override
                public void hideKeyboard(EditText etCurrent) {
                    mCustomKeyboardManager.hideSoftKeyboard(etCurrent);
                }

                @Override
                public boolean handleSpecialKey(EditText etCurrent, int primaryCode) {
                    return false;
                }
            };
            this.mWiFiDialogView= inflater.inflate(R.layout.fragment_wifi_connect_dialog, container);
            //---get the Button views---
            this.mCancel = (Button) mWiFiDialogView.findViewById(R.id.net_dialog_search_device_btncancel);
            this.mConnect = (Button) mWiFiDialogView.findViewById(R.id.net_dialog_search_device_btnconnect);
            this.mEtAddress = (EditText) mWiFiDialogView.findViewById(R.id.net_dialog_search_device_address);
            this.mEtPort = (EditText) mWiFiDialogView.findViewById(R.id.net_dialog_search_device_port);

            this.mEtAddress.setText(mBundleDaata.getString("ip"));
            this.mEtPort.setText(mBundleDaata.getString("port"));

            this.mCustomKeyboardManager.attachTo(this.mEtAddress, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtPort, HexKeyboard);

            // Button listener
            this.mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            this.mConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent _intent = new Intent(NetService.NET_ACTION_TCP_CONNECT);
                    _intent.putExtra(NetService.DEVICE_IP, mEtAddress.getText());
                    _intent.putExtra(NetService.DEVICE_PORT, mEtPort.getText());
                    getActivity().sendBroadcast(_intent);
                }
            });


        }


        return mWiFiDialogView;
    }
}
