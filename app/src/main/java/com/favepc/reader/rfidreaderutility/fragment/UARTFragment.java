package com.favepc.reader.rfidreaderutility.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.service.UartService;

import static com.favepc.reader.service.UartService.UART_NAME;

@SuppressLint("ValidFragment")
public class UARTFragment extends Fragment {

    private Context mContext;
    private Activity mActivity;
    private View mUARTView = null;
    private UARTMsgReceiver mUARTMsgReceiver;
    private TextView mTextViewMsgStatus, mTextViewMsg;
    private Spinner mSpinnerPort, mSpinnerBaudRate;
    private String[] mPortList = new String[] {};
    private String mPortName = "";
    private int mBaudRate = 38400;
    private Button mBtnSearch, mBtnConnect, mBtnDisconnect;

    public UARTFragment() {
        super();
    }

    public UARTFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mUARTMsgReceiver = new UARTMsgReceiver();
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_ACTION_DEVICE_SEARCH_CALLBACK));
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_MSG_CONNECTED));
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_MSG_CONNECT_FAIL));
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_ACTION_DISCONNECT));
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_MSG_DISCONNECTED));
        mContext.registerReceiver(mUARTMsgReceiver, new IntentFilter(UartService.UART_ACTION_CHANGE_INTERFACE));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mUARTMsgReceiver);
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mUARTView == null) {
            this.mUARTView = inflater.inflate(R.layout.fragment_uart, container, false);

            this.mSpinnerPort = this.mUARTView.findViewById(R.id.fragment_uart_spinner_select_port);
            this.mSpinnerPort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mPortName = mPortList[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            this.mSpinnerBaudRate = this.mUARTView.findViewById(R.id.fragment_uart_spinner_select_baudrate);
            ArrayAdapter<CharSequence> lunchList2 = ArrayAdapter.createFromResource(this.mContext, R.array.uart_baudrate, R.layout.spinner_style);
            this.mSpinnerBaudRate.setAdapter(lunchList2);
            this.mSpinnerBaudRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mBaudRate = Integer.parseInt(getResources().getStringArray(R.array.uart_baudrate)[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            this.mBtnSearch = mUARTView.findViewById(R.id.fragment_uart_select_search);
            this.mBtnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtnSearch.setEnabled(false);
                    mSpinnerPort.setAdapter(null);
                    searchUartDevice();
                }
            });

            this.mBtnConnect = mUARTView.findViewById(R.id.fragment_uart_select_connect);
            this.mBtnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtnConnect.setEnabled(false);
                    connectUartDevice();
                }
            });

            this.mBtnDisconnect = mUARTView.findViewById(R.id.fragment_uart_select_disconnect);
            this.mBtnDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    disconnectUartDevice();
                }
            });
            mBtnDisconnect.setEnabled(false);

            this.mTextViewMsg = mUARTView.findViewById(R.id.fragment_uart_select_msg);
            this.mTextViewMsgStatus = mUARTView.findViewById(R.id.uart_tvMsgStatus);
        }
        return this.mUARTView;
    }

    @Override
    public void onStart() {
        super.onStart();
        searchUartDevice();
    }

    public class UARTMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UartService.UART_ACTION_DEVICE_SEARCH_CALLBACK:
                    mBtnSearch.setEnabled(true);
                    mPortList = intent.getExtras().getStringArray(UartService.UART_PORTS);
                    String str = Build.DEVICE + " ";
                    if (mPortList.length == 0) {
                        mTextViewMsgStatus.setText(str.toUpperCase() + getString(R.string.uart_msg_no_support));
                        mTextViewMsg.setText(R.string.uart_no_device);
                        mBtnConnect.setEnabled(false);
                        mBtnSearch.setEnabled(false);
                    }
                    else {
                        mTextViewMsgStatus.setText(str.toUpperCase() + getString(R.string.uart_msg_support));
                        mBtnConnect.setEnabled(true);
                        ArrayAdapter<String> lunchList = new ArrayAdapter<String>(mContext, R.layout.spinner_style, mPortList);
                        mSpinnerPort.setAdapter(lunchList);
                    }
                    break;
                case UartService.UART_ACTION_CHANGE_INTERFACE:
                    mTextViewMsg.setText(getString(R.string.uart_msg_stop_service));
                    mActivity.sendBroadcast(new Intent(UartService.UART_ACTION_SERVICE_STOP));
                    break;
                case UartService.UART_MSG_CONNECTED:
                    mTextViewMsg.setText(intent.getExtras().getString(UART_NAME) + " " + getString(R.string.uart_select_connected));
                    ((MainActivity) mContext).interfaceCtrl(UartService.INTERFACE_UART, true);
                    mBtnDisconnect.setEnabled(true);
                    break;
                case UartService.UART_MSG_CONNECT_FAIL:
                    mBtnConnect.setEnabled(true);
                    mTextViewMsg.setText(intent.getExtras().getString(UART_NAME) + " " + getString(R.string.uart_select_connect_fail));
                    break;
                case UartService.UART_MSG_DISCONNECTED:
                    mTextViewMsg.setText(R.string.uart_select_disconnected);
                    mBtnConnect.setEnabled(true);
                    mBtnDisconnect.setEnabled(false);
                    ((MainActivity) mContext).interfaceCtrl(UartService.INTERFACE_UART, false);
                    break;
            }
        }
    }


    private void searchUartDevice() {
        Intent _intent = new Intent(UartService.UART_ACTION_DEVICE_SEARCH);
        mActivity.sendBroadcast(_intent);
    }

    /**
     * connect UART
     * @see UartService UART_ACTION_CONNECT case in MsgUartReceiver class
     */
    private void connectUartDevice() {
        Intent _intent = new Intent(UartService.UART_ACTION_CONNECT);
        _intent.putExtra(UART_NAME, mPortName);
        _intent.putExtra(UartService.UART_BUADRATE, mBaudRate);
        mActivity.sendBroadcast(_intent);
    }

    private void disconnectUartDevice() {
        Intent _intent = new Intent(UartService.UART_ACTION_DISCONNECT);
        mActivity.sendBroadcast(_intent);
    }
}
