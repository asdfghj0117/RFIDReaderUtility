package com.favepc.reader.rfidreaderutility.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.UCode8ListAdapter;
import com.favepc.reader.rfidreaderutility.object.UCode8;
import com.favepc.reader.rfidreaderutility.object.UCode8Mode;
import com.favepc.reader.service.ReaderModule;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;
import java.util.HashMap;


public class DemoUCode8Fragment extends Fragment {


    /**
     * RN16 enable
     */
    static final int PROC_UCODE8_ENABLE_RN16    = 0x01;
    /**
     * RN16 disable
     */
    static final int PROC_UCODE8_DISABLE_RN16   = 0x02;
    /**
     * Brand identifier(BI) enable
     */
    static final int PROC_UCODE8_SELECT_BIBIT	= 0x03;
    /**
     * EPC + TID response
     */
    static final int PROC_UCODE8_INVENTORY_TAG	= 0x04;

    private Context mContext;
    private Activity mActivity;
    private ReaderService mReaderService;
    private View mUCode8View = null;

    ArrayList<UCode8> mUCode8s = new ArrayList<UCode8>();

    private ArrayList<HashMap<String, String>> mProcessList;

    UCode8ListAdapter mUCode8ListAdapter;

    private CheckBox mCheckBoxBiEnable, mCheckBoxEpcTid, mCheckBoxSlot, mCheckBoxUI;
    private Spinner mSpinnerSlotQ, mSpinnerMode;
    private Button mButtonStart;
    private ListView mListViewTag;
    private TextView mTextViewTitle;

    private int	mProcessStatus = PROC_UCODE8_ENABLE_RN16;

    private ReaderModule.Type mType = ReaderModule.Type.Normal;


    /**
     * Default constructor.
     */
    public DemoUCode8Fragment() { super();}

