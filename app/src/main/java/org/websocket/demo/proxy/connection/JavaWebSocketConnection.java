package org.websocket.demo.proxy.connection;

import android.content.Context;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.websocket.demo.request.Constant;
import org.websocket.demo.util.LogUtil;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenfeiyue on 16/7/21.
 * JavaWebSocketConnection
 */
public class JavaWebSocketConnection extends BaseConnection {

    private static final String TAG = "JavaWebSocketConnection";
    private WebSocketClient socket;

    private static JavaWebSocketConnection instance;

    private boolean isConnected = false;

    public JavaWebSocketConnection(Context context) {
        super(context);
    }

    public static JavaWebSocketConnection instance(Context context) {
        if (instance == null) {
            synchronized (JavaWebSocketConnection.class) {
                if (instance == null) {
                    instance = new JavaWebSocketConnection(context);
                }
            }
        }
        return instance;
    }

    @Override
    public boolean sendMessage(String message) {
        // TODO
        return super.sendMessage(message);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void realConnect(String url) {
        try {
            isConnected = false;
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("Origin", url);
            socket = new WebSocketClient(new URI(url), new Draft_17(), httpHeaders, Constant.DEFAULT_CONNECT_TIMEOUT) {
                @Override
                public void onOpen(final ServerHandshake serverHandshakeData) {
                    connecting = false;
                    isConnected = true;
                    LogUtil.d(TAG, "已经连接到服务器【" + getURI() + "】");
                    notifyListener(true);
                }

                @Override
                public void onMessage(final String message) {
                    connecting = false;
                    isConnected = true;
                    LogUtil.d(TAG, "获取到服务器信息【" + message + "】");
                    if (message != null) {
                        System.out.println("onMessage " + message);
                        notifyGetMessage(message);
                    }
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {
                    connecting = false;
                    isConnected = false;
                    LogUtil.d(TAG, "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】" + remote);
                    notifyListener(false);
                }

                @Override
                public void onError(final Exception e) {
                    LogUtil.d(TAG, "连接发生了异常【异常原因：" + e + "】");
                    connecting = false;
                    isConnected = false;
                    notifyListener(false);
                }
            };
            socket.connect();
            connecting = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        if (socket == null) {
            return;
        }
        socket.close(1001, "closed by client");
    }
}
