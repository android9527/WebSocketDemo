package com.qfpay.push.request;

public interface ITimerHandler {
    void timeoutHandle(String sequenceNumber, int status);
}
