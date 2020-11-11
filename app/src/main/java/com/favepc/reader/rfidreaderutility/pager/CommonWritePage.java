package com.favepc.reader.rfidreaderutility.pager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.EditTextAdapter;
import com.favepc.reader.rfidreaderutility.object.CustomBaseKeyboard;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bruce_Chiang on 2017/4/6.
 */

public class CommonWritePage {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public static final String COMMON_ACTION_WRITE = "COMMON_ACTION_WRITE";
    public static final String PROCESS_ARGUMENT = "PROCESS_ARGUMENT";

    private Context         mContext;
    private Activity        mActivity;
    private AppContext      mAppContext;
    private LayoutInflater  mInflater;
    private View            mViewWrite;

    private Spinner         mSpinnerSelect, mSpinnerWrite;
    private EditTextAdapter mEtSelectAddress, mEtSelectLength, mEtSelectData,
            mEtAccessPassword, mEtWriteAddress, mEtWriteLength, mEtWriteData;
    private CheckBox        mCheckBoxSelect, mCheckBoxAccess, mCheckBoxProcess;
    private LinearLayout    ll1;
    private Drawable        mDrawableOK, mDrawableError;
    private Button          mButton;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private ArrayList<HashMap<String, String>> mProcessList;
    private ReaderService mReaderService;


    private boolean         mIsCheckSelectAdressArgs = false, mIsCheckSelectLengthArgs = false, mIsCheckSelectDataArgs = false;
    private boolean         mIsCheckAccessPassword = false;
    private boolean         mIsCheckWriteAddressArgs = false, mIsCheckWriteLengthArgs = false, mIsCheckWriteDataArgs = false;

