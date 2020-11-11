package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.HorizontalPickerView;
import com.favepc.reader.service.ReaderModule;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce_Chiang on 2017/3/9.
 */

@SuppressLint("ValidFragment")
public class RegularFragment extends Fragment {

    private static final int PROC_READ_VERSION		    = 0x01;
    private static final int PROC_READ_REGULATION		= 0x02;
    private static final int PROC_READ_MODE_AND_CHANNEL	= 0x03;
    private static final int PROC_READ_FREQ_OFFSET		= 0x04;
    private static final int PROC_READ_POWER			= 0x05;
    private static final int PROC_SET_POWER				= 0x06;

    private Context	mContext;
    private Activity mActivity;
    private AppContext mAppContext;
    private View mRegularView = null;
    private LayoutInflater mLayoutInflater;

    private ReaderService mReaderService;
    private Handler mHandler;

    private TextView mTvVersion, mTvArea, mTvFrequency, mTvOffset, mTvPower;
    private HorizontalPickerView mHorizontalPickerView;
    private Button mUpdate, mPowerSet;
    private ProgressBar mProgressBar;
    private TextView mTvPowerStatus;


    private int mPowerValue;
    private ReaderModule.Version mVersionFW = ReaderModule.Version.FI_RXXXX;
    private ReaderModule.Type mType = ReaderModule.Type.Normal;
    private int	mArea = 5;

    private int	mProcessStatus = PROC_READ_VERSION;

