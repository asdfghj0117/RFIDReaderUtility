package com.favepc.reader.rfidreaderutility;

import android.app.Application;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;

/**
 * Created by Bruce_Chiang on 2017/3/28.
 */

public class AppContext extends Application {

    private static AppContext instance;

    private boolean mActionIsAck = false;
    private String _selectAddress, _selectLength, _selectData, _accessPassword;
    private int _selectMemory, _selectBleHandheldMode, _btTimeBleHandheldMode;
    private CustomKeyboardManager _customKeyboardManager;

    public static AppContext getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setKeyboard(CustomKeyboardManager ckm) { this._customKeyboardManager = ckm; }
    public CustomKeyboardManager getKeyboard() { return this._customKeyboardManager; }

    public void setAck(boolean b) {
        this.mActionIsAck = b;
    }
    public boolean getAck() {
        return this.mActionIsAck;
    }

    public void setSelectMemory(int s) { _selectMemory = s; }
    public int getSelectMemory() { return  _selectMemory; }

    public void setSelectAddress(String s) { _selectAddress = s; }
    public String getSelectAddress() { return  _selectAddress; }

    public void setSelectLength(String s) { _selectLength = s; }
    public String getSelectLength() { return  _selectLength; }

    public void setSelectData(String s) { _selectData = s; }
    public String getSelectData() { return  _selectData; }

    public void setAccessPassword(String s) { this._accessPassword = s; }
    public String getAccessPassword() {
        return this._accessPassword;
    }

    public void setBleHandheldSelectMode(int i) { _selectBleHandheldMode = i; }
    public int getBleHandheldSelectMode() { return  _selectBleHandheldMode; }

    public void setBleHandheldBtTimeMode(int i) { _btTimeBleHandheldMode = i; }
    public int getBleHandheldBtTimeMode() { return  _btTimeBleHandheldMode; }
}
