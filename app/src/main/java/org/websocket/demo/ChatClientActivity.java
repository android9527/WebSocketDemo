package org.websocket.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.google.gson.Gson;

import org.java_websocket.WebSocketImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.request.BindRequest;
import org.websocket.demo.request.HeartbeatRequest;
import org.websocket.demo.request.PushResponse;
import org.websocket.demo.scheduletask.ScheduleTaskService;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

/**
 * @datetime 2016-02-16 09:28 GMT+8
 */
public class ChatClientActivity extends AppCompatActivity implements OnClickListener {
    private Button btnConnect, btnHeartbeat, btnClear;
    private ScrollView svChat;
    private Button btnClose, btnBind;
    private EditText etDetails;

    private okhttp3.ws.WebSocket socket;

    //    final String HOST = "ws://push.qfpay.com";
    final String HOST = "ws://172.100.101.106:9997";
    OkHttpClient okHttpClient;
    final Gson gson = new Gson();
    private static final int MESSAGE_CONNECTED = 1;
    private static final int MESSAGE_CLOSED = MESSAGE_CONNECTED + 1;

    private static final int MESSAGE_RECEIVE = MESSAGE_CLOSED + 1;

    private static final int MESSAGE_SEND = MESSAGE_RECEIVE + 1;

    private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    //信任全部
    private static X509TrustManager xtm = new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    };
    private static X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};

    private OkHttpClient init() {
        try {

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[0], xtmArray, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .writeTimeout(0, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(socketFactory)
                    .hostnameVerifier(HOSTNAME_VERIFIER);

//            client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
//            client.setReadTimeout(0, TimeUnit.MILLISECONDS);
//            client.setWriteTimeout(0, TimeUnit.MILLISECONDS);


            HttpLoggingInterceptor.Level level = /*ConstValue.DEBUG_MODE ? HttpLoggingInterceptor.Level.HEADERS : */HttpLoggingInterceptor.Level.BODY;

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new MyLogger()).setLevel(level);

            builder.addInterceptor(loggingInterceptor);    // log

            okHttpClient = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }


    private void connect() {
        Request request = new Request.Builder().url(HOST).addHeader("Origin", HOST).
                build();
        WebSocketCall call = WebSocketCall.create(okHttpClient, request);
        call.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(okhttp3.ws.WebSocket webSocket, Response response) {
                System.out.println("onOpen");
                socket = webSocket;
                handler.sendEmptyMessage(MESSAGE_CONNECTED);
            }

            @Override
            public void onFailure(IOException e, Response response) {
                e.printStackTrace();
                System.out.println("onFailure");
                socket = null;
                if (response != null) {
                    System.out.println("onFailure" + "   " + response.message());
                }
                handler.sendEmptyMessage(MESSAGE_CLOSED);
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                System.out.println("onMessage");
                if (message != null) {
                    String response = message.string();
                    System.out.println("onMessage" + response);
                    message.close();
                    parseMessage(response);
                }

            }

            @Override
            public void onPong(Buffer payload) {

            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("onClose" + reason);
                socket = null;
                handler.sendEmptyMessage(MESSAGE_CLOSED);
            }
        });
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
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

        ScheduleTaskService.getInstance().init(this.getApplicationContext());
        ServiceProxy.getInstance(this.getApplicationContext()).startHeartBeat();
        init();
        svChat = (ScrollView) findViewById(R.id.svChat);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnBind = (Button) findViewById(R.id.btnBind);
        btnClear = (Button) findViewById(R.id.btnClear);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                connect();
                break;
            case R.id.btnClose:
                close();
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

    private void parseMessage(String message) {
        try {
            JSONObject object = new JSONObject(message);
            if (!object.has("pkg_type")) {
                return;
            }
            String pkg_type = object.getString("pkg_type");

            if (pkg_type.equals("06") && object.has("msg_id")) {
                PushResponse response = new PushResponse();
                response.setMsgid(object.getString("msg_id"));
                response.setSign(response.getSign());
                sendMessage(gson.toJson(response));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Message msg = handler.obtainMessage(MESSAGE_RECEIVE);
        msg.obj = message;
        handler.sendMessage(msg);
    }

    private void sendMessage(final String message) {

        Log.e("ChatClientActivity", "send To Server" + message);

        if (socket == null || message == null) {
            return;
        }
        AsyncTaskExecutors.executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestBody requestBody = RequestBody.create(okhttp3.ws.WebSocket.TEXT, message);
                    socket.sendMessage(requestBody);
                    Message msg = handler.obtainMessage(MESSAGE_SEND);
                    msg.obj = message;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void close() {
        if (socket == null) {
            return;
        }
        try {
            socket.close(1000, "closed by client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();

        ServiceProxy.getInstance(this.getApplicationContext()).stopHeartBeat();
    }

    private void onConnected() {
        btnConnect.setEnabled(false);
        btnClose.setEnabled(true);
        btnBind.setEnabled(true);
        btnHeartbeat.setEnabled(true);
    }

    private void onClosed(){
        btnConnect.setEnabled(true);
        btnClose.setEnabled(false);
        btnBind.setEnabled(false);
        btnHeartbeat.setEnabled(false);
    }


    private class MyLogger implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(String message) {
            System.err.println("OKHTTP3 ---->" + message);
        }
    }
}