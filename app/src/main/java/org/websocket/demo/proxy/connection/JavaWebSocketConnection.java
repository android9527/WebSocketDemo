package org.websocket.demo.proxy.connection;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.websocket.demo.proxy.ImpsConnection;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenfeiyue on 16/7/21.
 * JavaWebSocketConnection
 */
public class JavaWebSocketConnection implements IConnection {
    private WebSocketClient client;
    private ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

    @Override
    public void disConnect() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void addImpsConnection(ImpsConnection impsConnection) {
        if (!impsConnections.contains(impsConnection)) {
            impsConnections.add(impsConnection);
        }
    }

    public void connect(String url) {
        try {
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("Origin", url);
            client = new WebSocketClient(new URI(url), new Draft_17(), httpHeaders, 20000) {
                @Override
                public void onOpen(final ServerHandshake serverHandshakeData) {

                    Log.e("wlf", "已经连接到服务器【" + getURI() + "】");
                }

                @Override
                public void onMessage(final String message) {

                    Log.e("wlf", "获取到服务器信息【" + message + "】");
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {

                    Log.e("wlf", "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】" + remote);
                }

                @Override
                public void onError(final Exception e) {

                    Log.e("wlf", "连接发生了异常【异常原因：" + e + "】");
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
