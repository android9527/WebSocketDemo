package com.qfpay.push.proxy.connection;

import android.content.Context;
import android.text.TextUtils;

import com.qfpay.push.proxy.OkHttp3Creator;
import com.qfpay.push.util.LogUtil;

import java.io.IOException;

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
public class OkHttpWebSocketConnection extends BaseConnection {

    private static final String TAG = "OkHttpWebSocketConnection";
    private static OkHttpWebSocketConnection instance;
    private okhttp3.ws.WebSocket socket;

    public OkHttpWebSocketConnection(Context context) {
        super(context);
    }

    public static OkHttpWebSocketConnection instance(Context context) {
        if (instance == null) {
            synchronized (OkHttpWebSocketConnection.class) {
                if (instance == null) {
                    instance = new OkHttpWebSocketConnection(context);
                }
            }
        }
        return instance;
    }

    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(okhttp3.ws.WebSocket webSocket, Response response) {
            LogUtil.d(TAG, "onOpen");
            socket = webSocket;
            connecting = false;
            notifyListener(true);
        }

        @Override
        public void onFailure(IOException e, Response response) {
            e.printStackTrace();
            String reason = "";
            if (response != null) {
                reason = response.message();
                response.close();
            }
            LogUtil.d(TAG, "onFailure   " + reason);
            socket = null;
            connecting = false;
            notifyListener(false);

        }

        @Override
        public void onMessage(ResponseBody message) throws IOException {
            connecting = false;
            if (message != null) {
                String response = message.string();
                LogUtil.d(TAG, "onMessage" + response);
                notifyGetMessage(response);
                message.close();
            }
        }

        @Override
        public void onPong(Buffer payload) {
            connecting = false;
        }

        @Override
        public void onClose(int code, String reason) {
            LogUtil.d(TAG, "onClose" + reason);
            socket = null;
            connecting = false;
            notifyListener(false);
        }
    };

    @Override
    public synchronized void realConnect(String url) {
        OkHttpClient okHttpClient = OkHttp3Creator.instance(mContext).getOkHttp3Client();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).addHeader("Origin", url).
                build();
        WebSocketCall call = WebSocketCall.create(okHttpClient, request);
        call.enqueue(webSocketListener);
        connecting = true;
    }

    @Override
    public synchronized void close() {
        if (socket == null) {
            return;
        }
        try {
            socket.close(1001, "closed by client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendMessage(final String message) {

        LogUtil.d(TAG, "send To Server " + message);

        return !(!isConnected() || TextUtils.isEmpty(message)) && sendMsgToServer(message);
    }

    @Override
    public boolean isConnected() {
        return socket != null;
    }


    /**
     * 发送数据到服务器
     * @param message message
     * @return true send
     */
    private synchronized boolean sendMsgToServer(final String message) {
        try {
            RequestBody requestBody = RequestBody.create(okhttp3.ws.WebSocket.TEXT, message);
            socket.sendMessage(requestBody);
            notifySendMessage(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
