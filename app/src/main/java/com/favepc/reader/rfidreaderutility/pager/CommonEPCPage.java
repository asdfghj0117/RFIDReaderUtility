package com.favepc.reader.rfidreaderutility.pager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.service.ReaderService;

/**
 * Created by Bruce_Chiang on 2017/3/27.
 */

public class CommonEPCPage {

    public static final String COMMON_ACTION_EPC_REPEAT = "COMMON_ACTION_EPC_REPEAT";
    public static final String COMMON_ACTION_EPC_END = "COMMON_ACTION_EPC_END";
    public static final String COMMON_ACTION_EPC_ONE = "COMMON_ACTION_EPC_ONE";
    public static final String STRING_DATA = "STRING_DATA";

    private Context 			mContext;
    private AppContext          mAppContext;
    private Activity            mActivity;
    private LayoutInflater 		mInflater;
    private ReaderService       mReaderService;
    private View                mViewEpc;
    private TextView            mTextView;
    private Button              mButton;
    private CheckBox            mCheckBox;
    private ProgressBar         mProgressBar;
    private boolean 		    mCheckBoxFlag = false;
    private boolean 		    mRunningFlag = false;

    public CommonEPCPage(Context context, Activity act, LayoutInflater inflater, ReaderService ser) {
        this.mContext = context;
        this.mActivity = act;
        this.mInflater = inflater;
        this.mReaderService = ser;
        this.mAppContext = (AppContext) context.getApplicationContext();

        this.mViewEpc = this.mInflater.inflate(R.layout.adapter_common_pager1, null);
        this.mTextView = (TextView) this.mViewEpc.findViewById(R.id.adapter_commnon_pager1_tv);

        this.mProgressBar = (ProgressBar) this.mViewEpc.findViewById(R.id.adapter_common_pager1_progressBar);
        this.mProgressBar.setVisibility(View.GONE);

        this.mButton = (Button) this.mViewEpc.findViewById(R.id.adapter_common_pager1_epc);
        this.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainActivity) mContext).isConnected()) {
                    if (mCheckBoxFlag) {
                        if (mRunningFlag) {
                            mRunningFlag = false;
                            mCheckBox.setEnabled(true);
                            mProgressBar.setVisibility(View.GONE);
                            mButton.setText(mContext.getResources().getString(R.string.common_adapter_pager1_epc));
                            sendBroadcast(COMMON_ACTION_EPC_END, null);
                        }
                        else {
                            mRunningFlag = true;
                            mCheckBox.setEnabled(false);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mButton.setText("STOP");
                            sendBroadcast(COMMON_ACTION_EPC_REPEAT, null);
                        }

                    }
                    else {
                        sendBroadcast(COMMON_ACTION_EPC_ONE, null);
                    }
                }
                else {
                    Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.mCheckBox = (CheckBox) this.mViewEpc.findViewById(R.id.adapter_common_pager1_repeat);
        this.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) mCheckBoxFlag = true;
                else mCheckBoxFlag = false;
            }
        });
    }

    public View getView() {
        return this.mViewEpc;
    }

    public void setInit() {
        mRunningFlag = false;
        mCheckBox.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        mButton.setText(mContext.getResources().getString(R.string.common_adapter_pager1_epc));
        sendBroadcast(COMMON_ACTION_EPC_END, null);
    }

    public void setData(@NonNull String data) {
        mTextView.setText(data);
    }

    private void sendBroadcast(@NonNull String action, String data) {
        Intent i = new Intent(action);
        i.putExtra(STRING_DATA, data);
        mContext.sendBroadcast(i);
    }
}
