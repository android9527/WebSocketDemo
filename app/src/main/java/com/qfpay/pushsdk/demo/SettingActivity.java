package com.qfpay.pushsdk.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.qfpay.pushsdk.util.Constant;
import com.qfpay.pushsdk.util.SPUtil;

/**
 * Created by chenfeiyue on 16/7/25.
 * SettingActivity
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUserId, etPushUrl, etAppType, etSecretKey, etDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SettingActivity", "onCreate");
        setContentView(R.layout.activity_setting);
        etUserId = (EditText) findViewById(R.id.et_user_id);
        etPushUrl = (EditText) findViewById(R.id.et_push_url);
        etAppType = (EditText) findViewById(R.id.et_app_type);
        etSecretKey = (EditText) findViewById(R.id.et_secret_key);
        etDeviceId = (EditText) findViewById(R.id.et_device_id);
        findViewById(R.id.btn_ok).setOnClickListener(this);

        String userId = SPUtil.getInstance(SettingActivity.this).getString(Constant.SPKey.KEY_USER_ID, Constant.userid);
        String pushUrl = SPUtil.getInstance(SettingActivity.this).getString(Constant.SPKey.KEY_PUSH_URL, Constant.URL);
        String appType = SPUtil.getInstance(SettingActivity.this).getString(Constant.SPKey.KEY_APP_TYPE, Constant.app_type);
        String secretKey = SPUtil.getInstance(SettingActivity.this).getString(Constant.SPKey.KEY_SECRET_KEY, Constant.secret_key);
        String deviceId = SPUtil.getInstance(SettingActivity.this).getString(Constant.SPKey.KEY_PRINT_DEVICE_ID, Constant.print_device_id);
        etUserId.setText(userId);
        etPushUrl.setText(pushUrl);
        etAppType.setText(appType);
        etSecretKey.setText(secretKey);
        etDeviceId.setText(deviceId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                SPUtil.getInstance(SettingActivity.this).save(Constant.SPKey.KEY_USER_ID, etUserId.getText().toString().trim());
                SPUtil.getInstance(SettingActivity.this).save(Constant.SPKey.KEY_PUSH_URL, etPushUrl.getText().toString().trim());
                SPUtil.getInstance(SettingActivity.this).save(Constant.SPKey.KEY_APP_TYPE, etAppType.getText().toString().trim());
                SPUtil.getInstance(SettingActivity.this).save(Constant.SPKey.KEY_SECRET_KEY, etSecretKey.getText().toString().trim());
                SPUtil.getInstance(SettingActivity.this).save(Constant.SPKey.KEY_PRINT_DEVICE_ID, etDeviceId.getText().toString().trim());
                break;
        }
    }
}
