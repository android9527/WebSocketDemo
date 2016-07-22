package org.websocket.demo.proxy.connection;

import org.websocket.demo.proxy.ImpsConnection;

/**
 * Created by chenfeiyue on 16/7/20.
 * IConnection
 */
public interface IConnection {
    void connect(String url);

    void disConnect();

    void sendMessage(String message);

    void addImpsConnection(ImpsConnection impsConnection);

    void removeImpsConnection(ImpsConnection impsConnection);

    boolean isConnected();

    void reConnect(String url);

    void stopReConnect();
}
