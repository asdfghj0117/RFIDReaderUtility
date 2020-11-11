package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.DemoURListAdapter;
import com.favepc.reader.rfidreaderutility.adapter.EditTextAdapter;
import com.favepc.reader.rfidreaderutility.object.CustomBaseKeyboard;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.rfidreaderutility.object.DemoUR;
import com.favepc.reader.service.OTGService;
import com.favepc.reader.service.ReaderService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bruce_Chiang on 2017/4/11.
 */
@SuppressLint("ValidFragment")
public class DemoURFragment extends Fragment {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";

    private Context mContext;
    private Activity mActivity;
    private View mDemoURView = null;
    private DemoURListAdapter mDemoURListAdapter;
    private ArrayList<DemoUR> mDemoURs = new ArrayList<DemoUR>();

    private Button mBtnStart, mBtnClear;
    private TextView mTextViewCount, mTextViewTagCount;
    private ProgressBar mProgressBar;
    private Spinner mSpinnerSlotQ, mSpinnerMemoryRead, mSpinnerMemorySelect;
    private EditTextAdapter mEtReadAddress, mEtReadLength, mEtAccessPassword, mEtSelectAddress, mEtSelectLength, mEtSelectData;
    private Drawable mDrawableOK, mDrawableError;
    private CheckBox cbSelect, cbAccess, cbRead, cbSlot;
    private LinearLayout ll1, ll2;
    private CheckBox cbProcess;

    private boolean mIsCheckSelectAddress = false, mIsCheckSelectLength = false, mIsCheckSelectData = false;
    private boolean mIsCheckAccessPassword = false;
    private boolean mIsCheckReadAddress = false, mIsCheckReadLength = false;
    private boolean m_bDemoURToggle = false;
    private int mRunCount = 0;

    private ReaderService mReaderService;
    private DemoURMsgReceiver mDemoURMsgReceiver;
    private byte[] mUR;
    private ArrayList<HashMap<String, String>> mProcessList;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private AppContext mAppContext;


    public DemoURFragment() { super();}
    public DemoURFragment(Context context, Activity activity) {
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
        this.mReaderService = new ReaderService();
        this.mDemoURMsgReceiver = new DemoURMsgReceiver();
        this.mContext.registerReceiver(mDemoURMsgReceiver, new IntentFilter(OTGService.OTG_ACTION_DISCONNECTED_DEMOUR));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.cbProcess.setChecked(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mDemoURView == null) {
            this.mDrawableOK 	= ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_check_black_48dp);
            this.mDrawableError = ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_close_black_48dp);
            this.mDrawableOK.setBounds(0, 0, 48, 48);
            this.mDrawableError.setBounds(0, 0, 48, 48);

            this.mProcessList = new ArrayList<>();

            //custom keyboard select
            HexKeyboard = new CustomBaseKeyboard(mContext, R.xml.keyboard) {
                @Override
                public void hideKeyboard(EditText etCurrent) {
                    mCustomKeyboardManager.hideSoftKeyboard(etCurrent);
                }

                @Override
                public boolean handleSpecialKey(EditText etCurrent, int primaryCode) {
                    return false;
                }
            };

            this.mDemoURView = inflater.inflate(R.layout.fragment_demour, container, false);
            this.mDemoURListAdapter = new DemoURListAdapter(this.mContext, R.layout.adapter_demour, this.mDemoURs);
            ListView lv = (ListView)mDemoURView.findViewById(R.id.demoUR_lvTags);
            lv.setAdapter(this.mDemoURListAdapter);

            this.mTextViewCount = (TextView) this.mDemoURView.findViewById(R.id.demoUR_tvcount);
            this.mTextViewTagCount = (TextView) this.mDemoURView.findViewById(R.id.demoUR_tvtagcount);

