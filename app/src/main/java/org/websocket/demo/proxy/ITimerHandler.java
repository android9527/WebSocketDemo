package org.websocket.demo.proxy;

public interface ITimerHandler {
    void timeoutHandle(String sequenceNumber, int status);
}
