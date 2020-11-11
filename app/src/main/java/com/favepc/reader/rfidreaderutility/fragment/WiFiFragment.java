package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.NETListAdapter;
import com.favepc.reader.rfidreaderutility.object.CustomBaseKeyboard;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.rfidreaderutility.object.NETDevice;
import com.favepc.reader.service.NetService;

import java.util.ArrayList;

/**
 * Created by Bruce_Chiang on 2017/3/9.
 */

@SuppressLint("ValidFragment")
public class WiFiFragment extends Fragment {

    private static final int START_WIFI_CONNECT = 2;

    private Context	mContext;
    private AppCompatActivity mActivity;
    private NetMsgReceiver mNetMsgReceiver;
    private View mWiFiView = null;
    private ArrayList<NETDevice> mNETDevices = new ArrayList<NETDevice>();
    private NETListAdapter mNETListAdapter;
    private CheckBox mCheckBoxSearchDevice, mCheckBoxAssignDevice;
    private TextView mTextViewMsg;
    private EditText mEditTextSearchDevice, mEditTextSearchTxPort, mEditTextSearchRxPort;
    private Button mBtnSearch, mBtnConnect, mBtnCancel;
    private EditText mEditTextAddress, mEditTextPort;
    private ProgressBar mProgressBar;
    private ListView mNEListView;


    private String mTcpAddr;
    private int mTcpPort;
    private boolean mTcpConnect = false;

    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private AppContext mAppContext;



