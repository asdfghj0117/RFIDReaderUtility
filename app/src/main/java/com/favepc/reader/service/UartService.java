package com.favepc.reader.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

/**
 * Created by Bruce_Chiang on 2019/5/09.
 */
public class UartService extends Service {

    public static final String UART_ACTION_SERVICE_START    = "UART_ACTION_SERVICE_START";
    public static final String UART_ACTION_SERVICE_STOP     = "UART_ACTION_SERVICE_STOP";
    public static final String UART_ACTION_DEVICE_SEARCH    = "UART_ACTION_DEVICE_SEARCH";
    public static final String UART_ACTION_DEVICE_SEARCH_CALLBACK     = "UART_ACTION_DEVICE_SEARCH_CALLBACK";
    public static final String UART_ACTION_CONNECT          = "UART_ACTION_CONNECT";
    public static final String UART_MSG_CONNECTED           = "UART_MSG_CONNECTED";
    public static final String UART_MSG_CONNECT_FAIL        = "UART_MSG_CONNECT_FAIL";
    public static final String UART_ACTION_SEND_DATA	    = "UART_ACTION_SEND_DATA";
    public static final String UART_ACTION_RECEIVE_DATA	    = "UART_ACTION_RECEIVE_DATA";
    public static final String UART_ACTION_DISCONNECT       = "UART_ACTION_DISCONNECT";
    public static final String UART_MSG_DISCONNECTED        = "UART_MSG_DISCONNECTED";
    public static final String UART_ACTION_CHANGE_INTERFACE = "UART_ACTION_CHANGE_INTERFACE";

    public static final String INTERFACE_UART   = "INTERFACE_UART";
    public static final String UART_NAME        = "UART_NAME";
    public static final String UART_PORTS       = "UART_PORTS";
    public static final String UART_BUADRATE    = "UART_BUADRATE";
    public static final String UART_SEND        = "UART_SEND";
    public static final String UART_RECEIVE     = "UART_RECEIVE";

    private MsgUartReceiver     mMsgUartReceiver;

    private static boolean 	    mIsOpened;
    private SerialPort          mSerialPort;
    private SerialPortFinder    mSerialPortFinder;
    private OutputStream        mOutputStream;
    private InputStream         mInputStream;
    private Thread 	            mReceiveThread;
    private boolean             mIsThreadStop = true;

    static {
        mIsOpened = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mSerialPortFinder = new SerialPortFinder();
        this.mMsgUartReceiver = new MsgUartReceiver();
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_SERVICE_START));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_SERVICE_STOP));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_DEVICE_SEARCH));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_CONNECT));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_DISCONNECT));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_SEND_DATA));
        registerReceiver(mMsgUartReceiver, new IntentFilter(UART_ACTION_RECEIVE_DATA));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.mReceiveThread != null) {
            this.mReceiveThread.interrupt();
            this.mReceiveThread = null;
        }

        unregisterReceiver(mMsgUartReceiver);
    }

    public String[] getAllDevicesPath() {
        return this.mSerialPortFinder.getAllDevicesPath();
    }

    private void open(String port, int baudRate) throws Exception {
        if (mIsOpened) {
            throw new Exception(port + " is already opened");
        }
        else {
            this.mSerialPort =  new SerialPort(new File(port), baudRate, 0);
            this.mOutputStream = this.mSerialPort.getOutputStream();
            this.mInputStream = this.mSerialPort.getInputStream();

            mIsOpened = true;
        }
    }

    private void close() throws Exception {
        if (mIsOpened) {
            this.mOutputStream.close();
            this.mInputStream.close();
            this.mSerialPort.close();
            this.mOutputStream = null;
            this.mInputStream = null;
            this.mSerialPort = null;

            mIsOpened = false;
        }
    }


    public class MsgUartReceiver extends BroadcastReceiver {

        String name;
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UART_ACTION_SERVICE_START:
                    break;
                case UART_ACTION_SERVICE_STOP:
                    if (mReceiveThread != null) {
                        mReceiveThread.interrupt();
                        mIsThreadStop = true;
                        mReceiveThread = null;
                    }
                    try {
                        close();
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
                case UART_ACTION_DEVICE_SEARCH:
                    new Thread(new SearchRunnable(intent)).start();
                    break;
                case UART_ACTION_CONNECT:
                    try {
                        name = intent.getExtras().getString(UART_NAME);
                        open(name, intent.getExtras().getInt(UART_BUADRATE));
                        if (mIsOpened) {
                            Intent _intent = new Intent(UART_MSG_CONNECTED);
                            _intent.putExtra(UART_NAME, name);
                            sendBroadcast(_intent);
                            mIsThreadStop = false;
                            mReceiveThread = new Thread(receivePlayRunnable);
                            mReceiveThread.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent _intent = new Intent(UART_MSG_CONNECT_FAIL);
                        _intent.putExtra(UART_NAME, name);
                        sendBroadcast(_intent);
                    }
                    break;
                case UART_ACTION_DISCONNECT:
                    try {
                        close();
                        Intent _intent = new Intent(UART_MSG_DISCONNECTED);
                        sendBroadcast(_intent);
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
                case UART_ACTION_SEND_DATA:
                    try {
                        mOutputStream.write(intent.getExtras().getByteArray(UART_SEND));
                    } catch (IOException e1) { e1.printStackTrace();}

                    break;
                /*case UART_ACTION_RECEIVE_DATA:
                    break;*/
            }
        }
    }



    private Runnable receivePlayRunnable = new Runnable() {
        byte[] temp = new byte[512];
        @Override
        public void run() {
            while (!mIsThreadStop) {
                try {
                    if (mInputStream == null) return;
                    int size = mInputStream.read(temp, 0, temp.length);
                    if (size > 0) {
                        byte[] buff = new byte[size];
                        System.arraycopy(temp, 0, buff, 0, size);
                        sendBroadcast(UART_ACTION_RECEIVE_DATA, buff);
                    }
                    Thread.sleep(4);
                } catch (Throwable e){
                    e.printStackTrace();}
            }
        }
    };



    private void sendBroadcast(String action, @NonNull byte[] data) {
        Intent i = new Intent(action);
        i.putExtra(UART_RECEIVE, data);
        sendBroadcast(i);
    }

    class SearchRunnable implements Runnable {

        private Intent _Intent;
        public SearchRunnable(Intent _intent) {
            _Intent = _intent;
        }

        @Override
        public void run() {


            String[] sPortList = mSerialPortFinder.getAllDevicesPath();

            Intent _intent = new Intent(UART_ACTION_DEVICE_SEARCH_CALLBACK);
            _intent.putExtra(UART_PORTS, sPortList);

            sendBroadcast(_intent);
        }
    }
}
