package org.websocket.demo.request;

public interface ITimerHandler {
    void timeoutHandle(String sequenceNumber, int status);
}