            this.ll1 = (LinearLayout) this.mDemoURView.findViewById(R.id.demoUR_ll1);
            this.ll2 = (LinearLayout) this.mDemoURView.findViewById(R.id.demoUR_ll2);
            this.cbSelect = (CheckBox) this.mDemoURView.findViewById(R.id.demoUR_checkbox_select);
            this.cbAccess = (CheckBox) this.mDemoURView.findViewById(R.id.demoUR_checkbox_access);
            this.cbRead = (CheckBox) this.mDemoURView.findViewById(R.id.demoUR_checkbox_read);
            this.cbSlot = (CheckBox) this.mDemoURView.findViewById(R.id.demoUR_checkbox_slotq);
            this.cbProcess = (CheckBox) this.mDemoURView.findViewById(R.id.demoUR_cbProcess);
            this.cbProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!b) {
                        cbSelect.setVisibility(View.VISIBLE);
                        ll1.setVisibility(View.VISIBLE);
                        mEtSelectData.setVisibility(View.VISIBLE);

                        cbAccess.setVisibility(View.VISIBLE);
                        mEtAccessPassword.setVisibility(View.VISIBLE);

                        cbRead.setVisibility(View.VISIBLE);
                        ll2.setVisibility(View.VISIBLE);

                        cbSlot.setVisibility(View.VISIBLE);
                        mSpinnerSlotQ.setVisibility(View.VISIBLE);
                    }
                    else {
                        cbSelect.setVisibility(View.GONE);
                        ll1.setVisibility(View.GONE);
                        mEtSelectData.setVisibility(View.GONE);

                        cbAccess.setVisibility(View.GONE);
                        mEtAccessPassword.setVisibility(View.GONE);

                        cbRead.setVisibility(View.GONE);
                        ll2.setVisibility(View.GONE);

                        cbSlot.setVisibility(View.GONE);
                        mSpinnerSlotQ.setVisibility(View.INVISIBLE);
                    }
                }
            });

            ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.slot_q,
                    R.layout.spinner_style);
            this.mSpinnerSlotQ = (Spinner) this.mDemoURView.findViewById(R.id.demoUR_spinner_q);
            this.mSpinnerSlotQ.setAdapter(lunchList);

            ArrayAdapter<CharSequence> lunchList1 = ArrayAdapter.createFromResource(this.mContext, R.array.common_memory_bank,
                    R.layout.spinner_style);
            this.mSpinnerMemorySelect = (Spinner) this.mDemoURView.findViewById(R.id.demoUR_select_spinner_memory);
            this.mSpinnerMemorySelect.setAdapter(lunchList1);
            this.mSpinnerMemoryRead = (Spinner) this.mDemoURView.findViewById(R.id.demoUR_read_spinner_memory);
            this.mSpinnerMemoryRead.setAdapter(lunchList1);
            this.mSpinnerMemoryRead.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    switch(i) {
                        case 0:
                            mEtReadAddress.setText("0000");
                            mEtReadLength.setText("04");
                            break;
                        case 1://EPC
                            mEtReadAddress.setText("0002");
                            mEtReadLength.setText("06");
                            break;
                        case 2://TID
                            mEtReadAddress.setText("0000");
                            mEtReadLength.setText("04");
                            break;
                        case 3://USER
                            mEtReadAddress.setText("0000");
                            mEtReadLength.setText("01");
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            this.mEtSelectAddress = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_select_address);
            this.mEtSelectLength = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_select_length);
            this.mEtSelectData = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_select_data);
            this.mEtAccessPassword = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_access_password);
            this.mEtReadAddress = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_address);
            this.mEtReadLength = (EditTextAdapter)this.mDemoURView.findViewById(R.id.demoUR_length);

            //Select
            this.mEtSelectAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        if (Integer.parseInt(s.toString(), 16) <= 0x3FFF) {
                            mEtSelectAddress.setError(null, mDrawableOK);
                            mIsCheckSelectAddress = true;
                        }
                        else {
                            mEtSelectAddress.setError(null, mDrawableError);
                            mIsCheckSelectAddress = false;
                        }
                    }
                    else {
                        mEtSelectAddress.setError(null, mDrawableError);
                        mIsCheckSelectAddress = false;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            this.mEtSelectLength.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        if ((Integer.parseInt(editable.toString(), 16) > 0 ) && (Integer.parseInt(editable.toString(), 16) <= 0x20)) {
                            mEtSelectLength.setError(null, mDrawableOK);
                            mIsCheckSelectLength = true;
                        }
                        else {
                            mEtSelectLength.setError(null, mDrawableError);
                            mIsCheckSelectLength = false;
                        }
                    }
                    else {
                        mEtSelectLength.setError(null, mDrawableError);
                        mIsCheckSelectLength = false;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            });
            this.mEtSelectData.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    int nLength = editable.length();
                    int nBitsLength = Integer.parseInt(mEtSelectLength.getText().toString(),16);
                    int nMax = nLength * 4;
                    int nMin = nLength * 4 - 3;
                    if ((nBitsLength < nMin) || (nBitsLength > nMax))
                    {
                        mEtSelectData.setError(null, mDrawableError);
                        mIsCheckSelectData = false;
                    }
                    else {
                        mEtSelectData.setError(null, mDrawableOK);
                        mIsCheckSelectData = true;
                    }
                }
            });

            //Access
            this.mEtAccessPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 8) {
                        mEtAccessPassword.setError(null, mDrawableOK);
                        mIsCheckAccessPassword = true;
                    }
                    else {
                        mEtAccessPassword.setError(null, mDrawableError);
                        mIsCheckAccessPassword = false;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            //Read
            this.mEtReadAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        if (Integer.parseInt(s.toString(), 16) <= 0x3FFF) {
                            mEtReadAddress.setError(null, mDrawableOK);
                            mIsCheckReadAddress = true;
                        }
                        else {
                            mEtReadAddress.setError(null, mDrawableError);
                            mIsCheckReadAddress = false;
                        }
                    }
                    else {
                        mEtReadAddress.setError(null, mDrawableError);
                        mIsCheckReadAddress = false;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            this.mEtReadLength.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        if ((Integer.parseInt(editable.toString(), 16) > 0 ) && (Integer.parseInt(editable.toString(), 16) <= 0x20)) {
                            mEtReadLength.setError(null, mDrawableOK);
                            mIsCheckReadLength = true;
                        }
                        else {
                            mEtReadLength.setError(null, mDrawableError);
                            mIsCheckReadLength = false;
                        }
                    }
                    else {
                        mEtReadLength.setError(null, mDrawableError);
                        mIsCheckReadLength = false;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            });



            this.mBtnStart = (Button)this.mDemoURView.findViewById(R.id.demoUR_btnStart);
            this.mBtnStart.setOnClickListener(new View.OnClickListener() {
                HashMap<String, String> _item;
                byte[] _d;

                @Override
                public void onClick(View view) {
                    if (!m_bDemoURToggle)
                    {
                        if (((MainActivity) mContext).isConnected()) {
                            mProcessList.clear();

                            if (cbSelect.isChecked()) {
                                if (mIsCheckSelectAddress && mIsCheckSelectLength && mIsCheckSelectData) {
                                    _d = mReaderService.T(String.valueOf(mSpinnerMemorySelect.getSelectedItemPosition()),
                                            mEtSelectAddress.getText().toString(),
                                            mEtSelectLength.getText().toString(),
                                            mEtSelectData.getText().toString());
                                    _item = new HashMap<String, String>();
                                    _item.put(PROCESS_COMMAND, "T");
                                    _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                                    mProcessList.add(_item);
                                }
                                else {
                                    Toast.makeText(mContext, "SELECT(T) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            if (cbAccess.isChecked()) {
                                if (!mIsCheckAccessPassword) {
                                    Toast.makeText(mContext, "ACCESS(P) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                _d = mReaderService.P(mEtAccessPassword.getText().toString());
                                _item = new HashMap<String, String>();
                                _item.put(PROCESS_COMMAND, "P");
                                _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                                mProcessList.add(_item);
                            }

                            if (cbRead.isChecked()) {
                                if (mIsCheckReadAddress && mIsCheckReadLength) {
                                    if (cbSlot.isChecked()) {
                                        try {
                                            _d = mReaderService.UR((mSpinnerSlotQ.getSelectedItemPosition() == 0) ? null : Integer.toString(mSpinnerSlotQ.getSelectedItemPosition(), 16),
                                                    String.valueOf(mSpinnerMemoryRead.getSelectedItemPosition()),
                                                    mEtReadAddress.getText().toString(),
                                                    mEtReadLength.getText().toString());
                                        }
                                        catch (IllegalArgumentException iae) {
                                            Toast.makeText(mContext, iae.getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                    }
                                    else {
                                        try {
                                            _d = mReaderService.UR(null,
                                                    Integer.toString(mSpinnerSlotQ.getSelectedItemPosition(), 16),
                                                    mEtReadAddress.getText().toString(),
                                                    mEtReadLength.getText().toString());
                                        }
                                        catch (IllegalArgumentException iae) {
                                            Toast.makeText(mContext, iae.getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                    }
                                    _item = new HashMap<String, String>();
                                    _item.put(PROCESS_COMMAND, "UR");
                                    _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                                    mProcessList.add(_item);
                                }
                                else {
                                    Toast.makeText(mContext, "READ(R) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            else {
                                if (cbSlot.isChecked()) {
                                    try {
                                        _d = mReaderService.U((mSpinnerSlotQ.getSelectedItemPosition() == 0) ? null: Integer.toString(mSpinnerSlotQ.getSelectedItemPosition(), 16));
                                    }
                                    catch (IllegalArgumentException iae) {
                                        Toast.makeText(mContext, iae.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                else {
                                    _d = mReaderService.U();
                                }
                                _item = new HashMap<String, String>();
                                _item.put(PROCESS_COMMAND, "U");
                                _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                                mProcessList.add(_item);
                            }

                            m_bDemoURToggle = true;
                            mBtnStart.setText("STOP");
                            mProgressBar.setVisibility(View.VISIBLE);
                            mDemoURHandler.post(mRunnableBackground);
                        }
                        else {
                            Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        initDemoUR();
                    }
                }
            });

            this.mBtnClear = (Button)this.mDemoURView.findViewById(R.id.demoUR_btnClear);
            this.mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRunCount = 0;
                    if (mDemoURs != null)
                        mDemoURs.clear();
                    if (mDemoURListAdapter != null)
                        mDemoURListAdapter.notifyDataSetChanged();
                    mTextViewCount.setText("");
                    mTextViewTagCount.setText("");


                }
            });


            this.mProgressBar = (ProgressBar)this.mDemoURView.findViewById(R.id.demoUR_progressBar);
            this.mProgressBar.setVisibility(View.GONE);

            this.mCustomKeyboardManager.attachTo(this.mEtSelectAddress, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtSelectLength, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtSelectData, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtAccessPassword, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtReadAddress, HexKeyboard);
            this.mCustomKeyboardManager.attachTo(this.mEtReadLength, HexKeyboard);
        }
        return this.mDemoURView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            initDemoUR();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        initDemoUR();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mDemoURMsgReceiver);
        mContext = null;
    }


    /**
     * */
    public class DemoURMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OTGService.OTG_ACTION_DISCONNECTED_DEMOUR:
                    if (m_bDemoURToggle) {
                        initDemoUR();
                        //startAutoReceiveBackground();
                        Toast.makeText(mContext, intent.getExtras().getString(OTGService.STRING_DATA), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    /***
     *
     */
    private void initDemoUR() {
        m_bDemoURToggle = false;
        mBtnStart.setText("RUN");
        mProgressBar.setVisibility(View.GONE);
        mDemoURHandler.removeCallbacks(mRunnableBackground);
    }

    private Handler mDemoURHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean _bCompare = false;
            int _runCount;

            switch (msg.what) {
                case 1:
                    //mTextViewCount.setText(Integer.toString((int)msg.obj));
                    break;
                case 3://UR
                    _runCount = msg.arg1;
                    String[] arrayUR = (ReaderService.Format.removeCRLFandTarget((String)msg.obj, "U")).split(",");

                    //if (((String) msg.obj).length() > 8) {
                        if (arrayUR[0].length() > 4) {
                            try {
                                if (ReaderService.Format.crc16(new BigInteger(arrayUR[0],16).toByteArray()) != 0x1D0F)
                                    break;
                            }
                            catch (NumberFormatException ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            DemoUR _demoUR;
                            if (arrayUR.length == 1) {
                                _demoUR = new DemoUR(
                                        arrayUR[0].substring(0, 4),
                                        arrayUR[0].substring(4, arrayUR[0].length() - 4),
                                        arrayUR[0].substring(arrayUR[0].length() - 4),
                                        "");
                            }
                            else
                            {
                                arrayUR[1] = arrayUR[1].replace("R", "");
                                _demoUR = new DemoUR(
                                        arrayUR[0].substring(0, 4),
                                        arrayUR[0].substring(4, arrayUR[0].length() - 4),
                                        arrayUR[0].substring(arrayUR[0].length() - 4),
                                        (arrayUR[1].length() > 0) ? arrayUR[1] : "");

                            }

                            if (mDemoURListAdapter.getCount() > 0) {
                                for (int j = 0; j < mDemoURListAdapter.getCount(); j ++)
                                {
                                    if (mDemoURs.get(j).EPC().equals(_demoUR.EPC()) &&
                                            mDemoURs.get(j).CRC16().equals(_demoUR.CRC16()) &&
                                            mDemoURs.get(j).MemRead().equals(_demoUR.MemRead())) {

                                        int _number = Integer.valueOf(mDemoURs.get(j).Count()) + 1;
                                        _demoUR.Count(String.valueOf(_number));
                                        //_demoUR.Percentage(String.valueOf((int)(_number * 100 / _runCount)) + "%");
                                        updateView(false, j, _demoUR);
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
                                _demoUR.Count(String.valueOf(1));
                                //_demoUR.Percentage(String.valueOf((int)(1 * 100 / _runCount)) + "%");
                                updateView(true, 0, _demoUR);
                            }
                            if (!_bCompare) {
                                _demoUR.Count(String.valueOf(1));
                                //_demoUR.Percentage(String.valueOf((int)(1 * 100 / _runCount)) + "%");
                                updateView(true, mDemoURListAdapter.getCount(), _demoUR);
                            }
                        }
                        else {
                            for (int j = 0; j < mDemoURListAdapter.getCount(); j ++)
                            {
                                int count = Integer.valueOf(mDemoURs.get(j).Count());
                                mDemoURs.get(j).Percentage((int)(count * 100 / mRunCount) + "%");
                            }
                            updateView();

                        }
                    /*}
                    else {
                        if (mDemoURListAdapter.getCount() > 0) {
                            for (int k = 0; k <  mDemoURListAdapter.getCount(); k++) {
                                DemoUR __demoUR = mDemoURs.get(k);
                                __demoUR.Percentage(String.valueOf((int)(Integer.parseInt(__demoUR.Count()) * 100 / _runCount)) + "%");
                                updateView(false, k, __demoUR);
                            }
                        }
                    }*/
                    break;
                case 2://U
                    _runCount = msg.arg1;
                    //String[] arrayU = ((String)msg.obj).split(ReaderService.COMMAND_END);

                    //for (int i = 0 ; i < arrayU.length ; i ++) {
                    if (((String) msg.obj).regionMatches(1, "U", 0, 1) && ((String) msg.obj).length() > 4) {

                        final String str = ReaderService.Format.removeCRLFandTarget((String)msg.obj, "U");
                        //if (str.indexOf("U") != -1)
                        //    break;
                        try {
                            if (ReaderService.Format.crc16(new BigInteger(str,16).toByteArray()) != 0x1D0F)
                                break;
                        }
                        catch (NumberFormatException ex) {
                            Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }


                        DemoUR _demoU2 = new DemoUR(
                                str.substring(0, 4),
                                str.substring(4, str.length() - 4),
                                str.substring(str.length() - 4),
                                "");

                        if (mDemoURListAdapter.getCount() > 0) {
                            for (int j = 0; j < mDemoURListAdapter.getCount(); j ++)
                            {
                                if (mDemoURs.get(j).EPC().equals(_demoU2.EPC()) &&
                                        mDemoURs.get(j).CRC16().equals(_demoU2.CRC16()) &&
                                        mDemoURs.get(j).MemRead().equals(_demoU2.MemRead())) {

                                    int _number = Integer.valueOf(mDemoURs.get(j).Count()) + 1;
                                    _demoU2.Count(String.valueOf(_number));
                                    //_demoU2.Percentage(String.valueOf((int)(_number * 100 / _runCount)) + "%");
                                    updateView(false, j, _demoU2);
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
                            _demoU2.Count(String.valueOf(1));
                            //_demoU2.Percentage(String.valueOf((int)(1 * 100 / _runCount)) + "%");
                            updateView(true, 0, _demoU2);
                        }
                        if (!_bCompare) {
                            _demoU2.Count(String.valueOf(1));
                            //_demoU2.Percentage(String.valueOf((int)(1 * 100 / _runCount)) + "%");
                            updateView(true, mDemoURListAdapter.getCount(), _demoU2);
                        }
                    }
                    else {
                        for (int j = 0; j < mDemoURListAdapter.getCount(); j ++)
                        {
                            int count = Integer.valueOf(mDemoURs.get(j).Count());
                            mDemoURs.get(j).Percentage((int)(count * 100 / mRunCount) + "%");
                        }
                        updateView();

                    }
                    //}
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     *
     * @param action true is add view, otherwise is set view.
     * @param position view position.
     * @param demoUR demoU class
     */
    private void updateView(boolean action, int position, DemoUR demoUR) {

        if (action)
            mDemoURs.add(position, demoUR);
        else
            mDemoURs.set(position, demoUR);
        //mDemoURListAdapter.notifyDataSetChanged();

        mTextViewTagCount.setText(Integer.toString(mDemoURListAdapter.getCount()));
    }

    private void updateView() {
        mDemoURListAdapter.notifyDataSetChanged();
        /*mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mDemoUListAdapter.notifyDataSetChanged();
            }
        });*/

    }


    StringBuilder _SubRaw = new StringBuilder(256);
    Byte _SubRawOld = 0;
    private Runnable mRunnableBackground = new Runnable() {

        @Override
        public void run() {

            new Thread(new Runnable() {
                int _timeOutX2 = 500, _index, _runCount, _processIndex = 0;
                int _timeOut = 3000;
                int _error = 0;
                boolean _processEnd = false;
                byte[] _bsData;
                String _strData;
                String strSubData = "";
                HashMap<String, String> _item;
                @Override
                public void run() {

                    while(m_bDemoURToggle) {


                        mRunCount++;
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                mTextViewCount.setText(Integer.toString(mRunCount));
                                //mTextViewTagCount.setText(Integer.toString(mDemoURListAdapter.getCount()));
                            }
                        });
                        _runCount = mRunCount;

                        if (!((MainActivity) mContext).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    initDemoUR();
                                }
                            });
                            return;
                        }

                        if (_processIndex == mProcessList.size())
                            _processIndex = 0;

                        _timeOut = 3000;
                        _error = 0;
                        _processEnd = false;
                        //[TX]
                        _item = mProcessList.get(_processIndex);
                        ((MainActivity) mContext).sendData(ReaderService.Format.stringToBytes(_item.get(PROCESS_DATA)));

                        //[RX]
                        while(_timeOut > 1 && !_processEnd) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mContext).checkData() > 0) {
                                _bsData = ((MainActivity) mContext).getData();
                                if (_bsData != null) {
                                    _timeOut = 3000;
                                    for (int i = 0; i < _bsData.length; i++) {
                                        if (_bsData[i] != 0) {
                                            _SubRaw.append((char) _bsData[i]);
                                            if (_bsData[i] == 0x0A) {
                                                if (_SubRawOld == 0x0D) {
                                                    _strData = _SubRaw.toString();

                                                    switch(mProcessList.get(_processIndex).get(PROCESS_COMMAND)) {
                                                        case "T":
                                                            if (_strData.indexOf("T") != -1) {
                                                                _error = 0;
                                                            }
                                                            else {
                                                                _error = -2;
                                                            }
                                                            _processEnd = true;
                                                            _SubRaw.setLength(0);
                                                            break;
                                                        case "P":
                                                            if (_strData.indexOf("P") != -1) {
                                                                _error = 0;
                                                            }
                                                            else {
                                                                _error = -2;
                                                            }
                                                            _processEnd = true;
                                                            _SubRaw.setLength(0);
                                                            break;
                                                        case "U":
                                                            Message msg = new Message();
                                                            msg.what = 2;
                                                            msg.arg1 = _runCount;
                                                            msg.obj = _SubRaw.toString();
                                                            Log.d("UUU", (String) msg.obj);
                                                            if (msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                                _error = 0;
                                                                _processEnd = true;
                                                            }
                                                            mDemoURHandler.sendMessage(msg);

                                                            _SubRaw.setLength(0);
                                                            break;
                                                        case "UR":
                                                            Message _msg = new Message();
                                                            _msg.what = 3;
                                                            _msg.arg1 = _runCount;
                                                            _msg.obj = _SubRaw.toString();
                                                            Log.d("UUU", (String) _msg.obj);
                                                            if (_msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                                _error = 0;
                                                                _processEnd = true;
                                                            }
                                                            mDemoURHandler.sendMessage(_msg);
                                                            _SubRaw.setLength(0);
                                                            break;
                                                        default:

                                                            break;
                                                    }
                                                }
                                            }
                                            _SubRawOld = _bsData[i];
                                        }//if (_bsData[i] != 0)
                                    }//for
                                }//if (_bsData != null)
                            }//if (((MainActivity) mContext).checkData() > 0)
                            _timeOut--;
                        }//while(_timeOut > 1 && !_processEnd)

                        if (_timeOut <= 0) _error = -3;
                        //error process
                        switch(_error) {
                            case -1:
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext, "No data callback. Stop the process", Toast.LENGTH_SHORT).show();
                                        initDemoUR();
                                    }
                                });
                                return;
                            case -2:
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext,
                                                String.format("Process [%s] data callback error: %s. Stop the process.",
                                                        mProcessList.get(_processIndex).get(PROCESS_COMMAND),
                                                        ReaderService.Format.showCRLF(_strData)), Toast.LENGTH_SHORT).show();
                                        initDemoUR();
                                    }
                                });
                                return;
                            case -3:
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext, "Communications Timeout: 3s", Toast.LENGTH_SHORT).show();
                                        initDemoUR();
                                    }
                                });
                                return;
                        }
                        _processIndex++;
                    }
                }
            }).start();
        }
    };


    private void softKeyboardHidden(EditText et){
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            et.setInputType(InputType.TYPE_NULL);
        }
        else{
            this.mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            Class<EditText> cls = EditText.class;

            try {
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(et, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
