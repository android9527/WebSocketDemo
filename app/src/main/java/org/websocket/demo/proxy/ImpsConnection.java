package org.websocket.demo.proxy;

public interface ImpsConnection {
    void receiveMsg(TcpMessage msg);

    void receiveMsg(String msg);

    void connectedNotify(boolean status, boolean needReConnect);

    void sendedMessage(String msg);
}
