package org.websocket.demo.proxy.connection;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.websocket.demo.AsyncTaskExecutors;
import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.proxy.OkHttp3Creator;
import org.websocket.demo.proxy.TcpMessage;
import org.websocket.demo.proxy.TcpMessageParser;
import org.websocket.demo.scheduletask.ScheduleTaskService;
import org.websocket.demo.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

/**
 * Created by chenfeiyue on 16/7/20.
 * OkHttpWebSocketConnection
 */
public class OkHttpWebSocketConnection implements IConnection {

    private static final String TAG = "WebSocketConnection";
    private final Gson gson = new Gson();
        private static OkHttpWebSocketConnection instance;
    private Context mContext;

    private boolean connecting = false;

    private ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

    public OkHttpWebSocketConnection(Context context) {
        this.mContext = context.getApplicationContext();
        ScheduleTaskService.getInstance().init(mContext.getApplicationContext());
    }

    public static OkHttpWebSocketConnection instance(Context context) {
        if (instance == null) {
            synchronized (OkHttp3Creator.class) {
                if (instance == null) {
                    instance = new OkHttpWebSocketConnection(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void addImpsConnection(ImpsConnection impsConnection) {
        if (!impsConnections.contains(impsConnection)) {
            impsConnections.add(impsConnection);
        }
    }

    private okhttp3.ws.WebSocket socket;

    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(okhttp3.ws.WebSocket webSocket, Response response) {
            System.out.println("onOpen");
            socket = webSocket;
            connecting = false;
            notifyListener(true);
        }

        @Override
        public void onFailure(IOException e, Response response) {
            e.printStackTrace();
            System.out.println("onFailure");
            socket = null;
            connecting = false;
            notifyListener(false);
            if (response != null) {
                System.out.println("onFailure" + "   " + response.message());
            }
        }

        @Override
        public void onMessage(ResponseBody message) throws IOException {
            System.out.println("onMessage");
            connecting = false;
            if (message != null) {
                String response = message.string();
                System.out.println("onMessage" + response);
                notifyGetMessage(response);
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
            connecting = false;
            notifyListener(false);
        }
    };

    @Override
    public synchronized void connect(String url) {
        if (connecting) {
            LogUtil.w(TAG, "TCP is connecting return !");
            return;
        }
        OkHttpClient okHttpClient = OkHttp3Creator.instance(mContext).getOkHttp3Client();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).addHeader("Origin", url).
                build();
        WebSocketCall call = WebSocketCall.create(okHttpClient, request);
        call.enqueue(webSocketListener);
        connecting = true;
    }

    @Override
    public void disConnect() {
        close();
    }

    private synchronized void close() {
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
    public void sendMessage(final String message) {

        Log.e("ChatClientActivity", "send To Server" + message);

        if (socket == null || message == null) {
            return;
        }
        AsyncTaskExecutors.executeTask(new Runnable() {
            @Override
            public void run() {
                sendMsgToServer(message);
            }
        });
    }

    private synchronized void sendMsgToServer(final String message) {
        try {
            RequestBody requestBody = RequestBody.create(okhttp3.ws.WebSocket.TEXT, message);
            socket.sendMessage(requestBody);
            notifySendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // TODO 消息封装
    private TcpMessage getMessage(String response) throws IOException {
        TcpMessage msg = null;
        msg = TcpMessageParser.bytes2TcpMsg(response.getBytes());
        return msg;
    }

    private void notifyListener(boolean isConnected) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.connectedNotify(isConnected);
            }
        }
    }

    private void notifyGetMessage(String response) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
//                            impsConnection.receiveMsg(getMessage(response));
                if (null == impsConnection)
                    continue;
                impsConnection.receiveMsg(response);
            }
        }
    }

    private void notifySendMessage(String message) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.sendedMessage(message);
            }
        }
    }
}
