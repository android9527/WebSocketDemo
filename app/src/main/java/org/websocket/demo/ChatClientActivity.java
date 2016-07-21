package org.websocket.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.java_websocket.WebSocketImpl;
import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.proxy.TcpMessage;
import org.websocket.demo.proxy.connection.IConnection;
import org.websocket.demo.proxy.connection.OkHttpWebSocketConnection;
import org.websocket.demo.request.BindRequest;
import org.websocket.demo.request.Constant;
import org.websocket.demo.request.HeartbeatRequest;
import org.websocket.demo.scheduletask.ScheduleTaskService;

/**
 * @datetime 2016-02-16 09:28 GMT+8
 */
public class ChatClientActivity extends AppCompatActivity implements OnClickListener, ImpsConnection {
    private Button btnConnect;
    private Button btnHeartbeat;
    private Button btnClose, btnBind;
    private EditText etAddress, etDetails;
    private final Gson gson = new Gson();
    private static final int MESSAGE_CONNECTED = 1;
    private static final int MESSAGE_CLOSED = MESSAGE_CONNECTED + 1;

    private static final int MESSAGE_RECEIVE = MESSAGE_CLOSED + 1;

    private static final int MESSAGE_SEND = MESSAGE_RECEIVE + 1;

    private IConnection client;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_CONNECTED:
                    onConnected();
                    break;
                case MESSAGE_CLOSED:
                    onClosed();
                    break;
                case MESSAGE_RECEIVE:
                    etDetails.append("获取到服务器信息 " + msg.obj + "\n");
                    break;
                case MESSAGE_SEND:
                    etDetails.append("发送数据到服务器 " + msg.obj + "\n");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);

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

        WebSocketImpl.DEBUG = true;
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");

        findViewById(R.id.btnBind).setOnClickListener(this);

//        client = new WebSocketConnection(this.getApplicationContext());
        client = OkHttpWebSocketConnection.instance(this);
        client.addImpsConnection(this);
        ScheduleTaskService.getInstance().init(this.getApplicationContext());
        onClosed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                client.connect(etAddress.getText().toString());

//                connect();
                break;
            case R.id.btnClose:
                client.disConnect();
                break;

            case R.id.btnBind:
                BindRequest request = new BindRequest();
                request.setSign(request.getSign());
                sendMessage(gson.toJson(request));
                break;
            case R.id.btnClear:
                etDetails.getText().clear();
                break;
            case R.id.btnHeartbeat:
                HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
                heartbeatRequest.setSign(heartbeatRequest.getSign());
                sendMessage(gson.toJson(heartbeatRequest));
                break;
        }
    }

    private void sendMessage(String message) {
        client.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        client.disConnect();
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

}