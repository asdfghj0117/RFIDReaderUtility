package com.favepc.reader.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Bruce_Chiang on 2017/11/2.
 */

public class NetService extends Service {

    public static final String NET_ACTION_SERVICE_START     = "NET_ACTION_SERVICE_START";
    public static final String NET_ACTION_SERVICE_STOP      = "NET_ACTION_SERVICE_STOP";
    public static final String NET_ACTION_UDP_SEARCH        = "NET_ACTION_UDP_SEARCH";
    public static final String NET_ACTION_UDP_SEARCH_CALLBACK   = "NET_ACTION_UDP_SEARCH_CALLBACK";

    public static final String NET_ACTION_TCP_CONNECT	    = "NET_ACTION_TCP_CONNECT";
    public static final String NET_ACTION_TCP_DISCONNECT	= "NET_ACTION_TCP_DISCONNECT";

    public static final String NET_ACTION_TCP_CONNECTED	    = "NET_ACTION_TCP_CONNECTED";
    public static final String NET_ACTION_TCP_DISCONNECTED	= "NET_ACTION_TCP_DISCONNECTED";
    public static final String NET_ACTION_TCP_SEND_DATA	    = "NET_ACTION_TCP_SEND_DATA";
    public static final String NET_ACTION_TCP_RECEIVE_DATA	= "NET_ACTION_TCP_RECEIVE_DATA";
    public static final String NET_ACTION_CHANGE_INTERFACE  = "NET_ACTION_CHANGE_INTERFACE";
    public static final String NET_ACTION_ERROR             = "NET_ACTION_ERROR";

    public static final String INTERFACE_NET = "INTERFACE_NET";

    public static final String STRING_DATA = "STRING_DATA";
    public static final String BYTES_DATA = "BYTES_DATA";

    public static final String UDP_DEVICE_NAME = "UDP_DEVICE_NAME";
    public static final String UDP_TX_PORT = "UDP_TX_PORT";
    public static final String UDP_RX_PORT = "UDP_RX_PORT";

    public static final String TCP_ADDRESS = "TCP_ADDRESS";
    public static final String TCP_PORT = "TCP_PORT";
    public static final String TCP_DATA = "TCP_DATA";

    public static final String DEVICE_MSG = "DEVICE_MSG";
    public static final String DEVICE_IP = "DEVICE_IP";
    public static final String DEVICE_PORT = "DEVICE_PORT";

    private MsgNetReceiver      mMsgNetReceiver;
    private UdpService          mUdpService = null;
    private TcpService          mTcpService = null;
    private TcpEventHandler     mTcpEventHandler;

