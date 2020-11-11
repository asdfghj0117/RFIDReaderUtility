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

public class CommonLockPage {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public static final String COMMON_ACTION_LOCK = "COMMON_ACTION_LOCK";
    public static final String PROCESS_ARGUMENT = "PROCESS_ARGUMENT";
    private Context         mContext;
    private Activity        mActivity;
    private AppContext      mAppContext;
    private LayoutInflater  mInflater;
    private View            mViewLock;

    private Spinner         mSpinnerSelect, mSpinnerKillPwd, mSpinnerAccessPwd, mSpinnerEpcBank, mSpinnerTidBank, mSpinnerUserBank;
    private EditTextAdapter mEtAccessPassword, mEtSelectAddress, mEtSelectLength, mEtSelectData,
                            mEtLockMask, mEtLockAction;
    private CheckBox        mCheckBoxSelect, mCheckBoxAccess, mCheckBoxProcess;
    private LinearLayout    ll1;
    private Drawable        mDrawableOK, mDrawableError;
    private Button          mButton;

    private boolean         mIsCheckSelectAdressArgs = false, mIsCheckSelectLengthArgs = false, mIsCheckSelectDataArgs = false;
    private boolean         mIsCheckAccessPasswordArgs = false;
    private int             Mask = 0, Action = 0;

    //private View            mUnderView;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private ArrayList<HashMap<String, String>> mProcessList;
    private ReaderService mReaderService;

