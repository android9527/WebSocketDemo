package org.websocket.demo.proxy.connection;

/**
 * Created by chenfeiyue on 16/7/20.
 * IConnection
 */
public interface IConnection {
    void connect(String url);

    void disConnect();

    void sendMessage(String message);
}
