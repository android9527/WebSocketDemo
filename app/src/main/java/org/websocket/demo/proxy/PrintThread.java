package org.websocket.demo.proxy;

/**
 * Created by chenfeiyue on 16/7/25.
 * PrintThread
 */
public class PrintThread extends Thread {

    private boolean isRun = false;

    @Override
    public void run() {
        isRun = true;

        while (isRun) {
            try {
                ServiceProxy.getInstance().printMsg();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void disConnect() {
        isRun = false;
        interrupt();
    }

    public void connect() {
        start();
    }
}
