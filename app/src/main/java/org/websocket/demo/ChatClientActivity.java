package org.websocket.demo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.java_websocket.WebSocketImpl;
import org.websocket.demo.proxy.BaseHandler;
import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.proxy.TcpMessage;
import org.websocket.demo.proxy.connection.IConnection;
import org.websocket.demo.proxy.connection.OkHttpWebSocketConnection;
import org.websocket.demo.util.Constant;
import org.websocket.demo.util.SPUtil;

/**
 * @datetime 2016-02-16 09:28 GMT+8
 */
public class ChatClientActivity extends AppCompatActivity implements OnClickListener, ImpsConnection {
    private String TAG = "ChatClientActivity";
    private Button btnConnect;
    private Button btnHeartbeat;
    private Button btnClose, btnBind;
    private EditText etAddress, etDetails;
    private static final int MESSAGE_CONNECTED = 1;
    private static final int MESSAGE_CLOSED = MESSAGE_CONNECTED + 1;

    private static final int MESSAGE_RECEIVE = MESSAGE_CLOSED + 1;

    private static final int MESSAGE_SEND = MESSAGE_RECEIVE + 1;

    private IConnection client;
    private MyHandler handler;

    private Button btnSetting, btnConnectPrint;

    private static final class MyHandler extends BaseHandler<ChatClientActivity> {
        public MyHandler(ChatClientActivity activity) {
            super(activity);
        }

        @Override
        public void onHandleMessage(Message msg, ChatClientActivity activity) {
            switch (msg.what) {
                case MESSAGE_CONNECTED:
                    activity.onConnected();
                    break;
                case MESSAGE_CLOSED:
                    activity.onClosed();
                    break;
                case MESSAGE_RECEIVE:
                    activity.etDetails.append("获取到服务器信息 " + msg.obj + "\n");
                    break;
                case MESSAGE_SEND:
                    activity.etDetails.append("发送数据到服务器 " + msg.obj + "\n");
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);
        handler = new MyHandler(this);
        ServiceProxy.getInstance().init(this);
//        DeviceUtil.setWifiNeverSleep(this.getApplicationContext());
        startService(new Intent(ChatClientActivity.this, WebSocketService.class));

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnBind = (Button) findViewById(R.id.btnBind);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etAddress.setText(SPUtil.getInstance(ChatClientActivity.this).getString(Constant.SPKey.KEY_PUSH_URL, Constant.URL));
        Button btnClear = (Button) findViewById(R.id.btnClear);
        etDetails = (EditText) findViewById(R.id.etDetails);
        btnHeartbeat = (Button) findViewById(R.id.btnHeartbeat);
        btnConnect.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnHeartbeat.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnConnectPrint = (Button) findViewById(R.id.btn_connect);
        btnSetting.setOnClickListener(this);
        btnConnectPrint.setOnClickListener(this);


        WebSocketImpl.DEBUG = true;
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");

        findViewById(R.id.btnBind).setOnClickListener(this);

//        client = new WebSocketConnection(this.getApplicationContext());
        client = OkHttpWebSocketConnection.instance(this);
        client.addImpsConnection(this);
//        onClosed();



        findViewById(R.id.btnSetting).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                client.connect(etAddress.getText().toString());
                break;
            case R.id.btnClose:
                client.disConnect(false);
                break;

            case R.id.btnBind:
                ServiceProxy.getInstance().startBindClient();
//                ServiceProxy.getInstance().startPrintThread();
                break;
            case R.id.btnClear:
                etDetails.getText().clear();
                break;
            case R.id.btnHeartbeat:
                ServiceProxy.getInstance().sendHeartBeatRequest();
//                ServiceProxy.getInstance().stopPrintThread();
                break;

            case R.id.btn_connect:
                ServiceProxy.getInstance().connectPrint();
                break;
            case R.id.btn_setting:
                showSettingDialog();
                break;
            case R.id.btnSetting:
                startActivity(new Intent(ChatClientActivity.this, SettingActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        client.disConnect();
        client.removeImpsConnection(this);
    }

    private void onConnected() {
        btnConnect.setEnabled(false);
        btnClose.setEnabled(true);
        btnBind.setEnabled(true);
        btnHeartbeat.setEnabled(true);
    }

    private void onClosed() {
        btnConnect.setEnabled(true);
        btnClose.setEnabled(false);
        btnBind.setEnabled(false);
        btnHeartbeat.setEnabled(false);
    }

    @Override
    public void receiveMsg(TcpMessage msg) {

    }

    @Override
    public void receiveMsg(String msg) {

        Message message = handler.obtainMessage(MESSAGE_RECEIVE);
        message.obj = msg;
        handler.sendMessage(message);
    }

    @Override
    public void connectedNotify(boolean status, boolean needReConnect) {
        if (status)
            handler.sendEmptyMessage(MESSAGE_CONNECTED);
        else
            handler.sendEmptyMessage(MESSAGE_CLOSED);
    }

    @Override
    public void sendedMessage(String msg) {
        Message message = handler.obtainMessage(MESSAGE_SEND);
        message.obj = msg;
        handler.sendMessage(message);
    }


    Dialog settingDialog;
    String ip, port;

    /**
     * 显示输入金额对话框
     */
    private void showSettingDialog() {
        View view = getLayoutInflater().inflate(R.layout.print_setting_dialog, null);
        final EditText portEditText = (EditText) view.findViewById(R.id.portText);
        final EditText ipEditText = (EditText) view.findViewById(R.id.ipEditText);
        ip = SPUtil.getInstance(this).getString(Constant.SPKey.KEY_PRINT_IP, Constant.DEFAULT_PRINT_IP);
        ipEditText.setText(ip);
        ipEditText.setSelection(ip.length());
        port = SPUtil.getInstance(this).getString(Constant.SPKey.KEY_PRINT_PORT, Constant.DEFAULT_PRINT_PORT);
        portEditText.setText(port);
        portEditText.setSelection(port.length());
        if (settingDialog == null) {
            settingDialog = new Dialog(ChatClientActivity.this);
            settingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        settingDialog.show();
        settingDialog.setContentView(view);
        view.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                settingDialog.dismiss();
                ip = ipEditText.getText().toString().trim();
                port = portEditText.getText().toString().trim();
                SPUtil.getInstance(ChatClientActivity.this).save(Constant.SPKey.KEY_PRINT_IP, ip);
                SPUtil.getInstance(ChatClientActivity.this).save(Constant.SPKey.KEY_PRINT_PORT, port);
            }
        });
    }
}