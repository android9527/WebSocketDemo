package org.websocket.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.websocket.demo.request.BindRequest;

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
public class ChatClientActivity1 extends AppCompatActivity implements OnClickListener {

    private ScrollView svChat;
    private Spinner spDraft;
    private EditText etAddress;
    private Spinner spAddress;
    private Button btnConnect;
    private Button btnClose;
    private EditText etDetails;

    private EditText etName;
    private EditText etMessage;
    private Button btnSend;

    private WebSocketClient client;// 连接客户端
    private DraftInfo selectDraft;// 连接协议



    private okhttp3.ws.WebSocket socket;


    //    private static final String HOST = "ws://push.qfpay.com";
//    final String HOST = "ws://push.qfpay.com";
        static final String HOST = "ws://172.100.101.106:9997";
    OkHttpClient okHttpClient;


    Gson gson = new Gson();

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
                    .connectTimeout(0, TimeUnit.MILLISECONDS)
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


    private void enqueue() {
        Request request = new Request.Builder().url(HOST).addHeader("Origin", HOST).
                build();
        WebSocketCall call = WebSocketCall.create(okHttpClient, request);
        call.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(okhttp3.ws.WebSocket webSocket, Response response) {
                System.out.println("onOpen");
                socket = webSocket;
            }

            @Override
            public void onFailure(IOException e, Response response) {
                e.printStackTrace();
                System.out.println("onFailure");
                if (response != null) {
                    System.out.println("onFailure" + "   " + response.message());
                }
                socket = null;
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                System.out.println("onMessage");
                if (message != null) {
                    System.out.println("onMessage" + message.string());
                    message.close();
                }
            }

            @Override
            public void onPong(Buffer payload) {

            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("onClose" + reason);
                socket = null;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);
        init();
        svChat = (ScrollView) findViewById(R.id.svChat);
        spDraft = (Spinner) findViewById(R.id.spDraft);
        etAddress = (EditText) findViewById(R.id.etAddress);
        spAddress = (Spinner) findViewById(R.id.spAddress);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnClose = (Button) findViewById(R.id.btnClose);
        etDetails = (EditText) findViewById(R.id.etDetails);

        etName = (EditText) findViewById(R.id.etName);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (Button) findViewById(R.id.btnSend);

        DraftInfo[] draftInfos = {new DraftInfo("WebSocket协议Draft_17", new Draft_17()), new DraftInfo
                ("WebSocket协议Draft_10", new Draft_10()), new DraftInfo("WebSocket协议Draft_76", new Draft_76()), new
                DraftInfo("WebSocket协议Draft_75", new Draft_75())};// 所有连接协议
        selectDraft = draftInfos[0];// 默认选择第一个连接协议

        ArrayAdapter<DraftInfo> draftAdapter = new ArrayAdapter<DraftInfo>(this, android.R.layout
                .simple_spinner_item, draftInfos);
        spDraft.setAdapter(draftAdapter);
        spDraft.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectDraft = (DraftInfo) spDraft.getItemAtPosition(position);// 选择连接协议

                etDetails.append("当前连接协议：" + selectDraft.draftName + "\n");

                Log.e("wlf", "选择连接协议：" + selectDraft.draftName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectDraft = null;// 清空

                Log.e("wlf", "未选择任何连接协议");
            }
        });

        ServerInfo[] serverInfos = {new ServerInfo("连接Java Web后台", "ws://172.100.101.106:9997"),
                new ServerInfo("连接Java Application后台", "ws://172.100.101.106:9997")};// 所有连接后台
        etAddress.setText(serverInfos[1].serverAddress);// 默认选择第一个连接协议

        ArrayAdapter<ServerInfo> serverAdapter = new ArrayAdapter<ServerInfo>(this, android.R.layout
                .simple_spinner_item, serverInfos);
        spAddress.setAdapter(serverAdapter);
        spAddress.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ServerInfo selectServerInfo = (ServerInfo) spAddress.getItemAtPosition(position);// 选择连接后台
                etAddress.setText(selectServerInfo.serverAddress);

                etDetails.append("当前连接后台：" + selectServerInfo.serverName + "\n");

                Log.e("wlf", "当前连接后台：" + selectServerInfo.serverName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectDraft = null;// 清空

                Log.e("wlf", "未选择任何连接后台");
            }
        });


        btnConnect.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        WebSocketImpl.DEBUG = true;
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");



        findViewById(R.id.btnBind).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:

                enqueue();