    public CommonWritePage(Context context, Activity act, LayoutInflater inflater, ReaderService rs, CustomKeyboardManager ckm) {
        this.mContext = context;
        this.mActivity = act;
        this.mInflater = inflater;
        this.mReaderService = rs;
        this.mCustomKeyboardManager = ckm;
        this.mAppContext = (AppContext) context.getApplicationContext();

        this.mProcessList = new ArrayList<>();

        this.mViewWrite = this.mInflater.inflate(R.layout.adapter_common_pager4, null);

        this.mDrawableOK 	= ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_check_black_48dp);
        this.mDrawableError = ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_close_black_48dp);
        this.mDrawableOK.setBounds(0, 0, 48, 48);
        this.mDrawableError.setBounds(0, 0, 48, 48);


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

        this.mCheckBoxSelect = (CheckBox) this.mViewWrite.findViewById(R.id.adapter_common_pager4_checkbox_select);
        this.mCheckBoxAccess = (CheckBox) this.mViewWrite.findViewById(R.id.adapter_common_pager4_checkbox_access);
        this.mCheckBoxProcess = (CheckBox) this.mViewWrite.findViewById(R.id.adapter_common_pager4_checkbox_process);
        this.ll1 = (LinearLayout) this.mViewWrite.findViewById(R.id.adapter_common_pager4_ll1);

        this.mCheckBoxProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mCheckBoxSelect.setVisibility(View.VISIBLE);
                    ll1.setVisibility(View.VISIBLE);
                    mEtSelectData.setVisibility(View.VISIBLE);
                    mCheckBoxAccess.setVisibility(View.VISIBLE);
                    mEtAccessPassword.setVisibility(View.VISIBLE);
                }
                else {
                    mCheckBoxSelect.setVisibility(View.GONE);
                    ll1.setVisibility(View.GONE);
                    mEtSelectData.setVisibility(View.GONE);
                    mCheckBoxAccess.setVisibility(View.GONE);
                    mEtAccessPassword.setVisibility(View.GONE);
                }
            }
        });

        ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.common_memory_bank,
                R.layout.spinner_style);

        //select component
        this.mSpinnerSelect = (Spinner) this.mViewWrite.findViewById(R.id.adapter_common_pager4_select_memory);
        this.mEtSelectAddress = (EditTextAdapter)  this.mViewWrite.findViewById(R.id.adapter_common_pager4_select_address);
        this.mEtSelectLength = (EditTextAdapter)  this.mViewWrite.findViewById(R.id.adapter_common_pager4_select_length);
        this.mEtSelectData = (EditTextAdapter)  this.mViewWrite.findViewById(R.id.adapter_common_pager4_select_data);
        this.mSpinnerSelect.setAdapter(lunchList);
        this.mSpinnerSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mAppContext.setSelectMemory(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        this.mEtSelectAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    long val = Long.parseLong(editable.toString(), 16);
                    if (val >= 0 && val <= 0x3FFF) {
                        mEtSelectAddress.setError(null, mDrawableOK);
                        mIsCheckSelectAdressArgs = true;

                    } else {
                        mEtSelectAddress.setError(null, mDrawableError);
                        mIsCheckSelectAdressArgs = false;
                    }
                }
                else {
                    mEtSelectAddress.setError(null, mDrawableError);
                    mIsCheckSelectAdressArgs = false;
                }
                mAppContext.setSelectAddress(editable.toString());
            }
        });
        this.mEtSelectLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    long val = Long.parseLong(editable.toString(), 16);
                    if (val >= 1 && val <= 0x60) {
                        mEtSelectLength.setError(null, mDrawableOK);
                        mIsCheckSelectLengthArgs = true;

                    } else {
                        mEtSelectLength.setError(null, mDrawableError);
                        mIsCheckSelectLengthArgs = false;
                    }
                }
                else {
                    mEtSelectLength.setError(null, mDrawableError);
                    mIsCheckSelectLengthArgs = false;
                }
                mAppContext.setSelectLength(editable.toString());
            }
        });
        this.mEtSelectData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                int nLength = editable.length();
                int nBitsLength = mEtSelectLength.length() > 0 ? Integer.parseInt(mEtSelectLength.getText().toString(),16) : 0;
                int nMax = nLength * 4;
                int nMin = nLength * 4 - 3;
                if ((nBitsLength < nMin) || (nBitsLength > nMax))
                {
                    mEtSelectData.setError(null, mDrawableError);
                    mIsCheckSelectDataArgs = false;
                }
                else {
                    mEtSelectData.setError(null, mDrawableOK);
                    mIsCheckSelectDataArgs = true;
                }
                mAppContext.setSelectData(editable.toString());
            }
        });

        //access component
        this.mEtAccessPassword = (EditTextAdapter)this.mViewWrite.findViewById(R.id.adapter_common_pager4_access_password);
        this.mEtAccessPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    long val = Long.parseLong(editable.toString(), 16);
                    if (val >= 0 && val <= 0xFFFFFFFFL) {
                        mEtAccessPassword.setError(null, mDrawableOK);
                        mIsCheckAccessPassword = true;
                    } else {
                        mEtAccessPassword.setError(null, mDrawableError);
                        mIsCheckAccessPassword = false;
                    }
                }
                else {
                    mEtAccessPassword.setError(null, mDrawableError);
                    mIsCheckAccessPassword = false;
                }
                mAppContext.setAccessPassword(editable.toString());
            }
        });

        //write component
        this.mSpinnerWrite = (Spinner) this.mViewWrite.findViewById(R.id.adapter_common_pager4_write_memory);
        this.mEtWriteAddress = (EditTextAdapter)this.mViewWrite.findViewById(R.id.adapter_common_pager4_address);
        this.mEtWriteLength = (EditTextAdapter)this.mViewWrite.findViewById(R.id.adapter_common_pager4_length);
        this.mEtWriteData = (EditTextAdapter)this.mViewWrite.findViewById(R.id.adapter_common_pager4_data);
        this.mSpinnerWrite.setAdapter(lunchList);
        this.mSpinnerWrite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        mEtWriteAddress.setText("0000");
                        mEtWriteLength.setText("04");
                        break;
                    case 1://EPC
                        mEtWriteAddress.setText("0002");
                        mEtWriteLength.setText("06");
                        break;
                    case 2://TID
                        mEtWriteAddress.setText("0000");
                        mEtWriteLength.setText("04");
                        break;
                    case 3://USER
                        mEtWriteAddress.setText("0000");
                        mEtWriteLength.setText("01");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        this.mEtWriteAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (Integer.parseInt(s.toString(), 16) <= 0x3FFF) {
                        mEtWriteAddress.setError(null, mDrawableOK);
                        mIsCheckWriteAddressArgs = true;
                    }
                    else {
                        mEtWriteAddress.setError(null, mDrawableError);
                        mIsCheckWriteAddressArgs = false;
                    }
                }
                else {
                    mEtWriteAddress.setError(null, mDrawableError);
                    mIsCheckWriteAddressArgs = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        this.mEtWriteLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    if ((Integer.parseInt(editable.toString(), 16) > 0 ) && (Integer.parseInt(editable.toString(), 16) <= 0x20)) {
                        mEtWriteLength.setError(null, mDrawableOK);
                        mIsCheckWriteLengthArgs = true;
                    }
                    else {
                        mEtWriteLength.setError(null, mDrawableError);
                        mIsCheckWriteLengthArgs = false;
                    }
                }
                else {
                    mEtWriteLength.setError(null, mDrawableError);
                    mIsCheckWriteLengthArgs = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });
        this.mEtWriteData.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    int nDataLength = editable.length();
                    int nWordsLength = Integer.parseInt(mEtWriteLength.getText().toString(),16);
                    if (nWordsLength * 4 != nDataLength)
                    {
                        mEtWriteData.setError(null, mDrawableError);
                        mIsCheckWriteDataArgs = false;
                    }
                    else {
                        mEtWriteData.setError(null, mDrawableOK);
                        mIsCheckWriteDataArgs = true;
                    }
                }
                else {
                    mEtWriteData.setError(null, mDrawableError);
                    mIsCheckWriteDataArgs = false;
                }
                //
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });

        this.mButton = (Button)this.mViewWrite.findViewById(R.id.adapter_common_pager4_write);
        this.mButton.setOnClickListener(new View.OnClickListener() {
            HashMap<String, String> _item;
            byte[] _d;

            @Override
            public void onClick(View view) {
                if (((MainActivity) mContext).isConnected()) {
                    mProcessList.clear();

                    if (mCheckBoxSelect.isChecked()) {
                        if (mIsCheckSelectAdressArgs && mIsCheckSelectLengthArgs && mIsCheckSelectDataArgs) {
                            _d = mReaderService.T(String.valueOf(mSpinnerSelect.getSelectedItemPosition()),
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
                    if (mCheckBoxAccess.isChecked()) {
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
                    if (mIsCheckWriteAddressArgs && mIsCheckWriteLengthArgs && mIsCheckWriteDataArgs) {
                        _d = mReaderService.W(String.valueOf(mSpinnerWrite.getSelectedItemPosition()),
                                mEtWriteAddress.getText().toString(),
                                mEtWriteLength.getText().toString(),
                                mEtWriteData.getText().toString());
                        _item = new HashMap<String, String>();
                        _item.put(PROCESS_COMMAND, "W");
                        _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                        mProcessList.add(_item);
                    }
                    else {
                        Toast.makeText(mContext, "WRITE(W) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendBroadcast(COMMON_ACTION_WRITE, mProcessList);
                }
                else {
                    Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.mCustomKeyboardManager.attachTo(this.mEtSelectAddress, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtSelectLength, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtSelectData, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtAccessPassword, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtWriteAddress, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtWriteLength, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtWriteData, HexKeyboard);

    }

    public View getView() {
        return this.mViewWrite;
    }

    public void setSelectMemory(int i) { this.mSpinnerSelect.setSelection(i);}
    public void setSelectAddress(String s) { this.mEtSelectAddress.setText(s);}
    public void setSelectLength(String s) { this.mEtSelectLength.setText(s);}
    public void setSelectData(String s) { this.mEtSelectData.setText(s);}
    public void setAccessPassword(String s) { this.mEtAccessPassword.setText(s); }

    private void sendBroadcast(@NonNull String action, ArrayList<HashMap<String, String>> al) {
        Intent i = new Intent(action);
        i.putExtra(PROCESS_ARGUMENT, al);
        mContext.sendBroadcast(i);
    }

}
