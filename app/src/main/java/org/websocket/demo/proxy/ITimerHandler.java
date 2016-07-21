package org.websocket.demo.proxy;

public interface ITimerHandler {
    void timeoutHandle(short sequenceNumber, int status);
}