//                try {
//
////                    enqueue();
//                    if (selectDraft == null) {
//                        return;
//                    }
//                    String address = etAddress.getText().toString().trim();
//                    if (address.contains("JSR356-WebSocket")) {
//                        address += etName.getText().toString().trim();
//                    }
//                    Log.e("wlf", "连接地址：" + address);
//                    Map<String,String> httpHeaders = new HashMap<>();
//                    httpHeaders.put("Origin", HOST);
//                    client = new WebSocketClient(new URI(address), selectDraft.draft, httpHeaders, 10000) {
//                        @Override
//                        public void onOpen(final ServerHandshake serverHandshakeData) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    etDetails.append("已经连接到服务器【" + getURI() + "】\n");
//
//                                    Log.e("wlf", "已经连接到服务器【" + getURI() + "】");
//
//                                    spDraft.setEnabled(false);
//                                    etAddress.setEnabled(false);
//                                    btnConnect.setEnabled(false);
//                                    etName.setEnabled(false);
//
//                                    btnClose.setEnabled(true);
//                                    btnSend.setEnabled(true);
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onMessage(final String message) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    etDetails.append("获取到服务器信息【" + message + "】\n");
//
//                                    Log.e("wlf", "获取到服务器信息【" + message + "】");
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onClose(final int code, final String reason, final boolean remote) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    etDetails.append("断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason +
//                                            "】\n");
//
//                                    Log.e("wlf", "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】");
//
//                                    spDraft.setEnabled(true);
//                                    etAddress.setEnabled(true);
//                                    btnConnect.setEnabled(true);
//                                    etName.setEnabled(true);
//
//                                    btnClose.setEnabled(false);
//                                    btnSend.setEnabled(false);
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError(final Exception e) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    etDetails.append("连接发生了异常【异常原因：" + e + "】\n");
//
//                                    Log.e("wlf", "连接发生了异常【异常原因：" + e + "】");
//
//                                    spDraft.setEnabled(true);
//                                    etAddress.setEnabled(true);
//                                    btnConnect.setEnabled(true);
//                                    etName.setEnabled(true);
//
//                                    btnClose.setEnabled(false);
//                                    btnSend.setEnabled(false);
//                                }
//                            });
//                        }
//                    };
//                    client.connect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                break;
            case R.id.btnClose:
                if (client != null) {
                    client.close();
                }
                break;
            case R.id.btnSend:
                try {
                    if (client != null) {
                        client.send(etName.getText().toString().trim() + "说：" + etMessage.getText().toString().trim());
                        svChat.post(new Runnable() {
                            @Override
                            public void run() {
                                svChat.fullScroll(View.FOCUS_DOWN);
                                etMessage.setText("");
                                etMessage.requestFocus();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnBind:

                BindRequest request = new BindRequest();
                request.setSign(request.getSign());

                sendMessage(gson.toJson(request));
                break;
        }
    }

    private void sendMessage(final String message) {

        Log.e("ChatClientActivity", "send To Server" + message);

        if (socket != null && message != null) {

            AsyncTaskExecutors.executeTask(new Runnable() {
                @Override
                public void run() {
                    RequestBody requestBody = RequestBody.create(okhttp3.ws.WebSocket.TEXT, message);
                    try {
                        socket.sendMessage(requestBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.close();
        }
    }

    private class DraftInfo {

        private final String draftName;
        private final Draft draft;

        public DraftInfo(String draftName, Draft draft) {
            this.draftName = draftName;
            this.draft = draft;
        }

        @Override
        public String toString() {
            return draftName;
        }
    }

    private class ServerInfo {

        private final String serverName;
        private final String serverAddress;

        public ServerInfo(String serverName, String serverAddress) {
            this.serverName = serverName;
            this.serverAddress = serverAddress;
        }

        @Override
        public String toString() {
            return serverName;
        }
    }


//
//    private void initWebSocketClient() {
//        Log.e("initWebSocketClient", "initWebSocketClient");
//        try {
//            URI url = new URI("ws://192.168.207.84:8080/");
//            WebSocket websocket = new WebSocketConnection(url);
//
//            // Register Event Handlers
//            websocket.setEventHandler(new WebSocketEventHandler() {
//                public void onOpen()
//                {
//                    Log.e(TAG, "--open");
//                }
//
//                public void onMessage(WebSocketMessage message)
//                {
//                    Log.e(TAG, "--received message: " + message.getText());
//                }
//
//                public void onClose()
//                {
//                    Log.e(TAG, "--close");
//                }
//            });
//
//            // Establish WebSocket Connection
//            websocket.connect();
//
//            // Send UTF-8 Text
//            websocket.send("hello world");
//
//            // Close WebSocket Connection
//            websocket.close();
//        }
//        catch (WebSocketException wse) {
//            wse.printStackTrace();
//        }
//        catch (URISyntaxException use) {
//            use.printStackTrace();
//        }
//    }



    private class MyLogger implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(String message) {
            System.err.println("OKHTTP3 ---->" + message);
        }
    }
}