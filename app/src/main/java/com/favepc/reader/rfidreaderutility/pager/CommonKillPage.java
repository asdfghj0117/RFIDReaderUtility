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

public class CommonKillPage {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public static final String COMMON_ACTION_KILL = "COMMON_ACTION_KILL";
    public static final String PROCESS_ARGUMENT = "PROCESS_ARGUMENT";
    private Context         mContext;
    private Activity        mActivity;
    private AppContext      mAppContext;
    private LayoutInflater  mInflater;
    private View            mViewKill;

    private Spinner         mSpinnerSelect;
    private EditTextAdapter mEtSelectAddress, mEtSelectLength, mEtSelectData, mEtAccessPassword, mEtKillPassword;
    private CheckBox        mCheckBoxSelect, mCheckBoxAccess, mCheckBoxProcess;
    private LinearLayout    ll1;
    private Drawable        mDrawableOK, mDrawableError;
    private Button          mButton;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private ArrayList<HashMap<String, String>> mProcessList;
    private ReaderService mReaderService;

    private boolean         mIsCheckSelectAdressArgs = false, mIsCheckSelectLengthArgs = false, mIsCheckSelectDataArgs = false;
    private boolean         mIsCheckAccessPasswordArgs = false, mIsCheckKillPasswordArgs = false;

    public CommonKillPage(Context context, Activity act, LayoutInflater inflater, ReaderService rs, CustomKeyboardManager ckm) {
        this.mContext = context;
        this.mActivity = act;
        this.mInflater = inflater;
        this.mReaderService = rs;
        this.mCustomKeyboardManager = ckm;
        this.mAppContext = (AppContext) context.getApplicationContext();

        this.mProcessList = new ArrayList<>();

        this.mViewKill = this.mInflater.inflate(R.layout.adapter_common_pager6, null);

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

        this.mCheckBoxSelect = (CheckBox) this.mViewKill.findViewById(R.id.adapter_common_pager6_checkbox_select);
        this.mCheckBoxAccess = (CheckBox) this.mViewKill.findViewById(R.id.adapter_common_pager6_checkbox_access);
        this.mCheckBoxProcess = (CheckBox) this.mViewKill.findViewById(R.id.adapter_common_pager6_checkbox_process);
        this.ll1 = (LinearLayout) this.mViewKill.findViewById(R.id.adapter_common_pager6_ll1);

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
        this.mSpinnerSelect = (Spinner)this.mViewKill.findViewById(R.id.adapter_common_pager6_select_memory);
        this.mEtSelectAddress = (EditTextAdapter)  this.mViewKill.findViewById(R.id.adapter_common_pager6_select_address);
        this.mEtSelectLength = (EditTextAdapter)  this.mViewKill.findViewById(R.id.adapter_common_pager6_select_length);
        this.mEtSelectData = (EditTextAdapter)  this.mViewKill.findViewById(R.id.adapter_common_pager6_select_data);
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
        this.mEtAccessPassword = (EditTextAdapter) this.mViewKill.findViewById(R.id.adapter_common_pager6_access_password);
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

        //kill component
        this.mEtKillPassword = (EditTextAdapter)this.mViewKill.findViewById(R.id.adapter_common_pager6_kill_password);
        this.mEtKillPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mEtKillPassword.setError(null, mDrawableOK);
                    mIsCheckKillPasswordArgs = true;
                }
                else {
                    mEtKillPassword.setError(null, mDrawableError);
                    mIsCheckKillPasswordArgs = false;
                }
            }
        });


        this.mButton = (Button)this.mViewKill.findViewById(R.id.adapter_common_pager6_kill);
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

                    if (!mIsCheckKillPasswordArgs) {
                        Toast.makeText(mContext, "KILL(K) parameter format is not correct.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    _d = mReaderService.K(ReaderService.Format.makesUpZero(mEtKillPassword.getText().toString(), 8), "0");
                    _item = new HashMap<String, String>();
                    _item.put(PROCESS_COMMAND, "K");
                    _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                    mProcessList.add(_item);

                    sendBroadcast(COMMON_ACTION_KILL, mProcessList);


                } else {
                    Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.mCustomKeyboardManager.attachTo(this.mEtSelectAddress, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtSelectLength, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtSelectData, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtAccessPassword, HexKeyboard);
        this.mCustomKeyboardManager.attachTo(this.mEtKillPassword, HexKeyboard);
    }

    public View getView() {
                return this.mViewKill;
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