    public CommonLockPage(Context context, Activity act, LayoutInflater inflater, ReaderService rs, CustomKeyboardManager ckm) {
        this.mContext = context;
        this.mActivity = act;
        this.mInflater = inflater;
        this.mReaderService = rs;
        this.mCustomKeyboardManager = ckm;
        this.mAppContext = (AppContext) context.getApplicationContext();

        this.mProcessList = new ArrayList<>();

        this.mViewLock = this.mInflater.inflate(R.layout.adapter_common_pager5, null);

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

        this.mCheckBoxSelect = (CheckBox) this.mViewLock.findViewById(R.id.adapter_common_pager5_checkbox_select);
        this.mCheckBoxAccess = (CheckBox) this.mViewLock.findViewById(R.id.adapter_common_pager5_checkbox_access);
        this.mCheckBoxProcess = (CheckBox) this.mViewLock.findViewById(R.id.adapter_common_pager5_checkbox_process);
        this.ll1 = (LinearLayout) this.mViewLock.findViewById(R.id.adapter_common_pager5_ll1);

        this.mCheckBoxProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mCheckBoxSelect.setVisibility(View.VISIBLE);
                    ll1.setVisibility(View.VISIBLE);
                    mEtSelectData.setVisibility(View.VISIBLE);
                    mCheckBoxAccess.setVisibility(View.VISIBLE);
                    mEtAccessPassword.setVisibility(View.VISIBLE);
                    mSpinnerKillPwd.setVisibility(View.VISIBLE);
                    mSpinnerAccessPwd.setVisibility(View.VISIBLE);
                    mSpinnerEpcBank.setVisibility(View.VISIBLE);
                    mSpinnerTidBank.setVisibility(View.VISIBLE);
                    mSpinnerUserBank.setVisibility(View.VISIBLE);
                    mEtLockMask.setVisibility(View.GONE);
                    mEtLockAction.setVisibility(View.GONE);
                }
                else {
                    mCheckBoxSelect.setVisibility(View.GONE);
                    ll1.setVisibility(View.GONE);
                    mEtSelectData.setVisibility(View.GONE);
                    mCheckBoxAccess.setVisibility(View.GONE);
                    mEtAccessPassword.setVisibility(View.GONE);
                    mSpinnerKillPwd.setVisibility(View.GONE);
                    mSpinnerAccessPwd.setVisibility(View.GONE);
                    mSpinnerEpcBank.setVisibility(View.GONE);
                    mSpinnerTidBank.setVisibility(View.GONE);
                    mSpinnerUserBank.setVisibility(View.GONE);
                    mEtLockMask.setVisibility(View.VISIBLE);
                    mEtLockAction.setVisibility(View.VISIBLE);

                    Mask = 0; Action = 0;
                    Mask |= lockPayloadMask(mSpinnerKillPwd.getSelectedItemPosition(), 8);
                    Mask |= lockPayloadMask(mSpinnerAccessPwd.getSelectedItemPosition(), 6);
                    Mask |= lockPayloadMask(mSpinnerEpcBank.getSelectedItemPosition(), 4);
                    Mask |= lockPayloadMask(mSpinnerTidBank.getSelectedItemPosition(), 2);
                    Mask |= lockPayloadMask(mSpinnerUserBank.getSelectedItemPosition(), 0);

                    mEtLockMask.setText(ReaderService.Format.makesUpZero(Integer.toString(Mask, 16), 3));
                    mEtLockAction.setText(ReaderService.Format.makesUpZero(Integer.toString(Action, 16), 3));
                }
            }
        });


        ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.common_memory_bank,
                R.layout.spinner_style);

        //select component
        this.mSpinnerSelect = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_select_memory);
        this.mEtSelectAddress = (EditTextAdapter)  this.mViewLock.findViewById(R.id.adapter_common_pager5_select_address);
        this.mEtSelectLength = (EditTextAdapter)  this.mViewLock.findViewById(R.id.adapter_common_pager5_select_length);
        this.mEtSelectData = (EditTextAdapter)  this.mViewLock.findViewById(R.id.adapter_common_pager5_select_data);
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
        this.mEtAccessPassword = (EditTextAdapter) this.mViewLock.findViewById(R.id.adapter_common_pager5_access_password);
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
                        mIsCheckAccessPasswordArgs = true;
                    } else {
                        mEtAccessPassword.setError(null, mDrawableError);
                        mIsCheckAccessPasswordArgs = false;
                    }
                }
                else {
                    mEtAccessPassword.setError(null, mDrawableError);
                    mIsCheckAccessPasswordArgs = false;
                }
                mAppContext.setAccessPassword(editable.toString());
            }
        });


        //lock component
        ArrayAdapter<CharSequence> lunchList1 = ArrayAdapter.createFromResource(this.mContext, R.array.common_lock_kill_pwd,
                R.layout.spinner_style);
        this.mSpinnerKillPwd = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_spinner_killpwd);
        this.mSpinnerKillPwd.setAdapter(lunchList1);
        lunchList1 = ArrayAdapter.createFromResource(this.mContext, R.array.common_lock_access_pwd,
                R.layout.spinner_style);
        this.mSpinnerAccessPwd = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_spinner_accesspwd);
        this.mSpinnerAccessPwd.setAdapter(lunchList1);

        ArrayAdapter<CharSequence> lunchList2 = ArrayAdapter.createFromResource(this.mContext, R.array.common_lock_epc_bank,
                R.layout.spinner_style);
        this.mSpinnerEpcBank = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_spinner_epc_bank);
        this.mSpinnerEpcBank.setAdapter(lunchList2);
        lunchList2 = ArrayAdapter.createFromResource(this.mContext, R.array.common_lock_tid_bank,
                R.layout.spinner_style);
        this.mSpinnerTidBank = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_spinner_tid_bank);
        this.mSpinnerTidBank.setAdapter(lunchList2);
        lunchList2 = ArrayAdapter.createFromResource(this.mContext, R.array.common_lock_user_bank,
                R.layout.spinner_style);
        this.mSpinnerUserBank = (Spinner)this.mViewLock.findViewById(R.id.adapter_common_pager5_spinner_user_bank);
        this.mSpinnerUserBank.setAdapter(lunchList2);

        this.mEtLockMask = (EditTextAdapter) this.mViewLock.findViewById(R.id.adapter_common_pager5_etmask);
        this.mEtLockAction = (EditTextAdapter) this.mViewLock.findViewById(R.id.adapter_common_pager5_etaction);



        this.mButton = (Button)this.mViewLock.findViewById(R.id.adapter_common_pager5_lock);
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
                        if (!mIsCheckAccessPasswordArgs) {
                            Toast.makeText(mContext, "ACCESS(P) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        _d = mReaderService.P(mEtAccessPassword.getText().toString());
                        _item = new HashMap<String, String>();
                        _item.put(PROCESS_COMMAND, "P");
                        _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                        mProcessList.add(_item);
                    }

                    Mask = 0; Action = 0;
                    Mask |= lockPayloadMask(mSpinnerKillPwd.getSelectedItemPosition(), 8);
                    Mask |= lockPayloadMask(mSpinnerAccessPwd.getSelectedItemPosition(), 6);
                    Mask |= lockPayloadMask(mSpinnerEpcBank.getSelectedItemPosition(), 4);
                    Mask |= lockPayloadMask(mSpinnerTidBank.getSelectedItemPosition(), 2);
                    Mask |= lockPayloadMask(mSpinnerUserBank.getSelectedItemPosition(), 0);

                    _d = mReaderService.L(ReaderService.Format.makesUpZero(Integer.toString(Mask, 16), 3),
                            ReaderService.Format.makesUpZero(Integer.toString(Action, 16), 3));
                    _item = new HashMap<String, String>();
                    _item.put(PROCESS_COMMAND, "L");
                    _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                    mProcessList.add(_item);

                    sendBroadcast(COMMON_ACTION_LOCK, mProcessList);

                    /*if (mCheckBoxAccess.isChecked()) {
                        if (!mIsCheckAccessPasswordArgs) {
                            mEtAccessPassword.requestFocus();
                            return;
                        }
                        sendBroadcast(COMMON_ACTION_PASSWORD_LOCK,
                                ReaderService.Format.makesUpZero(mEtAccessPassword.getText().toString(), 8),
                                ReaderService.Format.makesUpZero(Integer.toString(Mask, 16), 3),
                                ReaderService.Format.makesUpZero(Integer.toString(Action, 16), 3));
                    }
                    else {
                        sendBroadcast(COMMON_ACTION_LOCK,
                                ReaderService.Format.makesUpZero(Integer.toString(Mask, 16), 3),
                                ReaderService.Format.makesUpZero(Integer.toString(Action, 16), 3 ), null);

                    }*/
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
    }

    public View getView() {
        return this.mViewLock;
    }

    public void setSelectMemory(int i) { this.mSpinnerSelect.setSelection(i);}
    public void setSelectAddress(String s) { this.mEtSelectAddress.setText(s);}
    public void setSelectLength(String s) { this.mEtSelectLength.setText(s);}
    public void setSelectData(String s) { this.mEtSelectData.setText(s);}
    public void setAccessPassword(String s) {
        this.mEtAccessPassword.setText(s);
    }

    private int lockPayloadMask(int mask, int index) {
        if (mask == 0) return 0x0;
        else {
            Action |= (mask - 1) << index;
            if (((mask - 1) & 1) == 0) mask = 2;
            else mask = 3;
            return mask << index;
        }
    }

    private void sendBroadcast(@NonNull String action, ArrayList<HashMap<String, String>> al) {
        Intent i = new Intent(action);
        i.putExtra(PROCESS_ARGUMENT, al);
        mContext.sendBroadcast(i);
    }


}
