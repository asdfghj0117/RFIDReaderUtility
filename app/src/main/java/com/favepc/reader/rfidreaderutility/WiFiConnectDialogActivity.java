package com.favepc.reader.rfidreaderutility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.service.NetService;

/**
 * Created by Bruce_Chiang on 2017/12/8.
 */

public class WiFiConnectDialogActivity extends Activity {

    private Activity mActivity;
    private AppContext mAppContext;
    private CustomKeyboardManager mCustomKeyboardManager;

    private EditText mEtAddress, mEtPort;
    private Button mCancel, mConnect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect_dialog);

        this.mActivity = this;
        this.mAppContext = (AppContext) this.getApplicationContext();
        this.mCustomKeyboardManager = this.mAppContext.getKeyboard();

        this.mEtAddress = (EditText) findViewById(R.id.net_dialog_search_device_address);
        this.mEtPort = (EditText) findViewById(R.id.net_dialog_search_device_port);

        this.mCancel = (Button) findViewById(R.id.net_dialog_search_device_btncancel);
        this.mConnect = (Button) findViewById(R.id.net_dialog_search_device_btnconnect);

        Bundle _Bundle = getIntent().getExtras();
        this.mEtAddress.setText(_Bundle.getString("ip"));
        this.mEtPort.setText(_Bundle.getString("port"));

        this.mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        this.mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent _intent = new Intent(NetService.NET_ACTION_TCP_CONNECT);
                _intent.putExtra(NetService.DEVICE_IP, mEtAddress.getText());
                _intent.putExtra(NetService.DEVICE_PORT, mEtPort.getText());
                mActivity.sendBroadcast(_intent);
            }
        });
    }

}
