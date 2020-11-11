package com.favepc.reader.rfidreaderutility.fragment;
/**
 * *************************************************************************************************
 * FILE:			BLEFragment.java
 * ------------------------------------------------------------------------------------------------
 * COMPANY:			FAVEPC
 * VERSION:			V1.0
 * CREATED:			2017/3/9
 * DATE:
 * AUTHOR:			Bruce_Chiang
 * ------------------------------------------------------------------------------------------------
 * \PURPOSE:
 * - None
 * \NOTES:
 * - None
 * \Global Variables:
 * - None
 * ------------------------------------------------------------------------------------------------
 * REVISION		Date			User	Description
 * V1.0			2017/3/24     	Bruce	1.First create version
 * <p/>
 * ------------------------------------------------------------------------------------------------
 * *************************************************************************************************
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.DemoUListAdapter;
import com.favepc.reader.rfidreaderutility.object.DemoU;
import com.favepc.reader.service.OTGService;
import com.favepc.reader.service.ReaderService;

import java.math.BigInteger;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class DemoUFragment extends Fragment {

    private Context	mContext;
    private Activity mActivity;
    private View mDemoView = null;
    private DemoUListAdapter mDemoUListAdapter;
    private ArrayList<DemoU> mDemoUs = new ArrayList<DemoU>();

    private Button mBtnStart, mBtnClear;
    private TextView mTextViewReadCount, mTextViewTagCount;
    private ProgressBar mProgressBar;
    private boolean m_bDemoUToggle = false;
    private boolean m_bDemoUAutoDetect = false;
    //private boolean m_bDemoUEndCommand = false;
    private int mRunCount = 0;

    private ReaderService mReaderService;
    private DemoMsgReceiver mDemoMsgReceiver;

    public DemoUFragment() { super();}
    public DemoUFragment(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mReaderService = new ReaderService();
        this.mDemoMsgReceiver = new DemoMsgReceiver();
        this.mContext.registerReceiver(mDemoMsgReceiver, new IntentFilter(OTGService.OTG_ACTION_DISCONNECTED_DEMO));
        //this.m_bDemoUAutoDetect = true;
        //this.mAutoHandler.post(mRunnableAutoBackground);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mDemoView == null) {
            this.mDemoView = inflater.inflate(R.layout.fragment_demou, container, false);
            this.mDemoUListAdapter = new DemoUListAdapter(this.mContext, R.layout.adapter_demou, this.mDemoUs);
            ListView lv = (ListView)mDemoView.findViewById(R.id.demou_lvTags);
            lv.setAdapter(this.mDemoUListAdapter);

            this.mTextViewReadCount = (TextView)this.mDemoView.findViewById(R.id.demou_tvreadcount);
            this.mTextViewTagCount = (TextView)this.mDemoView.findViewById(R.id.demou_tvtagcount);

            this.mBtnStart = (Button)this.mDemoView.findViewById(R.id.demou_btnStart);
            this.mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!m_bDemoUToggle)
                    {
                        if (((MainActivity) mContext).isConnected()) {
                            //m_bDemoUAutoDetect = false;
                            //mAutoHandler.removeCallbacks(mRunnableAutoBackground);

                            m_bDemoUToggle = true;
                            //mHandler.post(mRunnableBackground);

                            mBtnStart.setText("STOP");
                            mProgressBar.setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        buttonInit();
                        //m_bDemoUAutoDetect = true;
                        //mAutoHandler.post(mRunnableAutoBackground);


                    }
                }
            });

            this.mBtnClear = (Button)this.mDemoView.findViewById(R.id.demou_btnClear);
            this.mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRunCount = 0;
                    if (mDemoUs != null)
                        mDemoUs.clear();
                    if (mDemoUListAdapter != null)
                        mDemoUListAdapter.notifyDataSetChanged();
                    mTextViewReadCount.setText("");
                    mTextViewTagCount.setText("");


                }
            });

            this.mProgressBar = (ProgressBar)this.mDemoView.findViewById(R.id.demou_progressBar);
            this.mProgressBar.setVisibility(View.GONE);

        }

        return this.mDemoView;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            buttonInit();
            m_bDemoUAutoDetect = false;
            mAutoHandler.removeCallbacks(mRunnableAutoBackground);
        }
        else {
            if (((MainActivity) mContext).isConnected())
            {
                m_bDemoUAutoDetect = true;
                mAutoHandler.post(mRunnableAutoBackground);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        buttonInit();
        m_bDemoUAutoDetect = false;
        mAutoHandler.removeCallbacks(mRunnableAutoBackground);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mDemoMsgReceiver);
        mContext = null;
    }

    /**
     * */
    public class DemoMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OTGService.OTG_ACTION_DISCONNECTED_DEMO:
                    if (m_bDemoUToggle) {
                        buttonInit();
                        m_bDemoUAutoDetect = false;
                        mAutoHandler.removeCallbacks(mRunnableAutoBackground);
                        Toast.makeText(mContext, intent.getExtras().getString(OTGService.STRING_DATA), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    /***
     *
     */
    private void buttonInit() {

        mBtnStart.setText("RUN");
        mProgressBar.setVisibility(View.GONE);
        m_bDemoUToggle = false;
        //mHandler.removeCallbacks(mRunnableBackground);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean _bCompare = false;
            String _strObj;

            switch (msg.what) {
                case 1:
                    //mTextViewReadCount.setText(Integer.toString((int)msg.obj));
                    break;
                case 2:
                    _strObj = (String)msg.obj;

                    if (_strObj.regionMatches(1, "U", 0, 1) && _strObj.length() > 4) {

                        final String str = _strObj.substring(2, _strObj.length() - 2);
                        if (str.length() == 0) break;
                        else if (str.length() == 8) {

                        }
                        else {
                            try {
                                if (ReaderService.Format.crc16(new BigInteger(str,16).toByteArray()) != 0x1D0F)
                                    break;
                            }
                            catch (NumberFormatException ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }



                        DemoU _demoU = new DemoU(
                                str.substring(0, 4),
                                str.substring(4, str.length() - 4),
                                str.substring(str.length() - 4));

                        if (mDemoUListAdapter.getCount() > 0) {
                            for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                            {
                                if (mDemoUs.get(j).EPC().equals(_demoU.EPC()) && mDemoUs.get(j).CRC16().equals(_demoU.CRC16())) {

                                    int _number = Integer.valueOf(mDemoUs.get(j).Count()) + 1;
                                    _demoU.Count(String.valueOf(_number));
                                    updateView(false, j, _demoU);
                                    _bCompare = true;
                                    break;
                                }
                                else {
                                    _bCompare = false;
                                }
                            }
                        }
                        else  {
                            _bCompare = true;
                            _demoU.Count(String.valueOf(1));
                            updateView(true, 0, _demoU);
                        }
                        if (!_bCompare) {
                            _demoU.Count(String.valueOf(1));
                            updateView(true, mDemoUListAdapter.getCount(), _demoU);
                        }
                    }
                    else {
                        for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                        {
                            int count = Integer.valueOf(mDemoUs.get(j).Count());
                            mDemoUs.get(j).Percentage((count * 100 / mRunCount) + "%");
                        }
                        updateView();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Handler mAutoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean _bCompare = false;
            String _strObj;

            switch (msg.what) {
                case 1:
                    //mTextViewReadCount.setText(Integer.toString((int)msg.obj));
                    break;
                case 2:
                    _strObj = (String)msg.obj;

                    if (_strObj.regionMatches(1, "U", 0, 1) && _strObj.length() > 4) {

                        final String str = _strObj.substring(2, _strObj.length() - 2);
                        if (str.length() == 0) break;
                        else if (str.length() == 8) {

                        }
                        else {
                            try {
                                if (ReaderService.Format.crc16(new BigInteger(str,16).toByteArray()) != 0x1D0F)
                                    break;
                            }
                            catch (NumberFormatException ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }



                        DemoU _demoU = new DemoU(
                                str.substring(0, 4),
                                str.substring(4, str.length() - 4),
                                str.substring(str.length() - 4));

                        if (mDemoUListAdapter.getCount() > 0) {
                            for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                            {
                                if (mDemoUs.get(j).EPC().equals(_demoU.EPC()) && mDemoUs.get(j).CRC16().equals(_demoU.CRC16())) {

                                    int _number = Integer.valueOf(mDemoUs.get(j).Count()) + 1;
                                    _demoU.Count(String.valueOf(_number));
                                    updateView(false, j, _demoU);
                                    _bCompare = true;
                                    break;
                                }
                                else {
                                    _bCompare = false;
                                }
                            }
                        }
                        else  {
                            _bCompare = true;
                            _demoU.Count(String.valueOf(1));
                            updateView(true, 0, _demoU);
                        }
                        if (!_bCompare) {
                            _demoU.Count(String.valueOf(1));
                            updateView(true, mDemoUListAdapter.getCount(), _demoU);
                        }
                    }
                    else {
                        for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                        {
                            int count = Integer.valueOf(mDemoUs.get(j).Count());
                            mDemoUs.get(j).Percentage((count * 100 / mRunCount) + "%");
                        }
                        updateView();
                    }
                    /*String[] array = ((String)msg.obj).split(ReaderService.COMMAND_END);

                    for (int i = 0 ; i < array.length ; i ++) {
                        if (array[i].regionMatches(1, "U", 0, 1) && array[i].length() > 4) {

                            final String str = array[i].substring(2, array[i].length());

                            if (str.length() > 8) {
                                try {
                                    if (ReaderService.Format.crc16(new BigInteger(str,16).toByteArray()) != 0x1D0F)
                                        break;
                                }
                                catch (NumberFormatException ex) {
                                    Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            DemoU _demoU = new DemoU(
                                    str.substring(0, 4),
                                    str.substring(4, str.length() - 4),
                                    str.substring(str.length() - 4));

                            if (mDemoUListAdapter.getCount() > 0) {
                                for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                                {
                                    if (mDemoUs.get(j).EPC().equals(_demoU.EPC()) && mDemoUs.get(j).CRC16().equals(_demoU.CRC16())) {

                                        int _number = Integer.valueOf(mDemoUs.get(j).Count()) + 1;
                                        _demoU.Count(String.valueOf(_number));
                                        //_demoU.Percentage(String.valueOf((int)(_number * 100 / mRunCount)) + "%");
                                        updateView(false, j, _demoU);
                                        _bCompare = true;
                                        break;
                                    }
                                    else {
                                        _bCompare = false;
                                    }
                                }
                            }
                            else  {
                                _bCompare = true;
                                _demoU.Count(String.valueOf(1));
                                //_demoU.Percentage(String.valueOf((int)(1 * 100 / mRunCount)) + "%");
                                updateView(true, 0, _demoU);
                            }
                            if (!_bCompare) {
                                _demoU.Count(String.valueOf(1));
                                //_demoU.Percentage(String.valueOf((int)(1 * 100 / mRunCount)) + "%");
                                updateView(true, mDemoUListAdapter.getCount(), _demoU);
                            }
                        }
                        else {
                            for (int j = 0; j < mDemoUListAdapter.getCount(); j ++)
                            {
                                int count = Integer.valueOf(mDemoUs.get(j).Count());
                                mDemoUs.get(j).Percentage((int)(count * 100 / mRunCount) + "%");
                            }
                            updateView();
                        }
                    }*/
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     *
     * @param action true is add view, otherwise is set view.
     * @param position view position.
     * @param demoU demoU class
     */
    private void updateView(boolean action, int position, DemoU demoU) {

        if (action)
            mDemoUs.add(position, demoU);
        else
            mDemoUs.set(position, demoU);
        //mDemoUListAdapter.notifyDataSetChanged();
    }


    private void updateView() {
        mDemoUListAdapter.notifyDataSetChanged();
        mTextViewTagCount.setText(Integer.toString(mDemoUListAdapter.getCount()));
    }


    StringBuilder _SubRaw = new StringBuilder(512);
    Byte _SubRawOld = 0;
    private Runnable mRunnableBackground = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                int _timeOutx4 = 500;
                byte[] bsData;

                @Override
                public void run() {

                    while(m_bDemoUToggle) {
                        //TX
                        mRunCount++;
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                mTextViewReadCount.setText(Integer.toString(mRunCount));
                            }
                        });

                        ((MainActivity) mContext).sendData(mReaderService.U());

                        //RX
                        _timeOutx4 = 500;
                        while(_timeOutx4 > 1) {
                            try {
                                Thread.sleep(4);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mContext).checkData() > 0) {
                                bsData = ((MainActivity) mContext).getData();
                                if (bsData != null) {
                                    _timeOutx4++;
                                    for (int i = 0; i < bsData.length; i++) {
                                        if (bsData[i] != 0) {
                                            _SubRaw.append((char) bsData[i]);
                                            if (bsData[i] == 0x0A) {
                                                if (_SubRawOld == 0x0D) {
                                                    Message msg = new Message();
                                                    msg.what = 2;
                                                    msg.obj = _SubRaw.toString();
                                                    if (msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                        _timeOutx4 = 1;
                                                    }

                                                    mHandler.sendMessage(msg);
                                                    _SubRaw.setLength(0);
                                                }
                                            }
                                            _SubRawOld = bsData[i];
                                        }
                                    }
                                }
                            }
                            _timeOutx4--;
                        }
                    }
                }
            }).start();
        }
    };

    private Runnable mRunnableAutoBackground = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                int index;
                byte[] bsData;
                String strData;
                String strSubData = "";
                int _timeOut = 600;

                @Override
                public void run() {

                    while(m_bDemoUAutoDetect) {
                        /*if (!((MainActivity) mContext).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    buttonInit();
                                    m_bDemoUAutoDetect = false;
                                    mAutoHandler.removeCallbacks(mRunnableAutoBackground);
                                }
                            });
                            return;
                        }*/

                        if (m_bDemoUToggle) {
                            //TX
                            mRunCount++;
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    mTextViewReadCount.setText(Integer.toString(mRunCount));
                                }
                            });

                            ((MainActivity) mContext).sendData(mReaderService.U());

                            //RX
                            _timeOut = 600;
                            while(_timeOut > 1) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {e.printStackTrace();}

                                if (((MainActivity) mContext).checkData() > 0) {
                                    bsData = ((MainActivity) mContext).getData();
                                    if (bsData != null) {
                                        _timeOut = 600;
                                        for (int i = 0; i < bsData.length; i++) {
                                            if (bsData[i] != 0) {
                                                _SubRaw.append((char) bsData[i]);
                                                if (bsData[i] == 0x0A) {
                                                    if (_SubRawOld == 0x0D) {
                                                        Message msg = new Message();
                                                        msg.what = 2;
                                                        msg.obj = _SubRaw.toString();
                                                        if (msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                            _timeOut = 1;
                                                        }

                                                        mAutoHandler.sendMessage(msg);
                                                        _SubRaw.setLength(0);
                                                    }
                                                }
                                                _SubRawOld = bsData[i];
                                            }
                                        }
                                    }
                                }
                                _timeOut--;
                            }
                        }
                        else {
                            //[RX]
                            try {
                                Thread.sleep(4);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mContext).checkData() > 0) {
                                bsData = ((MainActivity) mContext).getData();

                                if (bsData == null) return;

                                for (int i = 0; i < bsData.length; i++) {
                                    if (bsData[i] != 0) {
                                        _SubRaw.append((char) bsData[i]);
                                        if (bsData[i] == 0x0A) {
                                            if (_SubRawOld == 0x0D) {
                                                Message msg = new Message();
                                                msg.what = 2;
                                                msg.obj = _SubRaw.toString();
                                                if (msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                    mRunCount++;
                                                    mActivity.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            mTextViewReadCount.setText(Integer.toString(mRunCount));
                                                        }
                                                    });
                                                }

                                                mAutoHandler.sendMessage(msg);
                                                _SubRaw.setLength(0);
                                            }
                                        }
                                        _SubRawOld = bsData[i];
                                    }
                                }

                                /*if (bsData.length > 0) {
                                    strData = new String(bsData);
                                    if (strSubData.length() > 0) {
                                        strData = strSubData + strData;
                                        strSubData = "";
                                    }
                                    if ((index = strData.indexOf(ReaderService.COMMANDU_END)) != -1) {
                                        mRunCount++;
                                        mActivity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                mTextViewReadCount.setText(Integer.toString(mRunCount));
                                                //mTextViewTagCount.setText(Integer.toString(mDemoUListAdapter.getCount()));
                                            }
                                        });
                                        Message msg = new Message();
                                        msg.what = 2;
                                        msg.obj = strData.substring(0, index);
                                        mAutoHandler.sendMessage(msg);
                                    }
                                    else {
                                        strSubData = strData;
                                    }
                                }*/
                            }
                        }

                    }
                }
            }).start();
        }
    };
}
