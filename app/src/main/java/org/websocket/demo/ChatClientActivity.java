package org.websocket.demo;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;

import org.java_websocket.WebSocketImpl;
import org.websocket.demo.proxy.BaseHandler;
import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.proxy.TcpMessage;
import org.websocket.demo.proxy.connection.IConnection;
import org.websocket.demo.proxy.connection.OkHttpWebSocketConnection;
import org.websocket.demo.util.Constant;
import org.websocket.demo.util.SPUtil;
import org.websocket.demo.util.ToastUtil;

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

    private PortParameters mPortParam = new PortParameters();

    private Button btnSetting, btnConnectPrint;

    private int mPrinterId = 0;

    private GpService mGpService = null;

    private PrinterServiceConnection conn = null;

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
        etAddress.setText(Constant.URL);
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
        onClosed();
        registerBroadcast();
        connection();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                client.connect(etAddress.getText().toString());
                break;
            case R.id.btnClose:
                client.disConnect();
                break;

            case R.id.btnBind:
                ServiceProxy.getInstance().startBindClient();
                break;
            case R.id.btnClear:
                etDetails.getText().clear();
                break;
            case R.id.btnHeartbeat:
                ServiceProxy.getInstance().sendHeartBeatRequest();
                break;

            case R.id.btn_connect:
                connectToDevice();
                break;
            case R.id.btn_setting:
                showSettingDialog();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        client.disConnect();
        try {
            this.unregisterReceiver(printerStatusBroadcastReceiver);
            if (conn != null) {
                unbindService(conn); // unBindService
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void connectedNotify(boolean status) {
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
                setPrintParameters(ip, port);
            }
        });
    }

    private void setPrintParameters(String ip, String port) {
        mPortParam.setPortType(PortParameters.ETHERNET);
        mPortParam.setIpAddr(ip);
        mPortParam.setPortNumber(Integer.valueOf(port));
        if (checkPortParameters(mPortParam)) {
            PortParamDataBase database = new PortParamDataBase(this);
            database.deleteDataBase("" + 0);
            database.insertPortParam(mPrinterId, mPortParam);
        } else {
//            messageBox(getString(R.string.port_parameters_wrong));

            ToastUtil.showLong(ChatClientActivity.this, "打印机参数错误！");
        }
    }

    private boolean checkPortParameters(PortParameters param) {
        if (null == param) {
            return false;
        }
        boolean rel = false;
        int type = param.getPortType();
        if (type == PortParameters.BLUETOOTH) {
            if (!param.getBluetoothAddr().equals("")) {
                rel = true;
            }
        } else if (type == PortParameters.ETHERNET) {
            if ((!param.getIpAddr().equals("")) && (param.getPortNumber() != 0)) {
                rel = true;
            }
        } else if (type == PortParameters.USB) {
            if (!param.getUsbDeviceName().equals("")) {
                rel = true;
            }
        }
        return rel;
    }


    /**
     * 连接打印机
     */
    private void connectToDevice() {

        ip = SPUtil.getInstance(this).getString(Constant.SPKey.KEY_PRINT_IP, Constant.DEFAULT_PRINT_IP);
        port = SPUtil.getInstance(this).getString(Constant.SPKey.KEY_PRINT_PORT, Constant.DEFAULT_PRINT_PORT);
        setPrintParameters(ip, port);

        Log.e(TAG, String.valueOf(mPortParam.getPortOpenState()));
        if (!checkPortParameters(mPortParam)) {
            ToastUtil.showLong(ChatClientActivity.this, "打印机参数错误！");
            return;
        }
        int rel = 0;
        switch (mPortParam.getPortType()) {
            case PortParameters.USB:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getUsbDeviceName(), 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case PortParameters.ETHERNET:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getIpAddr(), mPortParam.getPortNumber());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case PortParameters.BLUETOOTH:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getBluetoothAddr(), 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
        Log.e(TAG, "result :" + String.valueOf(r));
        if (r != GpCom.ERROR_CODE.SUCCESS) {
            if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
                mPortParam.setPortOpenState(true);
            } else {
                ToastUtil.showLong(ChatClientActivity.this, GpCom.getErrorText(r));
            }
        }
    }

    /**
     * 断开打印机
     */
    private void disconnectToDevice() {
        if (null != mPortParam && mPortParam.getPortOpenState()) {
            Log.d(TAG, "DisconnectToDevice ");
            try {
                mGpService.closePort(mPrinterId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void connection() {
        conn = new PrinterServiceConnection();
        Log.i(TAG, "connection");
        Intent intent = new Intent("com.gprinter.aidl.GpPrintService");
        intent.setPackage(getPackageName());
        bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.i(TAG, "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
        }
    }


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GpCom.ACTION_CONNECT_STATUS);
        this.registerReceiver(printerStatusBroadcastReceiver, filter);
    }


    private BroadcastReceiver printerStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!GpCom.ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                return;
            }
            int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
            if (mPrinterId != id) {
                return;
            }
            int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
            Log.d(TAG, "connect status " + type);
            switch (type) {
                case GpDevice.STATE_CONNECTING:
                    setProgressBarIndeterminateVisibility(true);
                    mPortParam.setPortOpenState(false);
                    break;
                case GpDevice.STATE_NONE:
                    mPortParam.setPortOpenState(false);
                    break;
                case GpDevice.STATE_VALID_PRINTER:
                    mPortParam.setPortOpenState(true);
                    break;
                case GpDevice.STATE_INVALID_PRINTER:
                    ToastUtil.showLong(ChatClientActivity.this, "Please use Gprinter!");
                    break;
                default:
                    ToastUtil.showLong(ChatClientActivity.this, "Please use Gprinter!");
                    break;
            }
        }
    };

}