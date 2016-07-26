package org.websocket.demo.proxy.connection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;

import org.websocket.demo.proxy.ServiceProxy;
import org.websocket.demo.util.Constant;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;
import org.websocket.demo.util.ToastUtil;

/**
 * Created by chenfeiyue on 16/7/25.
 * PrintConnection
 */
public class PrinterConnection {

    private static final String TAG = "PrintConnection";
    private static PrinterConnection connection;
    private int mPrinterId = 0;

    private GpService mGpService = null;

    private PrinterServiceConnection conn = null;

    private PortParameters mPortParam = new PortParameters();
    private Context mContext;

    private Intent intent;

    public static PrinterConnection getInstance() {
        if (connection == null) {
            synchronized (PrinterConnection.class) {
                if (connection == null) {
                    connection = new PrinterConnection();
                }
            }
        }
        return connection;
    }

    public void init(Context context) {
        if (mContext != null) {
            return;
        }
        LogUtil.d(TAG, "init ");
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
        mContext = null;
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
    public synchronized void connect() {
        if (null == mGpService) {
            LogUtil.d(TAG, "the mGpService is null !");
            return;
        }
        String ip = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PRINT_IP, Constant.DEFAULT_PRINT_IP);
        String port = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PRINT_PORT, Constant.DEFAULT_PRINT_PORT);
        setPrintParameters(ip, port);

        if (!checkPortParameters(mPortParam)) {
            LogUtil.d(TAG, "打印机参数错误！");
            return;
        }
        LogUtil.e(TAG, String.valueOf(mPortParam.getPortOpenState()));
        int rel = 0;
        switch (mPortParam.getPortType()) {
            case PortParameters.ETHERNET:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getIpAddr(), mPortParam.getPortNumber());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case PortParameters.USB:
                try {
                    rel = mGpService.openPort(mPrinterId, mPortParam.getPortType(), mPortParam.getUsbDeviceName(), 0);
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
    private synchronized void disconnectToDevice() {
        LogUtil.d(TAG, "DisconnectToDevice ");
        // TODO
        if (null == mGpService) {
            LogUtil.d(TAG, "the mGpService is null !");
            return;
        }
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

    public BroadcastReceiver printerStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!GpCom.ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                return;
            }

//            context.startService(new Intent(context, WebSocketService.class));
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
                    ServiceProxy.getInstance().getServiceHandler().removeMessages(ServiceProxy.MSG_PRINT_TEXT);
                    break;
                case GpDevice.STATE_VALID_PRINTER:
                    // 连接打印机成功
                    mPortParam.setPortOpenState(true);
                    LogUtil.e(TAG, "连接打印机成功！");
                    ServiceProxy serviceProxy = ServiceProxy.getInstance();
                    ServiceProxy.ServiceHandler handler = serviceProxy.getServiceHandler();
                    if (!handler.hasMessages(ServiceProxy.MSG_PRINT_TEXT)) {
                        Message msg = handler.obtainMessage(ServiceProxy.MSG_PRINT_TEXT);
                        handler.sendMessageDelayed(msg, 3000L);
                    }
                    break;
                case GpDevice.STATE_INVALID_PRINTER:
                default:
                    LogUtil.e(TAG, "Please use Gprinter!");
                    ServiceProxy.getInstance().getServiceHandler().removeMessages(ServiceProxy.MSG_PRINT_TEXT);
                    break;
            }
        }
    };

    public synchronized boolean printText(String text) throws RemoteException {
        if (mContext == null || mGpService == null) {
            LogUtil.d("mContext == null ||  mGpService == null");
            return false;
        }

        boolean result = false;
//        try {
        int rel = mGpService.sendEscCommand(mPrinterId, text);
        GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
        if (r != GpCom.ERROR_CODE.SUCCESS) {
//                ToastUtil.showLong(mContext, GpCom.getErrorText(r));
            result = false;
        } else {
            result = true;
        }
//        } finally {
//            disConnect();
//        }
        return result;
    }

    public void getPrinterStatus() {
        if (mGpService == null) {
            LogUtil.d(TAG, "未连接打印机");
            return;
        }

        try {
            int status = mGpService.queryPrinterStatus(mPrinterId, 500);
            String str = "";
            if (status == GpCom.STATE_NO_ERR) {
                str = "打印机正常";
            } else {
                str = "打印机 ";
                if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
                    str += "脱机";
                }
                if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
                    str += "缺纸";
                }
                if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
                    str += "打印机开盖";
                }
                if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
                    str += "打印机出错";
                }
                if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
                    str += "查询超时";
                }
            }
            LogUtil.d(TAG, "打印机：" + mPrinterId + " 状态：" + str);
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
