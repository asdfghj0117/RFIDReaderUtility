package com.favepc.reader.rfidreaderutility.fragment;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.CommonListAdapter;
import com.favepc.reader.rfidreaderutility.adapter.CommonPageAdapter;
import com.favepc.reader.rfidreaderutility.adapter.WrapContentViewPager;
import com.favepc.reader.rfidreaderutility.object.Common;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.rfidreaderutility.pager.CommonEPCPage;
import com.favepc.reader.rfidreaderutility.pager.CommonKillPage;
import com.favepc.reader.rfidreaderutility.pager.CommonLockPage;
import com.favepc.reader.rfidreaderutility.pager.CommonReadPage;
import com.favepc.reader.rfidreaderutility.pager.CommonTIDPage;
import com.favepc.reader.rfidreaderutility.pager.CommonWritePage;
import com.favepc.reader.service.OTGService;
import com.favepc.reader.service.ReaderService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruce_Chiang on 2017/3/10.
 */

@SuppressLint("ValidFragment")
public class CommonFragment extends Fragment {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";

    private Context	mContext;
    private Activity mActivity;
    private AppContext mAppContext;
    private View mCommonView = null;
    private WrapContentViewPager mViewPager;
    private CommonListAdapter mCommonListAdapter;
    private ArrayList<Common> mCommons = new ArrayList<Common>();
    private LayoutInflater mLayoutInflater;

    //pager
    private List<View> mListCommonPageViews;
    private CommonPageAdapter mCommonPageAdapter;
    public static final int PAGE_EPC = 0;
    public static final int PAGE_TID = 1;

    private int	mCommonPagePosition = PAGE_EPC;
    private CommonEPCPage mCommonEPCPage;
    private CommonTIDPage mCommonTIDPage;
    private CommonReadPage mCommonReadPage;
    private CommonWritePage mCommonWritePage;
    private CommonLockPage mCommonLockPage;
    private CommonKillPage mCommonKillPage;

    private ReaderService mReaderService;
    private CommonMsgReceiver mCommnoMsgReceiver;
    private boolean mRunningFlag = false;
    private String mProcessCommand;
    private byte[] mProcess;
    private ArrayList<HashMap<String, String>> mProcessList;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private CustomKeyboardManager mCustomKeyboardManager;


    public CommonFragment() {
        super();
    }
    public CommonFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mCustomKeyboardManager = this.mAppContext.getKeyboard();
        this.mActivity = getActivity();
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mReaderService = new ReaderService();