    private boolean             mIsUdpService = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mMsgNetReceiver = new MsgNetReceiver();
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_SERVICE_START));
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_SERVICE_STOP));
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_UDP_SEARCH));
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_TCP_CONNECT));
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_TCP_DISCONNECT));
        registerReceiver(mMsgNetReceiver, new IntentFilter(NET_ACTION_TCP_SEND_DATA));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MsgNetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NET_ACTION_SERVICE_START:
                    if (!mIsUdpService) {
                        mIsUdpService = true;
                        mUdpService = new UdpService();
                    }
                    break;
                case NET_ACTION_SERVICE_STOP:
                    mIsUdpService = false;
                    break;

                case NET_ACTION_UDP_SEARCH:
                    new Thread(new SearchRunnable(intent)).start();
                    break;

                case NET_ACTION_TCP_CONNECT:
                    new Thread(new ConnectRunnable(intent)).start();

                    break;
                case NET_ACTION_TCP_DISCONNECT:
                    mTcpService.Close();
                    sendBroadcast(new Intent(NET_ACTION_TCP_DISCONNECTED));
                    break;

                case NET_ACTION_TCP_SEND_DATA:
                    if (mTcpService.isConnected()) {
                        byte[] _data = intent.getExtras().getByteArray(BYTES_DATA);
                        mTcpService.Send(_data);
                    }
                    break;
            }
        }
    }

    /**
     *
     */
    class SearchRunnable implements Runnable {

        private Intent _Intent;
        public SearchRunnable(Intent _intent) {
            _Intent = _intent;
        }

        @Override
        public void run() {
            String _name = _Intent.getExtras().getString(UDP_DEVICE_NAME);
            String _tp = _Intent.getExtras().getString(UDP_TX_PORT);
            String _rp = _Intent.getExtras().getString(UDP_RX_PORT);

            mUdpService.DestinationPort(Integer.parseInt(_tp));
            mUdpService.LocalPort(Integer.parseInt(_rp));

            mUdpService.Send(_name);
            mUdpService.SetTimeOut(2000);

            DatagramPacket _dp = mUdpService.Receive();

            Intent _intent = new Intent(NET_ACTION_UDP_SEARCH_CALLBACK);

            if(new String(_dp.getData(), 0, _dp.getLength()).equals(_name)) {
                _intent.putExtra(DEVICE_MSG, "OK");
                _intent.putExtra(DEVICE_IP, _dp.getAddress());
                _intent.putExtra(DEVICE_PORT, _dp.getPort());
            }
            else {
                _intent.putExtra(DEVICE_MSG, "NULL");
            }
            sendBroadcast(_intent);
        }
    }

    public class ConnectRunnable implements Runnable {

        private Intent _Intent;
        public ConnectRunnable(Intent _intent) {
            _Intent = _intent;
        }

        @Override
        public void run() {
            String address = _Intent.getExtras().getString(TCP_ADDRESS);
            int port = Integer.parseInt(_Intent.getExtras().getString(TCP_PORT));
            mTcpService = new TcpService(address, port);
            mTcpService.Connect(2000);

            Intent _intent;
            if (mTcpService.isConnected()) {
                _intent = new Intent(NET_ACTION_TCP_CONNECTED);
                _intent.putExtra(TCP_ADDRESS, address);
                _intent.putExtra(TCP_PORT, port);
            }
            else {
                _intent = new Intent(NET_ACTION_TCP_DISCONNECTED);
            }
            sendBroadcast(_intent);
        }
    }


    private TcpEventHandler getTcpDataReceived() {

        return mTcpEventHandler = new TcpEventHandler() {
            @Override
            protected void onDataReceived(byte[] bs) {
                sendBroadcast(TCP_DATA, bs);
            }
        };
    }


    private void sendBroadcast(String action, @NonNull byte[] data) {
        Intent i = new Intent(action);
        i.putExtra(BYTES_DATA, data);
        sendBroadcast(i);
    }

    private void sendBroadcast(String action, @NonNull String data) {
        Intent i = new Intent(action);
        i.putExtra(STRING_DATA, data);
        sendBroadcast(i);
    }





    class UdpService {

        private DatagramSocket mTxSocket = null;
        private DatagramSocket mRxSocket = null;
        private int mDestinationPort = 8877;
        private int mLocalPort = 8878;


        public UdpService() {

            try {
                mTxSocket = new DatagramSocket();
                mRxSocket = new DatagramSocket();

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }


        public void DestinationPort(int port) {
            mDestinationPort = port;
        }

        public void LocalPort(int port) {
            mLocalPort = port;
        }

        public void SetTimeOut(int t) {
            try {
                if (mRxSocket != null)
                    mRxSocket.setSoTimeout(t);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }


        public void Send(String data) {

            byte[] bd = (data.length() > 0) ? data.getBytes() : null;
            DatagramPacket _dp;

            try {
                if (bd != null) {
                    _dp = new DatagramPacket(bd, bd.length, new InetSocketAddress("255.255.255.255", mDestinationPort));
                    mTxSocket.send(_dp);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        public DatagramPacket Receive() {
            byte[] _data = new byte[256];
            DatagramPacket _dp = null;
            _dp = new DatagramPacket(_data, _data.length, new InetSocketAddress("255.255.255.255", mLocalPort));

            //This method blocks until a datagram is received.
            // The length field of the datagram packet object contains the length of
            // the received message. If the message is longer than the packet's length,
            // the message is truncated.
            try {
                mRxSocket.receive(_dp);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return  _dp;
        }

    }




    class TcpService {

        private Socket _Socket = null;
        private InetAddress _InetAddress = null;
        private int _Port = 0;
        private DataOutputStream _Out;
        private DataInputStream _In;
        //private TcpEventHandler _TcpEventHandler = null;

        /**
         *
         * @param address
         * @param port
         */
        public TcpService(String address, int port) {

            try {

                _InetAddress = InetAddress.getByName(address);
                _Port = port;
                _Socket = new Socket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         *
         * @param timeout_ms
         */
        public void Connect(int timeout_ms) {
            if (_Socket != null) {
                try {
                    //Connects this socket to the server with a specified timeout value.
                    // A timeout of zero is interpreted as an infinite timeout. The connection will
                    // then block until established or an error occurs.
                    _Socket.connect(new InetSocketAddress(_InetAddress,_Port), timeout_ms);
                    if (_Socket.isConnected()) {
                        _Out = new DataOutputStream(_Socket.getOutputStream());
                        _In = new DataInputStream(_Socket.getInputStream());
                        new Thread(DoReceiveWork).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         *
         * @return
         */
        public boolean isConnected() {
            return _Socket != null ? _Socket.isConnected() : false;
        }

        /**
         *
         */
        public void Close() {

            if (_Socket != null) {
                try {
                    _Socket.close();
                    _Out.close();
                    _In.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         *
         * @param data
         */
        public void Send(byte[] data) {
            try {
                _Out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private Runnable DoReceiveWork = new Runnable() {
            byte[] _buf = new byte[1024];

            @Override
            public void run() {
                while (_Socket.isConnected()) {
                    try {
                        int size = _In.read(_buf);
                        if (size > 0) {
                            byte[] buffer = new byte[size];
                            System.arraycopy(_buf, 0, buffer, 0, size);
                            sendBroadcast(NET_ACTION_TCP_RECEIVE_DATA, buffer);
                        }
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendBroadcast(new Intent(NET_ACTION_TCP_DISCONNECTED));
                        Close();
                        return;
                    }
                }
            }
        };

    }





    public abstract class TcpEventHandler extends Handler {
        //
        public final static int RECEIVE_DATA = 1;

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case RECEIVE_DATA:
                    onDataReceived((byte[])msg.obj);
                    break;
            }

            super.handleMessage(msg);
        }

        protected abstract void onDataReceived(byte[] b);
    }
}
