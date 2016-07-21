package org.websocket.demo.proxy.connection;

import android.content.Context;

import org.websocket.demo.proxy.ImpsConnection;
import org.websocket.demo.scheduletask.ScheduleTaskService;

import java.util.ArrayList;

/**
 * Created by chenfeiyue on 16/7/21.
 * BaseConnection
 */
public abstract class BaseConnection implements IConnection {
    public Context mContext;

    public boolean connecting = false;

    public ArrayList<ImpsConnection> impsConnections = new ArrayList<>();

    public BaseConnection(Context context){
        this.mContext = context.getApplicationContext();
        ScheduleTaskService.getInstance().init(mContext);
    }

    @Override
    public void addImpsConnection(ImpsConnection impsConnection) {
        if (!impsConnections.contains(impsConnection)) {
            impsConnections.add(impsConnection);
        }
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void disConnect() {
        close();
    }

    protected abstract void close();

    public void notifyListener(boolean isConnected) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.connectedNotify(isConnected);
            }
        }
    }

    public void notifyGetMessage(String response) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
//                            impsConnection.receiveMsg(getMessage(response));
                if (null == impsConnection)
                    continue;
                impsConnection.receiveMsg(response);
            }
        }
    }

    public void notifySendMessage(String message) {
        if (impsConnections != null && impsConnections.size() > 0) {
            for (ImpsConnection impsConnection : impsConnections) {
                if (null == impsConnection)
                    continue;
                impsConnection.sendedMessage(message);
            }
        }
    }

}