    /**
     * Brand identifier(BI) fragment constructor.
     * @param context The current context.
     * @param activity the Activity this fragment is currently associated with.
     */
    @SuppressLint("ValidFragment")
    public DemoUCode8Fragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    /**
     * called once the fragment is associated with its activity. send RN16 enable command
     * @param context The current context.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mReaderService = new ReaderService();

        if (((MainActivity) mContext).isConnected()) {
            mProcessStatus = PROC_UCODE8_ENABLE_RN16;
            mProcessEvent.post(mProcessRunnable);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mCheckBoxUI.setChecked(true);
    }

    /**
     * create BI view
     * @see UCode8ListAdapter
     * @see UCode8
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mUCode8View == null) {
            this.mProcessList = new ArrayList<>();


            this.mUCode8View = inflater.inflate(R.layout.fragment_ucode_8, container, false);
            //text view title
            this.mTextViewTitle = (TextView) mUCode8View.findViewById(R.id.uCode8_tvtitle);
            //list view tag
            this.mListViewTag = (ListView)mUCode8View.findViewById(R.id.uCode8_lvTags);
            //check box
            this.mCheckBoxBiEnable = (CheckBox) this.mUCode8View.findViewById(R.id.uCode8_checkbox_bi);
            this.mCheckBoxEpcTid = (CheckBox) this.mUCode8View.findViewById(R.id.uCode8_checkbox_epc_tid);
            this.mCheckBoxSlot = (CheckBox) this.mUCode8View.findViewById(R.id.uCode8_checkbox_slotQ);

            //spinner mode
            ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.ucode8_mode,
                    R.layout.spinner_style);
            this.mSpinnerMode = (Spinner) this.mUCode8View.findViewById(R.id.uCode8_spinner_mode);
            this.mSpinnerMode.setAdapter(lunchList);



            //spinner slot Q
            ArrayAdapter<CharSequence> lunchList2 = ArrayAdapter.createFromResource(this.mContext, R.array.slot_q,
                    R.layout.spinner_style);
            this.mSpinnerSlotQ = (Spinner) this.mUCode8View.findViewById(R.id.uCode8_spinner_q);
            this.mSpinnerSlotQ.setAdapter(lunchList2);

            //setting UI
            this.mCheckBoxUI = (CheckBox) this.mUCode8View.findViewById(R.id.uCode8_cbProcess);
            this.mCheckBoxUI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        mCheckBoxBiEnable.setVisibility(View.VISIBLE);
                        mSpinnerMode.setVisibility(View.VISIBLE);
                        mCheckBoxEpcTid.setVisibility(View.VISIBLE);
                        mCheckBoxSlot.setVisibility(View.VISIBLE);
                        mSpinnerSlotQ.setVisibility(View.VISIBLE);
                    }
                    else {
                        mCheckBoxBiEnable.setVisibility(View.GONE);
                        mSpinnerMode.setVisibility(View.GONE);
                        mCheckBoxEpcTid.setVisibility(View.GONE);
                        mCheckBoxSlot.setVisibility(View.GONE);
                        mSpinnerSlotQ.setVisibility(View.GONE);
                    }
                }
            });


            this.mButtonStart = (Button)this.mUCode8View.findViewById(R.id.uCode8_btnStart);
            this.mButtonStart.setOnClickListener(new View.OnClickListener() {
                HashMap<String, String> _item;
                byte[] _d;

                @Override
                public void onClick(View view) {
                    if (mUCode8s != null)
                        mUCode8s.clear();
                    if (mUCode8ListAdapter != null)
                        mUCode8ListAdapter.notifyDataSetChanged();

                    if (mCheckBoxBiEnable.isChecked()) {
                        switch (mSpinnerMode.getSelectedItemPosition()) {
                            case 0:
                                mUCode8ListAdapter = new UCode8ListAdapter(mContext, R.layout.adapter_ucode8, mUCode8s, UCode8Mode.BI_MODE1);
                                mListViewTag.setAdapter(mUCode8ListAdapter);
                                mTextViewTitle.setText(R.string.uCode8_title_mode1);
                                break;
                            case 1:
                                mUCode8ListAdapter = new UCode8ListAdapter(mContext, R.layout.adapter_ucode8, mUCode8s, UCode8Mode.BI_MODE2);
                                mListViewTag.setAdapter(mUCode8ListAdapter);
                                mTextViewTitle.setText(R.string.uCode8_title_mode2);
                                break;
                        }
                    }
                    if (mCheckBoxEpcTid.isChecked()) {
                        mTextViewTitle.setText(R.string.uCode8_title_epc_tid);
                        mUCode8ListAdapter = new UCode8ListAdapter(mContext, R.layout.adapter_ucode8, mUCode8s, UCode8Mode.EPC_TID);
                        mListViewTag.setAdapter(mUCode8ListAdapter);
                    }


                    if (((MainActivity) mContext).isConnected()) {
                        mButtonStart.setClickable(false);
                        mProcessStatus = PROC_UCODE8_SELECT_BIBIT;
                        mProcessEvent.post(mProcessRunnable);
                    }
                    else {
                        Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Use check box as Radio buttons to allow one option(BI enable or EPC+TID response) from a set.
            this.mCheckBoxBiEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        mCheckBoxEpcTid.setChecked(false);
                        mSpinnerMode.setEnabled(true);
                    }
                    else {
                        mSpinnerMode.setEnabled(false);
                    }
                }
            });
            this.mCheckBoxEpcTid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mCheckBoxBiEnable.setChecked(false);
                    }

                }
            });
        }
        return this.mUCode8View;
    }

    /**
     * when the hidden state of the fragment has changed. call RN16 enable or disable command
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (((MainActivity) mContext).isConnected()) {
                /**
                 * leaving this fragment, set RN16 disable.
                 * @see #mProcessRunnable -> PROC_UCODE8_DISABLE_RN16 case
                 */
                mProcessStatus = PROC_UCODE8_DISABLE_RN16;
                mProcessEvent.post(mProcessRunnable);
            }
            //init();
        }
        else {
            if (((MainActivity) mContext).isConnected()) {
                /**
                 * into this fragment, set RN16 enable.
                 * @see #mProcessRunnable  -> PROC_UCODE8_ENABLE_RN16 case
                 */
                mProcessStatus = PROC_UCODE8_ENABLE_RN16;
                mProcessEvent.post(mProcessRunnable);
            }
        }
    }


    private void init() {
        mProcessEvent.removeCallbacks(mProcessRunnable);
    }


    /**
     * Use handler to send and process Message and Runnable objects associated with a thread's MessageQueue.
     */
    private Handler mProcessEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = "", s2 = "";
            UCode8 uCode8;

            switch(msg.what) {
                case 1://[TX]]
                    uCode8 = new UCode8(false, (String) msg.obj,
                                null,
                                null);
                    mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8);
                    mUCode8ListAdapter.notifyDataSetChanged();
                    break;
                case 2://[RX]]
                    switch(mProcessStatus) {
                        case PROC_UCODE8_ENABLE_RN16:
                            s = (String) msg.obj;
                            int indexS = s.indexOf("S");
                            if (indexS != -1) {
                                //mProcessStatus = PROC_UCODE8_SELECT_BIBIT;
                                //this.post(mProcessRunnable);
                                Toast toastEn;
                                toastEn = Toast.makeText(mContext, "RN16 is Enable.", Toast.LENGTH_SHORT);
                                toastEn.setGravity(Gravity.CENTER, 0, 20);
                                toastEn.show();
                            }
                            this.removeCallbacks(mProcessRunnable);
                            break;
                        case PROC_UCODE8_DISABLE_RN16:
                            s2 = (String) msg.obj;
                            int indexS2 = s2.indexOf("S");
                            if (indexS2 != -1) {
                                Toast toastDis;
                                toastDis = Toast.makeText(mContext, "RN16 is Disable.", Toast.LENGTH_SHORT);
                                toastDis.setGravity(Gravity.CENTER, 0, 20);
                                toastDis.show();
                            }
                            this.removeCallbacks(mProcessRunnable);
                            break;

                        case PROC_UCODE8_SELECT_BIBIT:
                            s = (String) msg.obj;

                            //Brand identifier(BI) enable
                            if (mCheckBoxBiEnable.isChecked()) {
                                switch (mSpinnerMode.getSelectedItemPosition()) {
                                    //Mode 1
                                    case 0:
                                        uCode8 = new UCode8(true, ReaderService.Format.removeCRLF(s),
                                                null,
                                                null,
                                                null);
                                        mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8);
                                        break;
                                    //Mode 2
                                    case 1:
                                        uCode8 = new UCode8(true, ReaderService.Format.removeCRLF(s),
                                                null,
                                                null);
                                        mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8);
                                        break;
                                }
                                mUCode8ListAdapter.notifyDataSetChanged();
                            }
                            //Brand identifier(BI) disable
                            else {
                                uCode8 = new UCode8(true,ReaderService.Format.removeCRLF(s),
                                        null,
                                        null,
                                        null,
                                        null);
                                mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8);
                                mUCode8ListAdapter.notifyDataSetChanged();
                            }
                            int indexT = s.indexOf("T");
                            if (indexT != -1) {
                                mProcessStatus = PROC_UCODE8_INVENTORY_TAG;
                                this.post(mProcessRunnable);
                            }
                            break;

                        case PROC_UCODE8_INVENTORY_TAG:
                            if (((String) msg.obj).regionMatches(1, "U", 0, 1) && ((String) msg.obj).length() > 4) {
                                final String strRaw = ReaderService.Format.removeCRLFandTarget((String)msg.obj, "U");
                                //Brand identifier(BI) enable
                                if (mCheckBoxBiEnable.isChecked()) {

                                    switch (mSpinnerMode.getSelectedItemPosition()) {
                                        //Mode 1
                                        case 0:
                                            UCode8 uCode8_1 = new UCode8(true,null,
                                                    strRaw.substring(4, 8),
                                                    strRaw.substring(8, strRaw.length() - 4),
                                                    strRaw.substring(0, 4));
                                            mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8_1);
                                            break;
                                        //Mode 2
                                        case 1:
                                            int rn16 = ReaderService.Format.hexStringToInt(strRaw.substring(0, 4));
                                            int epc_end_word = ReaderService.Format.hexStringToInt(strRaw.substring(strRaw.length() - 8, strRaw.length() - 4));
                                            String bi = Integer.toHexString(rn16 ^ epc_end_word);

                                            UCode8 uCode8_2 = new UCode8(true, null,
                                                    strRaw.substring(4, 8),
                                                    strRaw.substring(8, strRaw.length() - 8) + bi);
                                            mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8_2);
                                            break;
                                    }
                                    mUCode8ListAdapter.notifyDataSetChanged();
                                }
                                //Brand identifier(BI) disable
                                else {
                                    UCode8 uCode8_EpcTid = new UCode8(true,null,
                                            strRaw.substring(4, 8),
                                            strRaw.substring(8, strRaw.length() - 28),
                                            strRaw.substring(strRaw.length() - 28, strRaw.length() - 4),
                                            strRaw.substring(0, 4));
                                    mUCode8s.add(mUCode8ListAdapter.getCount(), uCode8_EpcTid);
                                    mUCode8ListAdapter.notifyDataSetChanged();
                                }
                            }
                            else {
                                mButtonStart.setClickable(true);
                            }




                            this.removeCallbacks(mProcessRunnable);
                            break;
                    }
                    break;
                case 3:
                    Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    mButtonStart.setClickable(true);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private StringBuilder _SubRaw = new StringBuilder(512);
    private Byte _SubRawOld = 0;

    /**
     * implement command process in thread
     */
    private Runnable mProcessRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int _timeOutx10 = 200;
                    boolean _isAck = false;
                    int index;
                    byte[] bsData;
                    String strData;
                    String strSubData = "";
                    UCode8 uCode8;
                    Message msg;

                    //[TX]
                    /**
                     * sendData() : send command
                     * @see MainActivity sendData function
                     */
                    switch (mProcessStatus) {
                        case PROC_UCODE8_ENABLE_RN16:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.raw("S3,1"));
                            break;
                        case PROC_UCODE8_DISABLE_RN16:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.raw("S3,0"));
                            break;
                        case PROC_UCODE8_SELECT_BIBIT:
                            mType = ReaderModule.Type.Normal;
                            //EPC+TID (NXP U8) with RN16
                            if (mCheckBoxEpcTid.isChecked()) {
                                ((MainActivity) mActivity).sendData(mReaderService.raw("T1,203,1,8"));
                                msg = new Message();
                                msg.what = 1;
                                msg.obj = "T1,203,1,8";
                                mProcessEvent.sendMessage(msg);
                            }
                            else {
                                ((MainActivity) mActivity).sendData(mReaderService.raw("T1,204,1,8"));
                                msg = new Message();
                                msg.what = 1;
                                msg.obj = "T1,204,1,8";
                                mProcessEvent.sendMessage(msg);
                            }
                            break;
                        case PROC_UCODE8_INVENTORY_TAG:
                            mType = ReaderModule.Type.Normal;

                            if (mCheckBoxSlot.isChecked()) {
                                try {
                                    byte[] b = mReaderService.U((mSpinnerSlotQ.getSelectedItemPosition() == 0) ? null: Integer.toString(mSpinnerSlotQ.getSelectedItemPosition(), 16));
                                    msg = new Message();
                                    msg.what = 1;
                                    msg.obj = ReaderService.Format.removeCRLF(ReaderService.Format.bytesToString(b));
                                    mProcessEvent.sendMessage(msg);
                                    ((MainActivity) mActivity).sendData(b);
                                } catch (IllegalArgumentException e) {
                                    msg = new Message();
                                    msg.what = 3;
                                    msg.obj = e.getMessage();
                                    mProcessEvent.sendMessage(msg);
                                    return;
                                }

                            }
                            else {
                                msg = new Message();
                                msg.what = 1;
                                msg.obj = ReaderService.Format.removeCRLF(ReaderService.Format.bytesToString(mReaderService.U()));
                                mProcessEvent.sendMessage(msg);
                                ((MainActivity) mActivity).sendData(mReaderService.U());
                            }

                            break;
                    }

                    //[RX]
                    /**
                     * receive command
                     */
                    _timeOutx10 = 200;
                    _isAck = false;
                    if (mType.equals(ReaderModule.Type.Normal)) {
                        while (_timeOutx10 > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (((MainActivity) mActivity).checkData() > 0) {
                                bsData = ((MainActivity) mContext).getData();
                                if (bsData != null) {
                                    _timeOutx10++;
                                    for (int i = 0; i < bsData.length; i++) {
                                        if (bsData[i] != 0) {
                                            _SubRaw.append((char) bsData[i]);
                                            /**
                                             * check <CR><LF>
                                             * @see UHF RFID Reader Protocol NXP U8_RN16.pdf -> chapter 2.0 ASCII Protocol Commands
                                             */
                                            if (bsData[i] == 0x0A) {
                                                if (_SubRawOld == 0x0D) {
                                                    _isAck = true;
                                                    String str = _SubRaw.toString();

                                                    /**
                                                     * show data
                                                     * @see mProcessEvent handler
                                                     */
                                                    msg = new Message();
                                                    msg.what = 2;
                                                    msg.obj = str;
                                                    mProcessEvent.sendMessage(msg);

                                                    if (str.equals(ReaderService.COMMANDU_END)) {
                                                        _timeOutx10 = 0;
                                                    }
                                                    else {
                                                        if (str.regionMatches(1, "S", 0, 1) ||
                                                                str.regionMatches(1, "T", 0, 1))
                                                        {
                                                            _timeOutx10 = 0;
                                                        }
                                                    }
                                                    _SubRaw.setLength(0);
                                                }
                                            }
                                            _SubRawOld = bsData[i];
                                        }
                                    }
                                }
                            }
                            _timeOutx10--;
                        }
                    }

                    if (!_isAck) {
                        msg = new Message();
                        msg.what = 3;
                        msg.obj = _SubRaw.toString();
                        mProcessEvent.sendMessage(msg);
                    }
                }
            }).start();
        }
    };
}
