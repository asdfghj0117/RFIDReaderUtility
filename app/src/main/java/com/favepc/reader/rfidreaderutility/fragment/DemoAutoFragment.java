package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;

public class DemoAutoFragment extends Fragment {

    private Context mContext;
    private Activity mActivity;
    private View mDemoIRView = null;
    private Button mBtnClear;
    private ReaderService mReaderService;
    private ListView lvIRMessage;
    private ArrayList<String> mDemoIRs = new ArrayList<String>();
    private ArrayAdapter<String> mDemoIRAdapter;
    private boolean m_bDemoIRAutoDetect = false;
    private boolean m_bCombine = true;

    public DemoAutoFragment() { super();}
    @SuppressLint("ValidFragment")
    public DemoAutoFragment(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mReaderService = new ReaderService();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (this.mDemoIRView == null) {
            this.mDemoIRView = inflater.inflate(R.layout.fragment_auto, container, false);

            this.mDemoIRAdapter = new ArrayAdapter<String>(this.mContext, R.layout.adapter_demoir, mDemoIRs);
            this.lvIRMessage = (ListView)this.mDemoIRView.findViewById(R.id.fragment_demoir_lv_msg);
            this.lvIRMessage.setAdapter(this.mDemoIRAdapter);

            this.mBtnClear = (Button)this.mDemoIRView.findViewById(R.id.fragment_demoir_btn_clear);
            this.mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDemoIRs != null)
                        mDemoIRs.clear();
                    if (mDemoIRAdapter != null)
                        mDemoIRAdapter.notifyDataSetChanged();
                }
            });

            if (((MainActivity) mContext).isConnected())
            {
                m_bDemoIRAutoDetect = true;
                mAutoHandler.post(mRunnableAutoBackground);
            }
        }

        return this.mDemoIRView;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            m_bDemoIRAutoDetect = false;
            mAutoHandler.removeCallbacks(mRunnableAutoBackground);
        }
        else {
            if (((MainActivity) mContext).isConnected())
            {
                m_bDemoIRAutoDetect = true;
                mAutoHandler.post(mRunnableAutoBackground);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_bDemoIRAutoDetect = false;
        mAutoHandler.removeCallbacks(mRunnableAutoBackground);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.fragment_auto, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_combine:
                if (m_bCombine) {
                    m_bCombine = false;
                }
                else {
                    m_bCombine = true;
                }
                return true;
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if(m_bCombine) {
            menu.findItem(R.id.action_combine).setTitle(R.string.menu_no_combine);
        }
        else {
            menu.findItem(R.id.action_combine).setTitle(R.string.menu_combine);
        }
        super.onPrepareOptionsMenu(menu);
    }

    StringBuilder mSubRawData = new StringBuilder(256);
    Byte _SubRawOld = 0;
    private Runnable mRunnableAutoBackground = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                byte[] _bsData;

                @Override
                public void run() {

                    while(m_bDemoIRAutoDetect) {
                        if (!((MainActivity) mContext).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    m_bDemoIRAutoDetect = false;
                                    mAutoHandler.removeCallbacks(mRunnableAutoBackground);
                                }
                            });
                            return;
                        }

                        //[RX]
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {e.printStackTrace();}

                        if (((MainActivity) mContext).checkData() > 0) {
                            _bsData = ((MainActivity) mContext).getData();

                            if (_bsData == null) return;

                            if (m_bCombine) {
                                for (int i = 0; i < _bsData.length; i++) {
                                    mSubRawData.append((char)_bsData[i]);
                                    if (_bsData[i] == 0x0A) {
                                        if (_SubRawOld == 0x0D)
                                        {
                                            Message _msg1 = new Message();
                                            _msg1.what = 2;
                                            _msg1.obj = mSubRawData.toString();
                                            Log.d("UUU", ReaderService.Format.showCRLF((String) _msg1.obj));
                                            mAutoHandler.sendMessage(_msg1);
                                            mSubRawData.setLength(0);
                                        }
                                    }
                                    _SubRawOld = _bsData[i];
                                }
                            }
                            else {
                                Message _msg = new Message();
                                _msg.what = 2;
                                _msg.obj = String.format("%3d:%s", _bsData.length, ReaderService.Format.bytesToString(_bsData));
                                Log.d("UUU", ReaderService.Format.showCRLF((String) _msg.obj));
                                mAutoHandler.sendMessage(_msg);
                            }
                        }
                    }
                }
            }).start();
        }
    };

    private Handler mAutoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    mDemoIRs.add(ReaderService.Format.showCRLF((String)msg.obj));
                    mDemoIRAdapter.notifyDataSetChanged();
                    lvIRMessage.setSelection(mDemoIRAdapter.getCount() - 1);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