        this.mCommnoMsgReceiver = new CommonMsgReceiver();
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(OTGService.OTG_ACTION_DISCONNECTED_COMMON));

        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonEPCPage.COMMON_ACTION_EPC_REPEAT));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonEPCPage.COMMON_ACTION_EPC_ONE));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonEPCPage.COMMON_ACTION_EPC_END));

        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonTIDPage.COMMON_ACTION_TID_REPEAT));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonTIDPage.COMMON_ACTION_TID_ONE));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonTIDPage.COMMON_ACTION_TID_END));

        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonReadPage.COMMON_ACTION_READ));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonWritePage.COMMON_ACTION_WRITE));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonLockPage.COMMON_ACTION_LOCK));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonKillPage.COMMON_ACTION_KILL));
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
        if (this.mCommonView == null) {

            this.mLayoutInflater = inflater;
            this.mCommonView = inflater.inflate(R.layout.fragment_common, container, false);

            this.mCommonListAdapter = new CommonListAdapter(this.mContext, R.layout.adapter_common, this.mCommons);
            ListView lv = (ListView)this.mCommonView.findViewById(R.id.common_lvMsg);
            lv.setAdapter(this.mCommonListAdapter);


            Runnable mPendingRunnable = new Runnable() {
                @Override
                public void run() {
                    //create pager: epc,tid, read, write
                    {
                        mCommonEPCPage = new CommonEPCPage(mContext, mActivity, mLayoutInflater, mReaderService);
                        mCommonTIDPage = new CommonTIDPage(mContext, mActivity, mLayoutInflater, mReaderService);
                        mCommonReadPage = new CommonReadPage(mContext, mActivity, mLayoutInflater, mReaderService, mCustomKeyboardManager);
                        mCommonWritePage = new CommonWritePage(mContext, mActivity, mLayoutInflater, mReaderService, mCustomKeyboardManager);
                        mCommonLockPage = new CommonLockPage(mContext, mActivity, mLayoutInflater, mReaderService, mCustomKeyboardManager);
                        mCommonKillPage = new CommonKillPage(mContext, mActivity, mLayoutInflater, mReaderService, mCustomKeyboardManager);
                    }
                    //add page view
                    {
                        mListCommonPageViews = new ArrayList<View>();
                        mListCommonPageViews.add(mCommonEPCPage.getView());
                        mListCommonPageViews.add(mCommonTIDPage.getView());
                        mListCommonPageViews.add(mCommonReadPage.getView());
                        mListCommonPageViews.add(mCommonWritePage.getView());
                        mListCommonPageViews.add(mCommonLockPage.getView());
                        mListCommonPageViews.add(mCommonKillPage.getView());
                    }
                    mCommonPageAdapter = new CommonPageAdapter(mContext, mListCommonPageViews, 0);

                    mViewPager = (WrapContentViewPager) mCommonView.findViewById(R.id.common_viewPager);
                    mViewPager.setAdapter(mCommonPageAdapter);
                    mViewPager.setCurrentItem(0);
                    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            initCommon();
                            mCommonPagePosition = position;
                            switch (position) {
                                case 2:
                                    mCommonReadPage.setSelectMemory(mAppContext.getSelectMemory());
                                    mCommonReadPage.setSelectAddress(mAppContext.getSelectAddress());
                                    mCommonReadPage.setSelectLength(mAppContext.getSelectLength());
                                    mCommonReadPage.setSelectData(mAppContext.getSelectData());
                                    mCommonReadPage.setAccessPassword(mAppContext.getAccessPassword());
                                    break;
                                case 3:
                                    mCommonWritePage.setSelectMemory(mAppContext.getSelectMemory());
                                    mCommonWritePage.setSelectAddress(mAppContext.getSelectAddress());
                                    mCommonWritePage.setSelectLength(mAppContext.getSelectLength());
                                    mCommonWritePage.setSelectData(mAppContext.getSelectData());
                                    mCommonWritePage.setAccessPassword(mAppContext.getAccessPassword());
                                    break;
                                case 4:
                                    mCommonLockPage.setSelectMemory(mAppContext.getSelectMemory());
                                    mCommonLockPage.setSelectAddress(mAppContext.getSelectAddress());
                                    mCommonLockPage.setSelectLength(mAppContext.getSelectLength());
                                    mCommonLockPage.setSelectData(mAppContext.getSelectData());
                                    mCommonLockPage.setAccessPassword(mAppContext.getAccessPassword());
                                    break;
                                case 5:
                                    mCommonKillPage.setSelectMemory(mAppContext.getSelectMemory());
                                    mCommonKillPage.setSelectAddress(mAppContext.getSelectAddress());
                                    mCommonKillPage.setSelectLength(mAppContext.getSelectLength());
                                    mCommonKillPage.setSelectData(mAppContext.getSelectData());
                                    mCommonKillPage.setAccessPassword(mAppContext.getAccessPassword());
                                    break;
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                }
            };
            if (mPendingRunnable != null) {
                mCommonHandler.post(mPendingRunnable);
            }


        }
        return this.mCommonView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            initCommon();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        initCommon();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mCommnoMsgReceiver);
        mContext = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.fragment_clear, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                if (mCommons != null && mCommonListAdapter != null) {
                    mCommons.clear();
                    mCommonListAdapter.notifyDataSetChanged();
                }
                return true;
        }
        return false;
    }

    public class CommonMsgReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OTGService.OTG_ACTION_DISCONNECTED_COMMON:
                    initCommon();
                    break;
                case CommonEPCPage.COMMON_ACTION_EPC_ONE:
                    mProcessCommand = CommonEPCPage.COMMON_ACTION_EPC_ONE;
                    mProcess = mReaderService.Q();
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonEPCPage.COMMON_ACTION_EPC_REPEAT:
                    mProcessCommand = CommonEPCPage.COMMON_ACTION_EPC_REPEAT;
                    mProcess = mReaderService.Q();
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonEPCPage.COMMON_ACTION_EPC_END:
                    mProcessCommand = CommonEPCPage.COMMON_ACTION_EPC_END;
                    mCommonHandler.removeCallbacks(mRunnableBackground);
                    break;
                case CommonTIDPage.COMMON_ACTION_TID_ONE:
                    mProcessCommand = CommonTIDPage.COMMON_ACTION_TID_ONE;
                    mProcess = mReaderService.TID();
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonTIDPage.COMMON_ACTION_TID_REPEAT:
                    mProcessCommand = CommonTIDPage.COMMON_ACTION_TID_REPEAT;
                    mProcess = mReaderService.TID();
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonTIDPage.COMMON_ACTION_TID_END:
                    mProcessCommand = CommonTIDPage.COMMON_ACTION_TID_END;
                    mCommonHandler.removeCallbacks(mRunnableBackground);
                    break;
                case CommonReadPage.COMMON_ACTION_READ:
                    mProcessCommand = CommonReadPage.COMMON_ACTION_READ;
                    mProcessList = (ArrayList<HashMap<String, String>>) intent.getExtras().get(CommonReadPage.PROCESS_ARGUMENT);
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonWritePage.COMMON_ACTION_WRITE:
                    mProcessCommand = CommonWritePage.COMMON_ACTION_WRITE;
                    mProcessList = (ArrayList<HashMap<String, String>>) intent.getExtras().get(CommonReadPage.PROCESS_ARGUMENT);
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonLockPage.COMMON_ACTION_LOCK:
                    mProcessCommand = CommonLockPage.COMMON_ACTION_LOCK;
                    mProcessList = (ArrayList<HashMap<String, String>>) intent.getExtras().get(CommonReadPage.PROCESS_ARGUMENT);
                    mCommonHandler.post(mRunnableBackground);
                    break;
                case CommonKillPage.COMMON_ACTION_KILL:
                    mProcessCommand = CommonKillPage.COMMON_ACTION_KILL;
                    mProcessList = (ArrayList<HashMap<String, String>>) intent.getExtras().get(CommonReadPage.PROCESS_ARGUMENT);
                    mCommonHandler.post(mRunnableBackground);
                    break;
            }
        }
    }


    /**
     * init and stop all action
     * */
    private void initCommon() {
        switch(mCommonPagePosition) {
            case PAGE_EPC:
                mCommonEPCPage.setInit();
                break;
            case PAGE_TID:
                mCommonTIDPage.setInit();
                break;
        }
        mCommonHandler.removeCallbacks(mRunnableBackground);
    }


    /**
     * @param common Common class
     */
    private void updateView(Common common) {
        if (common.Title())
            mAppContext.setAck(true);
        else
            mAppContext.setAck(false);
        this.mCommons.add(mCommonListAdapter.getCount(), common);
        this.mCommonListAdapter.notifyDataSetChanged();

        if (mCommonListAdapter.getCount() > 300) {
            mCommons.clear();
            mCommonListAdapter.notifyDataSetChanged();
        }
    }


    private Handler mCommonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1://[TX]
                    updateView(new Common(false,
                            ReaderService.Format.showCRLF(ReaderService.Format.bytesToString((byte[])msg.obj)),
                            mDateFormat.format(new Date())));
                    ((MainActivity) mActivity).sendData((byte[])msg.obj);
                    break;
                case 2://[RX]
                    updateView(new Common(true,
                            ReaderService.Format.showCRLF((String)msg.obj),
                            mDateFormat.format(new Date())));
                    switch (mProcessCommand) {
                        case CommonEPCPage.COMMON_ACTION_EPC_ONE:
                        case CommonEPCPage.COMMON_ACTION_EPC_REPEAT:
                            mCommonEPCPage.setData(ReaderService.Format.removeCRLFandTarget((String) msg.obj, "Q"));
                            break;
                        case CommonTIDPage.COMMON_ACTION_TID_ONE:
                        case CommonTIDPage.COMMON_ACTION_TID_REPEAT:
                            mCommonTIDPage.setData(ReaderService.Format.removeCRLFandTarget((String) msg.obj, "R"));
                            break;
                    }

                    break;
            }

            super.handleMessage(msg);
        }
    };

    private Runnable mRunnableBackground = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                int _timeOutx10 = 100, _processIndex = 0, _index;
                int _error = 0;
                byte[] _bsData;
                String strData;
                String strSubData = "";
                HashMap<String, String> _item;

                @Override
                public void run() {
                    //initialization ack status
                    mAppContext.setAck(true);

                    while(true) {
                        _timeOutx10 = 100;
                        while(!mAppContext.getAck() && _timeOutx10 > 0) {
                            try {
                                Thread.sleep(10);
                                _timeOutx10--;
                            } catch (InterruptedException e) {e.printStackTrace();}
                        }

                        if (!((MainActivity) mActivity).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    initCommon();
                                }
                            });
                        }



                        //[TX]
                        switch (mProcessCommand) {
                            case CommonEPCPage.COMMON_ACTION_EPC_ONE:
                            case CommonEPCPage.COMMON_ACTION_EPC_REPEAT:
                            case CommonTIDPage.COMMON_ACTION_TID_ONE:
                            case CommonTIDPage.COMMON_ACTION_TID_REPEAT:
                                mCommonHandler.sendMessage(mCommonHandler.obtainMessage(1, mProcess));
                                break;
                            case CommonReadPage.COMMON_ACTION_READ:
                            case CommonWritePage.COMMON_ACTION_WRITE:
                            case CommonLockPage.COMMON_ACTION_LOCK:
                            case CommonKillPage.COMMON_ACTION_KILL:
                                if (_processIndex == mProcessList.size())
                                    return;
                                _item = mProcessList.get(_processIndex);
                                mCommonHandler.sendMessage(mCommonHandler.obtainMessage(1, ReaderService.Format.stringToBytes(_item.get(PROCESS_DATA))));
                                break;
                        }


                        _timeOutx10 = 100;
                        while(_timeOutx10 > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mActivity).checkData() > 0) {
                                _bsData = ((MainActivity) mActivity).getData();
                                if (_bsData == null) {
                                    _error = -1;
                                    return;
                                }

                                if (_bsData.length > 0) {
                                    strData = new String(_bsData);
                                    if (strSubData.length() > 0) {
                                        strData = strSubData + strData;
                                        strSubData = "";
                                    }

                                    if ((_index = strData.indexOf(ReaderService.COMMAND_END)) != -1) {
                                        mCommonHandler.sendMessage(mCommonHandler.obtainMessage(2, strData.substring(0, _index + 2)));
                                        _timeOutx10 = 0;
                                    }
                                    else {
                                        strSubData = strData;
                                        _timeOutx10++;
                                    }
                                }
                            }
                            _timeOutx10--;
                        } //while(_timeOutx10 > 1)

                        //error process
                        switch(_error) {
                            case -1:
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext, "No data callback. Stop the process", Toast.LENGTH_SHORT).show();
                                        initCommon();
                                    }
                                });
                                return;
                        }
                        _processIndex++;

                        if (mProcessCommand.equals(CommonEPCPage.COMMON_ACTION_EPC_ONE) ||
                                mProcessCommand.equals(CommonEPCPage.COMMON_ACTION_EPC_END) ||
                                mProcessCommand.equals(CommonTIDPage.COMMON_ACTION_TID_ONE) ||
                                mProcessCommand.equals(CommonTIDPage.COMMON_ACTION_TID_END) ||
                                mProcessCommand.equals(CommonKillPage.COMMON_ACTION_KILL)) {
                            return;
                        }

                        //read, write, lock
                        if (mProcessCommand.equals(CommonReadPage.COMMON_ACTION_READ) ||
                                mProcessCommand.equals(CommonWritePage.COMMON_ACTION_WRITE) ||
                                mProcessCommand.equals(CommonLockPage.COMMON_ACTION_LOCK)) {
                            if (_processIndex == mProcessList.size())
                                return;
                        }
                    }
                }
            }).start();
        }
    };
}