    public RegularFragment() {
        super();
    }
    public RegularFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mReaderService = new ReaderService();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mRegularView == null) {
            this.mLayoutInflater = inflater;
            this.mRegularView = inflater.inflate(R.layout.fragment_regular, container, false);

            this.mHandler = new Handler();

            //info
            this.mTvVersion = (TextView) this.mRegularView.findViewById(R.id.regular_info_version);
            this.mTvArea = (TextView) this.mRegularView.findViewById(R.id.regular_info_area);
            this.mTvFrequency = (TextView) this.mRegularView.findViewById(R.id.regular_info_frequency);
            this.mTvOffset = (TextView) this.mRegularView.findViewById(R.id.regular_info_offset);
            this.mTvPower = (TextView) this.mRegularView.findViewById(R.id.regular_info_power);
            this.mProgressBar = (ProgressBar)this.mRegularView.findViewById(R.id.regular_info_progressBar);
            this.mProgressBar.setVisibility(View.GONE);
            this.mUpdate = (Button) this.mRegularView.findViewById(R.id.regular_info_update);
            this.mUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((MainActivity) mContext).isConnected()) {

                        mProgressBar.setVisibility(View.VISIBLE);
                        mTvVersion.setText("");
                        mTvArea.setText("");
                        mTvFrequency.setText("");
                        mTvOffset.setText("");
                        mTvPower.setText("");
                        mPowerSet.setEnabled(false);

                        mProcessStatus = PROC_READ_VERSION;
                        mProcessEvent.post(mProcessRunnable);
                    }
                    else {
                        Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //power
            this.mHorizontalPickerView = (HorizontalPickerView) this.mRegularView.findViewById(R.id.regular_power_pickerview);
            this.mHorizontalPickerView.initViewParam(
                            ReaderModule.DataRepository.GetPowerMin(mVersionFW),
                            ReaderModule.DataRepository.GetPowerMax(mVersionFW));
            this.mHorizontalPickerView.setValueChangeListener(new HorizontalPickerView.OnValueChangeListener() {
                @Override
                public void onValueChange(float value) {
                    mPowerValue = ReaderModule.DataRepository.GetPowerValue(mVersionFW, (int)value);
                }
            });

            this.mPowerSet = (Button) this.mRegularView.findViewById(R.id.regular_power_set);
            this.mPowerSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProcessStatus = PROC_SET_POWER;
                    mTvPowerStatus.setText("");
                    mProcessEvent.post(mProcessRunnable);
                }
            });
            this.mPowerSet.setEnabled(false);

            this.mTvPowerStatus = (TextView) this.mRegularView.findViewById(R.id.regular_power_set_status);

        }
        return this.mRegularView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            init();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        init();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }



    /***
     *
     */
    private void init() {
        this.mProgressBar.setVisibility(View.GONE);
        mProcessEvent.removeCallbacks(mProcessRunnable);
    }




    private Handler mProcessEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = "";
            switch(msg.what) {
                case 2://[RX]
                    switch(mProcessStatus) {
                        case PROC_READ_VERSION:
                            mTvVersion.setText(ReaderService.Format.removeCRLFandTarget((String) msg.obj, "V"));
                            try {
                                mVersionFW = ReaderModule.check(ReaderService.Format.hexStringToInt(((String) msg.obj).substring(2, 6)));
                                switch(mVersionFW) {
                                    case FI_R3008:
                                    case FI_RXXXX:
                                        this.removeCallbacks(mProcessRunnable);
                                        mProgressBar.setVisibility(View.GONE);
                                        mPowerSet.setEnabled(false);
                                        Toast.makeText(mContext, "This version cannot check other information", Toast.LENGTH_SHORT).show();
                                        break;
                                    case FI_R300A_C1:
                                    case FI_R300A_C2:
                                    case FI_R300A_C2C4:
                                    case FI_R300A_C3:
                                    case FI_R300A_C2C5:
                                    case FI_R300A_C2C6:
                                    case FI_R300A_C3C5:
                                    case FI_R300T_D1:
                                    case FI_R300T_D2:
                                    case FI_R300T_D204:
                                    case FI_R300T_D205:
                                    case FI_R300T_D206:
                                    case FI_R300S:
                                    case FI_A300S:
                                    case FI_R300S_D305:
                                    case FI_R300S_D306:
                                    case FI_R300V_D406:
                                    case FI_R300A_H:
                                    case FI_R300T_H:
                                    case FI_R300V_H:
                                    case FI_R300S_H:
                                        mHorizontalPickerView.initViewParam(
                                                ReaderModule.DataRepository.GetPowerMin(mVersionFW),
                                                ReaderModule.DataRepository.GetPowerMax(mVersionFW));
                                        mProcessStatus = PROC_READ_REGULATION;
                                        this.post(mProcessRunnable);
                                        break;
                                }
                            }
                            catch (ArrayIndexOutOfBoundsException ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case PROC_READ_REGULATION:
                            s = (String) msg.obj;
                            int index = s.indexOf("N");
                            if (index != -1) {
                                switch (s.substring(index + 2, index + 3)) {
                                    case "1": mTvArea.setText("US (902~928)"); mArea = 1; break;
                                    case "2": mTvArea.setText("TW (922~928)"); mArea = 2; break;
                                    case "3": mTvArea.setText("CN (920~925)"); mArea = 3; break;
                                    case "4": mTvArea.setText("CN2 (840~845)"); mArea = 4; break;
                                    case "5": mTvArea.setText("EU (865~868)"); mArea = 5; break;
                                    case "6": mTvArea.setText("JP (916~921)"); mArea = 6; break;
                                    case "7": mTvArea.setText("KR (917~921)"); mArea = 7; break;
                                    case "8": mTvArea.setText("VN (918~923)"); mArea = 8; break;
                                    case "9": mTvArea.setText("EU2 (916~920)"); mArea = 9; break;
                                    case "A": mTvArea.setText("IN (865~867)"); mArea = 10; break;
                                }
                            }
                            mProcessStatus = PROC_READ_MODE_AND_CHANNEL;
                            this.post(mProcessRunnable);
                            break;
                        case PROC_READ_MODE_AND_CHANNEL:
                            byte[] bd = null;
                            switch (mVersionFW) {
                                case FI_R300A_C1:
                                case FI_R300T_D1:
                                    bd = (byte[]) msg.obj;
                                    break;
                                case FI_R300A_C2:
                                case FI_R300A_C2C4:
                                case FI_R300A_C3:
                                case FI_R300T_D2:
                                case FI_R300T_D204:
                                case FI_R300S:
                                case FI_A300S:
                                case FI_R300A_C2C5:
                                case FI_R300A_C2C6:
                                case FI_R300A_C3C5:
                                case FI_R300T_D205:
                                case FI_R300T_D206:
                                case FI_R300S_D305:
                                case FI_R300S_D306:
                                case FI_R300V_D406:
                                case FI_R300A_H:
                                case FI_R300T_H:
                                case FI_R300V_H:
                                case FI_R300S_H:
                                    bd = ReaderService.Format.hexStringToBytes(ReaderService.Format.removeCRLF((String) msg.obj));
                                    break;
                            }
                            if (bd[0] == 0xFF || bd[0] == 0x0) mTvFrequency.setText("hopping");
                            else {
                                String m;
                                double j;
                                int i = (bd[1] > 0) ? bd[1] - 1 : bd[1];
                                switch (mArea) {
                                    case 1:
                                        j = 903.24 + i * 0.48;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 2:
                                        j = 922.84 + i * 0.36;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 3:
                                        j = 920.125 + i * 0.25;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 4:
                                        j = 840.125 + i * 0.25;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 5:
                                        j = 865.7 + i * 0.6;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 6:
                                        j = 916.8 + i * 1.2;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 7:
                                        j = 917.3 + i * 0.6;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 8:
                                        j = 918.84  + i * 0.36;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 9:
                                        j = 916.3  + i * 1.2;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                    case 10:
                                        j = 865.7   + i * 0.6;
                                        s = String.format("%.3fMHz", j);
                                        break;
                                }

                                if (bd[0] == 0x01) {
                                    m = "Carry";
                                }
                                else {
                                    m = "RX";
                                }
                                mTvFrequency.setText(String.format("Fix, %s, %s", m, s));
                            }

                            mProcessStatus = PROC_READ_FREQ_OFFSET;
                            this.post(mProcessRunnable);
                            break;
                        case PROC_READ_FREQ_OFFSET:
                            bd = null;
                            switch (mVersionFW) {
                                case FI_R300A_C1:
                                case FI_R300T_D1:
                                    bd = (byte[]) msg.obj;
                                    break;
                                case FI_R300A_C2:
                                case FI_R300A_C2C4:
                                case FI_R300A_C3:
                                case FI_R300T_D2:
                                case FI_R300T_D204:
                                case FI_R300S:
                                case FI_A300S:
                                case FI_R300A_C2C5:
                                case FI_R300A_C2C6:
                                case FI_R300A_C3C5:
                                case FI_R300T_D205:
                                case FI_R300T_D206:
                                case FI_R300S_D305:
                                case FI_R300S_D306:
                                case FI_R300V_D406:
                                case FI_R300A_H:
                                case FI_R300T_H:
                                case FI_R300V_H:
                                case FI_R300S_H:
                                    bd = ReaderService.Format.hexStringToBytes(ReaderService.Format.removeCRLF((String) msg.obj));
                                    break;
                            }
                            if (bd[0] > 0x01 || bd[0] == -1)    //-1 = 0xFF
                                mTvOffset.setText("N/A");
                            else {
                                String strSymbol = (bd[0] == 0x00) ? "-" : "+";
                                int ii = ((bd[1] << 8) & 0xFF00) + (bd[2] & 0xFF);
                                double db = (double)ii * (double)30.5;
                                mTvOffset.setText(strSymbol + String.format("%.0fHz", db));
                            }
                            mProcessStatus = PROC_READ_POWER;
                            this.post(mProcessRunnable);
                            break;
                        case PROC_READ_POWER:
                            bd = ReaderService.Format.hexStringToBytes(ReaderService.Format.removeCRLFandTarget((String) msg.obj, "N"));
                            if (bd[0] == -1)    //-1 = 0xFF
                                mTvPower.setText("N/A");
                            else {
                                switch (mVersionFW) {

                                    case FI_R300T_D1:
                                    case FI_R300T_D2:
                                    case FI_R300T_D204:
                                    case FI_R300T_D205:
                                    case FI_R300T_D206:
                                    case FI_R300T_H:
                                        if (bd[0] >= 0x1B) {
                                            mTvPower.setText("25dBm");
                                            mHorizontalPickerView.setValue(25);
                                        }
                                        else {
                                            mTvPower.setText(String.valueOf(bd[0] - 2) + "dBm");
                                            mHorizontalPickerView.setValue(bd[0] - 2);
                                        }

                                        break;
                                    case FI_R300A_C1:
                                    case FI_R300A_C2:
                                    case FI_R300A_C2C4:
                                    case FI_R300A_C3:
                                    case FI_R300A_C2C5:
                                    case FI_R300A_C2C6:
                                    case FI_R300A_C3C5:
                                    case FI_R300A_H:
                                        if (bd[0] >= 0x14) {
                                            mTvPower.setText("18dBm");
                                            mHorizontalPickerView.setValue(18);
                                        }
                                        else {
                                            mTvPower.setText(String.valueOf(bd[0] - 2) + "dBm");
                                            mHorizontalPickerView.setValue(bd[0] - 2);
                                        }
                                        break;

                                    case FI_R300S:
                                    case FI_A300S:
                                    case FI_R300S_D305:
                                    case FI_R300S_D306:
                                    case FI_R300S_H:
                                        if (bd[0] >= 0x1B) {
                                            mTvPower.setText("27dBm");
                                            mHorizontalPickerView.setValue(27);
                                        }
                                        else {
                                            mTvPower.setText(String.valueOf(bd[0]) + "dBm");
                                            mHorizontalPickerView.setValue(bd[0]);
                                        }
                                        break;
                                    case FI_R300V_D406:
                                    case FI_R300V_H:
                                        if (bd[0] >= 0x1B) {
                                            mTvPower.setText("29dBm");
                                            mHorizontalPickerView.setValue(29);
                                        }
                                        else {
                                            mTvPower.setText(String.valueOf(bd[0] +2) + "dBm");
                                            mHorizontalPickerView.setValue(bd[0] +2);
                                        }
                                        break;
                                }

                            }
                            this.removeCallbacks(mProcessRunnable);
                            mProgressBar.setVisibility(View.GONE);
                            mPowerSet.setEnabled(true);
                            break;
                        case PROC_SET_POWER:
                            String s1 = ReaderService.Format.removeCRLFandTarget((String) msg.obj, "N");
                            int val = ReaderService.Format.hexStringToInt(s1);
                            if (val == mPowerValue) {
                                mTvPowerStatus.setText("Set Power OK");
                            }
                            else {
                                mTvPowerStatus.setText("Set Power Error");
                            }
                            break;
                    }
                    break;
                case 3://[RX] error
                    switch(mProcessStatus) {
                        case PROC_READ_VERSION:
                            mTvVersion.setText("[err] " + ReaderService.Format.showCRLF((String) msg.obj));
                            break;
                        case PROC_READ_REGULATION:
                            mTvArea.setText("[err] " + ReaderService.Format.showCRLF((String) msg.obj));
                            break;
                        case PROC_READ_MODE_AND_CHANNEL:
                            break;
                        case PROC_READ_FREQ_OFFSET:
                            break;
                        case PROC_READ_POWER:
                            break;
                        case PROC_SET_POWER:
                            switch (mVersionFW) {
                                case FI_R300T_D1:
                                case FI_R300T_D2:
                                case FI_R300T_D204:
                                case FI_R300T_D205:
                                case FI_R300T_D206:
                                case FI_R300T_H:

                                case FI_R300A_C2C4:
                                case FI_R300A_C3:
                                case FI_R300A_C2C5:
                                case FI_R300A_C2C6:
                                case FI_R300A_C3C5:
                                case FI_R300A_H:
                                    break;
                                case FI_R300A_C1:
                                case FI_R300A_C2:
                                    mTvPowerStatus.setText("this version power setting is no feedback, please check status");
                                    break;
                                case FI_R300S:
                                case FI_A300S:
                                case FI_R300S_D305:
                                case FI_R300S_D306:
                                case FI_R300S_H:

                                    break;
                                case FI_R300V_D406:
                                case FI_R300V_H:
                                    break;
                            }
                            break;
                    }
                    this.removeCallbacks(mProcessRunnable);
                    mProgressBar.setVisibility(View.GONE);
                    mPowerSet.setEnabled(true);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Runnable mProcessRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                int _timeOutx10 = 100;
                int index;
                byte[] bsData;
                String strData;
                String strSubData = "";
                boolean _isAck = false;

                @Override
                public void run () {

                    //[TX]
                    switch (mProcessStatus) {
                        case PROC_READ_VERSION:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.V());
                            break;
                        case PROC_READ_REGULATION:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.readRegulation());
                            break;
                        case PROC_READ_MODE_AND_CHANNEL:
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            switch (mVersionFW) {
                                case FI_R300A_C1:
                                case FI_R300T_D1:
                                    mType = ReaderModule.Type.AA;
                                    ((MainActivity) mActivity).sendData(mReaderService.AA("FF04008702"));
                                    break;
                                case FI_R300A_C2:
                                case FI_R300A_C2C4:
                                case FI_R300A_C3:
                                case FI_R300T_D2:
                                case FI_R300T_D204:
                                case FI_R300S:
                                case FI_A300S:
                                case FI_R300A_C2C5:
                                case FI_R300A_C2C6:
                                case FI_R300A_C3C5:
                                case FI_R300T_D205:
                                case FI_R300T_D206:
                                case FI_R300S_D305:
                                case FI_R300S_D306:
                                case FI_R300V_D406:
                                case FI_R300S_H:
                                case FI_R300A_H:
                                case FI_R300V_H:
                                case FI_R300T_H:
                                    mType = ReaderModule.Type.Normal;
                                    ((MainActivity) mActivity).sendData(mReaderService.readFrequencyChannel());
                                    break;
                            }
                            break;
                        case PROC_READ_FREQ_OFFSET:
                            switch (mVersionFW) {
                                case FI_R300A_C1:
                                case FI_R300T_D1:
                                    mType = ReaderModule.Type.AA;
                                    ((MainActivity) mActivity).sendData(mReaderService.AA("FF04008903"));
                                    break;
                                case FI_R300A_C2:
                                case FI_R300A_C2C4:
                                case FI_R300A_C3:
                                case FI_R300T_D2:
                                case FI_R300T_D204:
                                case FI_R300S:
                                case FI_A300S:
                                case FI_R300A_C2C5:
                                case FI_R300A_C2C6:
                                case FI_R300A_C3C5:
                                case FI_R300T_D205:
                                case FI_R300T_D206:
                                case FI_R300S_D305:
                                case FI_R300S_D306:
                                case FI_R300V_D406:
                                case FI_R300S_H:
                                case FI_R300A_H:
                                case FI_R300V_H:
                                case FI_R300T_H:
                                    mType = ReaderModule.Type.Normal;
                                    ((MainActivity) mActivity).sendData(mReaderService.readFrequencyOffset());
                                    break;
                            }
                            break;
                        case PROC_READ_POWER:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.readPower());
                            break;
                        case PROC_SET_POWER:
                            mType = ReaderModule.Type.Normal;
                            ((MainActivity) mActivity).sendData(mReaderService.setPower(Integer.toString(mPowerValue, 16)));
                            break;
                    }

                    //[RX]
                    _timeOutx10 = 100;
                    _isAck = false;
                    if (mType.equals(ReaderModule.Type.Normal)) {
                        while (_timeOutx10 > 1) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (((MainActivity) mActivity).checkData() > 0) {
                                bsData = ((MainActivity) mActivity).getData();
                                if (bsData == null) return;
                                if (bsData.length > 0) {
                                    strData = new String(bsData);
                                    if (strSubData.length() > 0) {
                                        strData = strSubData + strData;
                                        strSubData = "";
                                    }
                                    if ((index = strData.indexOf(ReaderService.COMMAND_END)) != -1) {
                                        _isAck = true;
                                        Message msg = new Message();
                                        msg.what = 2;
                                        msg.obj = strData.substring(0, index + 2);
                                        mProcessEvent.sendMessage(msg);
                                        _timeOutx10 = 1;
                                    } else {
                                        strSubData = strData;
                                        _timeOutx10++;
                                    }
                                }
                            }
                            _timeOutx10--;
                        }
                    }
                    else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        int idx = ((MainActivity) mActivity).checkData();
                        int len = 0;
                        if (idx > 0) {
                            List<byte[]> arrayByte = new ArrayList<>();
                            for (int i = 0; i < idx; i++) {
                                bsData = ((MainActivity) mActivity).getData();
                                len += bsData.length;
                                arrayByte.add(bsData);

                            }
                            byte[] tbsData = new byte[len];
                            len = 0;
                            for (int j = 0; j < idx; j++) {
                                System.arraycopy(arrayByte.get(j), 0, tbsData, len, arrayByte.get(j).length);
                                len += arrayByte.get(j).length;
                            }
                            _isAck = true;
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = tbsData;
                            mProcessEvent.sendMessage(msg);
                        }
                    }


                    if (!_isAck) {
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = strSubData;
                        mProcessEvent.sendMessage(msg);
                    }
                }
            }).start();
        }
    };
}
