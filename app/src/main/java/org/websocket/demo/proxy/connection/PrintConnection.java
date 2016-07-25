package org.websocket.demo.proxy.connection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;

import org.websocket.demo.WebSocketService;
import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.util.Constant;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;
import org.websocket.demo.util.ToastUtil;

/**
 * Created by chenfeiyue on 16/7/25.
 * PrintConnection
 */
public class PrintConnection {

    private static final String TAG = "PrintConnection";
    private static PrintConnection connection;
    private int mPrinterId = 0;

    private GpService mGpService = null;

    private PrinterServiceConnection conn = null;

    private PortParameters mPortParam = new PortParameters();
    private Context mContext;

    private Intent intent;

    public static PrintConnection getInstance() {
        if (connection == null) {
            synchronized (PrintConnection.class) {
                if (connection == null) {
                    connection = new PrintConnection();
                }
            }
        }
        return connection;
    }

    public void init(Context context) {
        if (mContext != null) {
            return;
        }
        mContext = context.getApplicationContext();
        bindService();
        registerBroadcast();
    }

    public void disConnect() {
        disconnectToDevice();
    }

    public void shutdown() {
        disconnectToDevice();
        unBindService();
        try {
            mContext.unregisterReceiver(printerStatusBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置打印机参数
     *
     * @param ip   ip
     * @param port port
     */
    private void setPrintParameters(String ip, String port) {
        mPortParam.setPortType(PortParameters.ETHERNET);
        mPortParam.setIpAddr(ip);
        mPortParam.setPortNumber(Integer.valueOf(port));
        if (checkPortParameters(mPortParam)) {
            PortParamDataBase database = new PortParamDataBase(mContext);
            database.deleteDataBase("" + 0);
            database.insertPortParam(mPrinterId, mPortParam);
        } else {
            ToastUtil.showLong(mContext, "打印机参数错误！");
        }
    }

    /**
     * 检查打印机参数
     *
     * @param param param
     * @return true
     */
    private boolean checkPortParameters(PortParameters param) {
        if (null == param) {
            return false;
        }
        boolean rel = false;
        int type = param.getPortType();
        if (type == PortParameters.BLUETOOTH) {
            if (!param.getBluetoothAddr().equals("")) {
                rel = true;
            }
        } else if (type == PortParameters.ETHERNET) {
            if ((!param.getIpAddr().equals("")) && (param.getPortNumber() != 0)) {
                rel = true;
            }
        } else if (type == PortParameters.USB) {
            if (!param.getUsbDeviceName().equals("")) {
                rel = true;
            }
        }
        return rel;
    }


    /**
     * 连接打印机
     */
    public void connect() {
        String ip = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PRINT_IP, Constant.DEFAULT_PRINT_IP);
        String port = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PRINT_PORT, Constant.DEFAULT_PRINT_PORT);
        setPrintParameters(ip, port);

        LogUtil.e(TAG, String.valueOf(mPortParam.getPortOpenState()));
        if (!checkPortParameters(mPortParam)) {
            ToastUtil.showLong(mContext, "打印机参数错误！");
            return;
        }
        int rel = 0;
        switch (mPortParam.getPortType()) {
            case PortParameters.USB:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getUsbDeviceName(), 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case PortParameters.ETHERNET:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getIpAddr(), mPortParam.getPortNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case PortParameters.BLUETOOTH:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getBluetoothAddr(), 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
        LogUtil.e(TAG, "result :" + String.valueOf(r));
        if (r != GpCom.ERROR_CODE.SUCCESS) {
            if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
                mPortParam.setPortOpenState(true);
            } else {
//                ToastUtil.showLong(mContext, GpCom.getErrorText(r));
                // TODO
                LogUtil.d(TAG, GpCom.getErrorText(r));
            }
        }
    }

    /**
     * 断开打印机
     */
    private void disconnectToDevice() {
        if (null != mPortParam && mPortParam.getPortOpenState()) {
            LogUtil.d(TAG, "DisconnectToDevice ");
            try {
                mGpService.closePort(mPrinterId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void bindService() {
        conn = new PrinterServiceConnection();
        LogUtil.d(TAG, "connection");
        intent = new Intent("com.gprinter.aidl.GpPrintService");
        intent.setPackage(mContext.getPackageName());
        mContext.startService(intent);
        mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    private void unBindService() {
        LogUtil.d(TAG, "unBindService");
        if (conn != null) {
            try {
                mContext.unbindService(conn);
                mContext.stopService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            LogUtil.d(TAG, "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
        }
    }


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GpCom.ACTION_CONNECT_STATUS);
        mContext.registerReceiver(printerStatusBroadcastReceiver, filter);
    }

    private BroadcastReceiver printerStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!GpCom.ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                return;
            }

            context.startService(new Intent(context, WebSocketService.class));
            int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
            if (mPrinterId != id) {
                return;
            }
            int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
            LogUtil.d(TAG, "connect status " + type);
            switch (type) {
                case GpDevice.STATE_CONNECTING:
                    mPortParam.setPortOpenState(false);
                    break;
                case GpDevice.STATE_NONE:
                    mPortParam.setPortOpenState(false);
                    ServiceProxy.getInstance().stopPrintThread();
                    break;
                case GpDevice.STATE_VALID_PRINTER:
                    // 连接打印机成功
                    mPortParam.setPortOpenState(true);
                    LogUtil.e(TAG, "连接打印机成功！");
                    ToastUtil.showLong(mContext, "连接打印机成功！");
                    ServiceProxy.getInstance().startPrintThread();

                    break;
                case GpDevice.STATE_INVALID_PRINTER:
                default:
                    ToastUtil.showLong(mContext, "Please use Gprinter!");
                    ServiceProxy.getInstance().stopPrintThread();
                    break;
            }
        }
    };

    public boolean printText(String text) {
        if (mContext == null) {
            LogUtil.d("mContext == null");
            return false;
        }

        boolean result = false;
        try {
            int rel = mGpService.sendEscCommand(mPrinterId, text);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
//                ToastUtil.showLong(mContext, GpCom.getErrorText(r));
                result = false;
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
