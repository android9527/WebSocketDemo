package org.websocket.demo.proxy.connection;

import android.content.Context;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UdpConnection extends BaseConnection {
    private static final String TAG = "UdpConnection";

    private static final int MAX_LENGTH = 1024;

    private static final int PORT = 9002;

    private DatagramSocket receiveSocket = null;

    private ReceiveThread receiveThread;

    private boolean isRun = false;

    private static UdpConnection instance;

    public static UdpConnection getInstance(Context context) {

        if (instance == null) {
            synchronized (UdpConnection.class) {
                if (instance == null) {
                    instance = new UdpConnection(context);
                }
            }
        }
        return instance;
    }

    private UdpConnection(Context context) {
        super(context);
    }

    @Override
    public synchronized void connect(String url) {
        if (isRun) {
            return;
        }
        try {
            receiveSocket = new DatagramSocket(PORT);
            receiveThread = new ReceiveThread();
            receiveThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void close() {
        isRun = false;
        receiveSocket.disconnect();
        receiveSocket.close();
        receiveThread = null;
    }

    @Override
    public void sendMessage(String message) {
        // TODO
    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {
            Log.d(TAG, "startReceive");

            if (receiveSocket == null) {
                return;
            }

            isRun = true;

            while (isRun) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(
                            new byte[MAX_LENGTH], MAX_LENGTH);
                    receiveSocket.receive(receivePacket);

                    // get received data
                    byte data[] = receivePacket.getData();

                    if (data != null) {
//                        dataParser.parse(Arrays.copyOfRange(data, 0,
//                                receivePacket.getLength()));

                        notifyGetMessage(Arrays.toString(data));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
    }

}