    public WiFiFragment() {
        super();
    }
    public WiFiFragment(Context context, AppCompatActivity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mCustomKeyboardManager = this.mAppContext.getKeyboard();
        this.mActivity = (AppCompatActivity) context;
        this.mNetMsgReceiver = new NetMsgReceiver();

        this.mContext.registerReceiver(mNetMsgReceiver, new IntentFilter(NetService.NET_ACTION_CHANGE_INTERFACE));
        this.mContext.registerReceiver(mNetMsgReceiver, new IntentFilter(NetService.NET_ACTION_UDP_SEARCH_CALLBACK));
        this.mContext.registerReceiver(mNetMsgReceiver, new IntentFilter(NetService.NET_ACTION_TCP_CONNECTED));
        this.mContext.registerReceiver(mNetMsgReceiver, new IntentFilter(NetService.NET_ACTION_TCP_DISCONNECTED));

        this.mActivity.sendBroadcast(new Intent(NetService.NET_ACTION_SERVICE_START));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mWiFiView == null) {
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

            this.mWiFiView = inflater.inflate(R.layout.fragment_wifi, container, false);
            this.mNETListAdapter = new NETListAdapter(this.mContext, R.layout.adapter_netdevice, this.mNETDevices);
            this.mNEListView = (ListView)mWiFiView.findViewById(R.id.net_lvDevice);
            this.mNEListView.setAdapter(this.mNETListAdapter);
            this.mNEListView.setOnItemClickListener(deviceClickListener);

            this.mTextViewMsg = (TextView) mWiFiView.findViewById(R.id.net_tvMsg);

            this.mCheckBoxSearchDevice = (CheckBox) mWiFiView.findViewById(R.id.net_title_search);
            this.mCheckBoxSearchDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        mEditTextSearchDevice.setEnabled(true);
                        mEditTextSearchTxPort.setEnabled(true);
                        mEditTextSearchRxPort.setEnabled(true);
                        mBtnSearch.setEnabled(true);
                    }
                    else {
                        mEditTextSearchDevice.setEnabled(false);
                        mEditTextSearchTxPort.setEnabled(false);
                        mEditTextSearchRxPort.setEnabled(false);
                        mBtnSearch.setEnabled(false);
                    }
                }
            });
            this.mEditTextSearchDevice = (EditText) mWiFiView.findViewById(R.id.net_title_search_device);
            this.mEditTextSearchTxPort = (EditText) mWiFiView.findViewById(R.id.net_title_tx_port);
            this.mEditTextSearchRxPort = (EditText) mWiFiView.findViewById(R.id.net_title_rx_port);
            this.mBtnSearch = (Button) mWiFiView.findViewById(R.id.net_device_search);
            this.mBtnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBtnSearch.setEnabled(false);
                    mNETListAdapter.clear();
                    mProgressBar.setVisibility(View.VISIBLE);
                    searchWiFiDevice();
                }
            });


            this.mCheckBoxAssignDevice = (CheckBox) mWiFiView.findViewById(R.id.net_title_assign);
            this.mCheckBoxAssignDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        mEditTextAddress.setEnabled(true);
                        mEditTextPort.setEnabled(true);
                        if (!mTcpConnect)
                            mBtnConnect.setEnabled(true);
                    }
                    else {
                        mEditTextAddress.setEnabled(false);
                        mEditTextPort.setEnabled(false);
                        mBtnConnect.setEnabled(false);
                    }
                }
            });

            this.mEditTextAddress = (EditText) mWiFiView.findViewById(R.id.net_address);
            this.mEditTextPort = (EditText) mWiFiView.findViewById(R.id.net_port);
            this.mCustomKeyboardManager.attachTo(this.mEditTextAddress, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEditTextPort, HexKeyboard);

            this.mBtnConnect = (Button) mWiFiView.findViewById(R.id.net_device_connect);
            this.mBtnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connectWiFiDevice();
                }
            });
            this.mBtnCancel = (Button) mWiFiView.findViewById(R.id.net_device_disconnect);
            this.mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disconnectWiFiDevice();
                }
            });

            this.mProgressBar = (ProgressBar) mWiFiView.findViewById(R.id.net_device_progressBar);
            this.mProgressBar.setVisibility(View.GONE);

            this.mEditTextSearchDevice.setEnabled(false);
            this.mEditTextSearchTxPort.setEnabled(false);
            this.mEditTextSearchRxPort.setEnabled(false);
            this.mBtnSearch.setEnabled(false);

            this.mEditTextAddress.setEnabled(false);
            this.mEditTextPort.setEnabled(false);
            this.mBtnConnect.setEnabled(false);
            this.mBtnCancel.setEnabled(false);
        }
        return mWiFiView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mNetMsgReceiver);
        mContext = null;
    }

    private void searchWiFiDevice() {
        Intent _intent = new Intent(NetService.NET_ACTION_UDP_SEARCH);
        _intent.putExtra(NetService.UDP_DEVICE_NAME, mEditTextSearchDevice.getText().toString());
        _intent.putExtra(NetService.UDP_TX_PORT, mEditTextSearchTxPort.getText().toString());
        _intent.putExtra(NetService.UDP_RX_PORT, mEditTextSearchRxPort.getText().toString());
        mActivity.sendBroadcast(_intent);
    }

    private void connectWiFiDevice() {
        Intent _intent = new Intent(NetService.NET_ACTION_TCP_CONNECT);
        _intent.putExtra(NetService.TCP_ADDRESS, mEditTextAddress.getText().toString());
        _intent.putExtra(NetService.TCP_PORT, mEditTextPort.getText().toString());
        mActivity.sendBroadcast(_intent);
    }

    private void disconnectWiFiDevice() {
        Intent _intent = new Intent(NetService.NET_ACTION_TCP_DISCONNECT);
        _intent.putExtra(NetService.TCP_ADDRESS, mTcpAddr);
        _intent.putExtra(NetService.TCP_PORT, mTcpPort);
        mActivity.sendBroadcast(_intent);
    }

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        TextView _tvIP, _tvPort;
        String ip, port;
        EditText _etAddr, _etPort;
        AlertDialog.Builder _builder;
        AlertDialog _dialog;
        CustomBaseKeyboard _hexKeyboard;
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            _tvIP = (TextView)((RelativeLayout)view).findViewById(R.id.adapter_net_deviceIP);
            _tvPort = (TextView)((RelativeLayout)view).findViewById(R.id.adapter_net_devicePort);
            ip = _tvIP.getText().toString();
            port = _tvPort.getText().toString();

            /*Intent _intent = new Intent(mContext, WiFiConnectDialogActivity.class);
            _intent.putExtra("ip", ip);
            _intent.putExtra("port", port);

            startActivityForResult(_intent, START_WIFI_CONNECT);*/

            Bundle bundle = new Bundle();
            bundle.putString("ip", ip);
            bundle.putString("port", port);
            WifiConnectFragment wifiConnectDialog = new WifiConnectFragment(bundle, mContext);
            wifiConnectDialog.setCancelable(false);
            //wifiConnectDialog.show(mActivity.getFragmentManager(), "WifiConnect Dialog");
            wifiConnectDialog.show(mActivity.getSupportFragmentManager(), "WifiConnect Dialog");

            /*final View _v = LayoutInflater.from(mActivity).inflate(R.layout.adapter_netdevice_dialog, null);
            _etAddr = (EditText) (_v.findViewById(R.id.net_dialog_search_device_address));
            _etPort = (EditText) (_v.findViewById(R.id.net_dialog_search_device_port));

            _hexKeyboard = new CustomBaseKeyboard(_v.getContext(), R.xml.keyboard_digit) {

                @Override
                public void hideKeyboard(EditText etCurrent) {

                }

                @Override
                public boolean handleSpecialKey(EditText etCurrent, int primaryCode) {
                    return false;
                }
            };
            mCustomKeyboardManager.attachTo(_etAddr, _hexKeyboard);
            mCustomKeyboardManager.attachTo(_etPort, _hexKeyboard);
            _etAddr.setText(ip);
            _etPort.setText(port);

            _builder = new AlertDialog.Builder(mActivity);
            _builder.setView(_v);
            _builder.setNegativeButton(R.string.net_action_disconnect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            _builder.setPositiveButton(R.string.net_action_connect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent _intent = new Intent(NetService.NET_ACTION_TCP_CONNECT);
                    _intent.putExtra(NetService.DEVICE_IP, _etAddr.getText());
                    _intent.putExtra(NetService.DEVICE_PORT, _etPort.getText());
                    mActivity.sendBroadcast(_intent);
                }
            });
            _dialog = _builder.create();
            _dialog.setTitle(R.string.net_action_connect_device);
            _dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            _dialog.show();*/
        }
    };


    public class NetMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NetService.NET_ACTION_CHANGE_INTERFACE:
                    mTextViewMsg.setText(getString(R.string.net_msg_stop_service));
                    mActivity.sendBroadcast(new Intent(NetService.NET_ACTION_SERVICE_STOP));
                    break;
                case NetService.NET_ACTION_UDP_SEARCH_CALLBACK:
                    mBtnSearch.setEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    if (intent.getExtras().getString(NetService.DEVICE_MSG).equals("OK")) {
                        mNETListAdapter.add(new NETDevice(
                                "",
                                intent.getExtras().getString(NetService.DEVICE_IP),
                                "",
                                intent.getExtras().getString(NetService.DEVICE_PORT),
                                0));
                    }
                    else {
                        mNETListAdapter.add(new NETDevice("No devices found"));
                    }
                    break;
                case NetService.NET_ACTION_TCP_CONNECTED:
                    mTcpAddr = intent.getExtras().getString(NetService.TCP_ADDRESS);
                    mTcpPort = intent.getExtras().getInt(NetService.TCP_PORT);
                    mTcpConnect = true;
                    mBtnConnect.setEnabled(false);
                    mBtnCancel.setEnabled(true);
                    mTextViewMsg.setText("Connected to " + mTcpAddr + ":" + mTcpPort);
                    ((MainActivity) mContext).interfaceCtrl(NetService.INTERFACE_NET, true);

                    break;
                case NetService.NET_ACTION_TCP_DISCONNECTED:
                    mTcpConnect = false;
                    mBtnCancel.setEnabled(false);
                    if (mCheckBoxAssignDevice.isChecked())
                        mBtnConnect.setEnabled(true);
                    mTextViewMsg.setText("Disconnected");
                    ((MainActivity) mContext).interfaceCtrl(NetService.INTERFACE_NET, false);
                    break;
            }
        }
    }
}
