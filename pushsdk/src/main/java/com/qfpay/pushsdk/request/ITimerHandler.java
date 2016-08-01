package com.qfpay.pushsdk.request;

public interface ITimerHandler {
    void timeoutHandle(String sequenceNumber, int status);
}
