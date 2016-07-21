package org.websocket.demo.proxy.connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

import org.websocket.demo.proxy.ImpsConnection;

public class UdpConnection implements IConnection{
    private static final String TAG = "UdpConnection";

    private static final int MAX_LENGTH = 1024;

    private static final int PORT = 9002;

    private DatagramSocket receiveSocket = null;

    private ReceiveThread receiveThread;

    private boolean isRun = false;

    private static UdpConnection conn;

    private ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

    @Override
    public void addImpsConnection(ImpsConnection impsConnection) {
        if (!impsConnections.contains(impsConnection)) {
            impsConnections.add(impsConnection);
        }
    }

    public static UdpConnection getInstance(Context context) {
        if (conn == null) {
            conn = new UdpConnection(context);
        }
        return conn;
    }

    private UdpConnection(Context context) {
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
    public void disConnect() {
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


    private void notifyListener(boolean isConnected) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.connectedNotify(isConnected);
            }
        }
    }

    private void notifyGetMessage(String response) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
//                            impsConnection.receiveMsg(getMessage(response));
                if (null == impsConnection)
                    continue;
                impsConnection.receiveMsg(response);
            }
        }
    }

    private void notifySendMessage(String message) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
//                            impsConnection.receiveMsg(getMessage(response));
                if (null == impsConnection)
                    continue;
                impsConnection.sendedMessage(message);
            }
        }
    }
}
