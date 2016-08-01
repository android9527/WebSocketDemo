package com.qfpay.push.proxy;

/**
 * Created by chenfeiyue on 16/7/25.
 * PrintThread
 */
public class PrintThread extends Thread {

    @Override
    public void run() {

        try {
            ServiceProxy.getInstance().printMsg();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ServiceProxy.getInstance().disConnectPrint();
        }
    }

    public void disConnect() {
        ServiceProxy.getInstance().stopPrintMsg();
        interrupt();
    }

    public void connect() {
        start();
    }
}
