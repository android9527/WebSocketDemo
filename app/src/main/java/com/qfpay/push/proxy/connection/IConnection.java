package com.qfpay.push.proxy.connection;

import com.qfpay.push.request.ImpsConnection;

/**
 * Created by chenfeiyue on 16/7/20.
 * IConnection
 */
public interface IConnection {
    void connect(String url);

    void disConnect(boolean needReConnect);

    boolean sendMessage(String message);

    void addImpsConnection(ImpsConnection impsConnection);

    void removeImpsConnection(ImpsConnection impsConnection);

    void removeAllImpsConnection();

    boolean isConnected();

    void reConnect(String url);

    void stopReConnect();
}
