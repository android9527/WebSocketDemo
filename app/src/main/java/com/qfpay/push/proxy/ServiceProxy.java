package com.qfpay.push.proxy;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.gprinter.command.EscCommand;
import com.qfpay.push.proxy.connection.IConnection;
import com.qfpay.push.proxy.connection.OkHttpWebSocketConnection;
import com.qfpay.push.proxy.connection.PrinterConnection;
import com.qfpay.push.request.BaseRequest;
import com.qfpay.push.request.BindRequest;
import com.qfpay.push.request.HeartbeatRequest;
import com.qfpay.push.request.Http;
import com.qfpay.push.request.ImpsConnection;
import com.qfpay.push.request.PushResponse;
import com.qfpay.push.request.RequestParam;
import com.qfpay.push.request.SocketRequest;
import com.qfpay.push.request.TcpMessage;
import com.qfpay.push.request.TimeoutHandler;
import com.qfpay.push.scheduletask.ScheduleTask;
import com.qfpay.push.scheduletask.ScheduleTaskService;
import com.qfpay.push.util.AsyncTaskExecutors;
import com.qfpay.push.util.Constant;
import com.qfpay.push.util.DeviceUtil;
import com.qfpay.push.util.LogUtil;
import com.qfpay.push.util.SPUtil;
import com.qfpay.push.util.ToastUtil;
import com.qfpay.push.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceProxy implements ScheduleTask.Callback, ImpsConnection {
    private TimeoutHandler timeoutHandler = new TimeoutHandler();

    private static final String TAG = "serviceProxy";

    private Context mContext;

    private int reBindCount = 0;

    /**
     * 网络状态监听器
     */
    private NetworkConnectivityListener mNetworkConnectivityListener;

    /**
     * 网络切换事件
     */
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;

    public static final int MSG_PRINT_TEXT = EVENT_NETWORK_STATE_CHANGED + 1;

    public static final int MSG_PRINTER_CONNECT_FAILED = MSG_PRINT_TEXT + 1;

    private ServiceHandler mServiceHandler;

    private IConnection connection;

    private Gson gson = new Gson();

    private static ServiceProxy serviceProxy;


    // 打印队列
    private LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    /**
     * 消息请求队列
     */
    private static Hashtable<String, SocketRequest> reqQueue = new Hashtable<>();

    /**
     * 打印线程
     */
    private PrintThread printThread;

    /**
     * 单线程打印，防止每次连接上打印机后都会开启线程打印
     */
    private boolean isPrinting = false;

    /**
     * 自己发起的连接打印机请求，连接广播之间互相影响
     */
    private boolean isStartByMe = false;

    public ServiceProxy() {
        mServiceHandler = new ServiceHandler(ServiceProxy.this);
    }

    public LinkedBlockingQueue<String> getBlockingQueue() {
        return blockingQueue;
    }

    public void init(Context context) {
        if (mContext != null) {
            return;
        }

        LogUtil.d(TAG, "init");
        mContext = context.getApplicationContext();
        ScheduleTaskService.getInstance().init(mContext);
        connection = OkHttpWebSocketConnection.instance(mContext);
        initListener(mContext);
        PrinterConnection.getInstance().init(mContext);
    }

    private void initListener(Context context) {
        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.registerHandler(mServiceHandler,
                EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(context);
        connection.addImpsConnection(this);
    }

    public static ServiceProxy getInstance() {
        if (serviceProxy == null) {
            synchronized (ServiceProxy.class) {
                if (serviceProxy == null) {
                    serviceProxy = new ServiceProxy();
                }
            }
        }
        return serviceProxy;
    }

    /**
     * 是否已连接到Server
     *
     * @return 是否连接
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * 类描述：监听网络连接回调Handler 修改时间：
     */
    public static final class ServiceHandler extends BaseHandler<ServiceProxy> {
        public ServiceHandler(ServiceProxy proxy) {
            super(proxy);
        }

        @Override
        public void onHandleMessage(Message msg, ServiceProxy proxy) {
            switch (msg.what) {
                case EVENT_NETWORK_STATE_CHANGED:
                    proxy.networkStateChanged();
                    break;
                case MSG_PRINT_TEXT:
                    proxy.startPrintMsg();
                    break;
                case MSG_PRINTER_CONNECT_FAILED:
                    proxy.connPrintFail();
                    break;
                default:
            }
        }
    }

    public ServiceHandler getServiceHandler() {
        return mServiceHandler;
    }

    /**
     * 方法名称：networkStateChanged 方法描述：网络状态改变时回被调用 输入参数： 返回类型：void： 备注：
     */
    private void networkStateChanged() {
        if (null == mNetworkConnectivityListener) {
            LogUtil.d(TAG, "networkStateChanged: return");
            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();

        if (null == networkInfo) {
            LogUtil.d(TAG, "networkInfo = null");
            return;
        }

        NetworkInfo.State state = networkInfo.getState();

        switch (state) {
            case CONNECTED:
                connect();
                break;
            case SUSPENDED:
            case DISCONNECTED:
                LogUtil.d(TAG, "DISCONNECTED");

                // TODO
                connectedNotify(false, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveMsg(TcpMessage msg) {
        processRsp(msg);
    }

    @Override
    public void receiveMsg(String msg) {
//        parseMessage(msg);
    }

    /**
     * 当连接到服务器会回调该方法
     */
    @Override
    public void connectedNotify(boolean status, boolean needReConnect) {
        LogUtil.d(TAG, "connectedNotify:" + "ConnectStatus:" + status);
        if (status) {
            startBindClient();
        } else {
//            ScheduleTaskService.getInstance().shutdown();
            disConnect(needReConnect);
        }
    }

    @Override
    public void sendedMessage(String msg) {

    }


    /**
     * 断开服务器连接
     *
     * @param needReConnect 是否需要重连
     */
    public void disConnect(boolean needReConnect) {
        connection.disConnect(needReConnect);
        stopPrintMsg();
        PrinterConnection.getInstance().disConnect();
    }


    /**
     * 连接服务器
     */
    public void connect() {
        Constant.app_type = DeviceUtil.getAppType(mContext);
        Constant.secret_key = DeviceUtil.getSecretKey(mContext);

        if (TextUtils.isEmpty(Constant.app_type) || TextUtils.isEmpty(Constant.secret_key)) {
            ToastUtil.showLong(mContext, "请检查Manifest配置！");
            connection.disConnect(false);
        } else if (!isConnected()) {
            String pushUrl = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_PUSH_URL, Constant.URL, false);
            connection.connect(pushUrl);
        }

    }


    /**
     * 绑定打印机
     */
    public void startBindClient() {
        BindRequest request = new BindRequest();

        String appType = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_APP_TYPE, Constant.app_type, false);
        request.setUserid(SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_USER_ID, Constant.userid, false));
        request.setApptype(appType);
        request.setDeviceid(DeviceUtil.getUniqueId(mContext));
        Constant.secret_key = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_SECRET_KEY, Constant.secret_key, false);

        request.setSign(request.getSign());
        sendRequest(request, true/*, true*/);
    }

    /**
     * 取消重连
     */
    public void stopReBindClient() {
        reBindCount = 0;
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .stopSchedule(reBindCallback);
        LogUtil.d(TAG, "Stop stopReBindClient...leave");
    }

    /**
     * 重新绑定打印机
     */
    private void startReBindClient() {
        LogUtil.d(TAG, "startReBindClient()");
        int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RESEND_COUNT, Constant.DEFAULT_RESEND_COUNT, false);
        if (reBindCount >= count) {
            return;
        }
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(reBindCallback, getReconnectInterval());
    }

    /**
     * 重新绑定打印机Callback
     */
    ScheduleTask.Callback reBindCallback = new ScheduleTask.Callback() {
        @Override
        public long doSchedule() {
            reBindCount++;
            LogUtil.d(TAG, "重新绑定打印机次数 " + reBindCount);
            startBindClient();
            int count = SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_RECONNECT_COUNT,
                    Constant.DEFAULT_RECONNECT_COUNT, false);
            if (reBindCount >= count) {
                return 0;
            }
            return getReconnectInterval();
        }
    };

    /**
     * 获取重连间隔
     *
     * @return 重连间隔
     */
    public long getReconnectInterval() {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_MSG_RESENT_INTERVAL,
                Constant.DEFAULT_RECONNECT_INTERVAL, false);
    }

    /**
     * 发送请求
     *
     * @param tcpMessage     tcpMessage
     * @param isNeedResponse 是否需要响应
     *                       //     * @param isNeedResend   是否需要重发
     * @return Http
     */
    public Http sendRequest(TcpMessage tcpMessage, boolean isNeedResponse/*,
                            boolean isNeedResend*/) {
        if (!isConnected()) {
            return null;
        }
        Http http;
        try {
            RequestParam param = new RequestParam();
            param.setTimeout(SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_MSG_TIMEOUT, Constant.DEFAULT_TIMEOUT, false));
            param.setTimeHandler(timeoutHandler);
            SocketRequest socketRequest = new SocketRequest(param, tcpMessage);
            socketRequest.setNeedRsp(isNeedResponse);
//            socketRequest.setNeedResend(isNeedResend);
            socketRequest.setNeedResend(isNeedResponse);
            http = Http.sendRequest(socketRequest);
        } catch (Exception e) {
            e.printStackTrace();
            http = null;
        }

        return http;
    }


    /**
     * 发送请求
     *
     * @param request        request
     * @param isNeedResponse 是否需要响应
     *                       //     * @param isNeedResend   是否需要重发
     * @return Http
     */
    public Http sendRequest(BaseRequest request, boolean isNeedResponse/*,
                            boolean isNeedResend*/) {
        if (!isConnected()) {
            return null;
        }

        TcpMessage tcpMessage = new TcpMessage();
        tcpMessage.setRequest(request);

        return sendRequest(tcpMessage, isNeedResponse);
    }

    /**
     * 方法描述：处理响应消息 输入参数：@param socketMessage
     * 返回类型：void： 备注：
     */
    private void processRsp(TcpMessage tcpMessage) {
//        SocketRequest request = getSocketRequest(tcpMessage.getAnswerSequenceId());

        SocketRequest request = getSocketRequest(tcpMessage.getSequenceId());

        if (null == request) {
            LogUtil.d(TAG, "processRsp request == null");
        } else {
            request.getHttp().cancel();
        }
        parseMessage(tcpMessage.getBody());

    }

    /**
     * 方法描述：获取包含指定消息的请求 输入参数：@param
     * socketMessage 输入参数：@return 返回类型：SocketRequest： 备注：
     */
    public SocketRequest getSocketRequest(String sequenceId) {
        if (isEmptyReqQueue() || TextUtils.isEmpty(sequenceId)) {
            return null;
        }
        return reqQueue.get(sequenceId);
    }


    /**
     * 方法名称：startHeartBeat 方法描述：启动心跳 输入参数： 返回类型：void： 备注：
     */
    public void startHeartBeat() {
        // 启动心跳
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(this, getHeartBeatInterval());
    }

    /**
     * 心跳时间间隔
     *
     * @return 时间间隔
     */
    private long getHeartBeatInterval() {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_HEARTBEAT_INTERVAL,
                Constant.DEFAULT_HEARTBEAT_INTERVAL, false);
    }

    /**
     * 停止心跳数据
     */
    public void stopHeartBeat() {
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .stopSchedule(this);
        LogUtil.d(TAG, "Stop HeartBeat...leave");
    }

    /**
     * 设置心跳时间间隔
     */
    public void setHeartbeatInterval(long value) {
        SPUtil.getInstance(mContext).save(Constant.SPKey.KEY_HEARTBEAT_INTERVAL, value, false);
    }


    /**
     * 方法描述：发送心跳请求 输入参数： 返回类型：void： 备注：
     */
    public void sendHeartBeatRequest() {
        LogUtil.d(TAG, "------->sendHeartBeatRequest");
        try {
            // 构造心跳请求
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
            String appType = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_APP_TYPE, Constant.app_type, false);
            String secretKey = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_SECRET_KEY, Constant.secret_key, false);
            heartbeatRequest.setApptype(appType);
            Constant.secret_key = secretKey;
            heartbeatRequest.setSign(heartbeatRequest.getSign());
            sendRequest(heartbeatRequest, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消所以请求
     */
    public void cancelAllRequest() {
        LogUtil.d(TAG, "cancelAllRequest");
        if (this.isEmptyReqQueue()) {
            return;
        }
        Enumeration<SocketRequest> elements = reqQueue.elements();
        for (Enumeration<SocketRequest> element = elements; elements.hasMoreElements(); ) {
            SocketRequest request = element.nextElement();

            if (null != request) {
                Http http = request.getHttp();

                if (http != null) {
                    http.cancel();
                }
            }
        }
        delAllSocketRequest();
    }

    /**
     * 方法描述：删除所有请求 输入参数: 返回类型：void 备注：
     */
    private void delAllSocketRequest() {
        if (isEmptyReqQueue()) {
            return;
        }

        reqQueue.clear();
    }

    private boolean isEmptyReqQueue() {
        return null == reqQueue || reqQueue.isEmpty();
    }

    /**
     * 方法名称：shutdown 方法描述：停止服务 输入参数： 返回类型：void： 备注：
     */
    public void shutdown() {
        clearAll();
        stopPrintMsg();
        clearPrintQueue();
        connection.removeAllImpsConnection();
        connection.disConnect(false);
        PrinterConnection.getInstance().shutdown();
        mContext = null;
    }

    /**
     * 方法描述：清除内存数据 输入参数: 返回类型：void 备注：
     */
    private void clearAll() {
        // 清除缓存数据
        cancelAllRequest();
        ScheduleTaskService.getInstance().shutdown();
        stopNetworkListener();
    }

    /**
     * 停止网络监听
     */
    private void stopNetworkListener() {
        if (null != mNetworkConnectivityListener) {
            LogUtil.d(TAG, "unregisterHandler mNetworkConnectivityListener");
            mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
            mNetworkConnectivityListener.stopListening();
            mNetworkConnectivityListener = null;
        }
    }

    @Override
    public long doSchedule() {

        if (!isConnected()) {
            return 0;
        }

        sendHeartBeatRequest();

        return getHeartBeatInterval();
    }

    /**
     * 方法描述：从请求队列中删除一个请求 输入参数:@param
     * socketRequest 返回类型：void 备注：
     */
    public void delSocketRequest(SocketRequest request) {
        if (null == request) {
            return;
        }
        delSocketRequest(request.getSequenceNumber());
    }

    /**
     * 方法描述：删除一个流水号的请求 输入参数：@param
     * sequenceNumber 返回类型：void： 备注：
     */
    private void delSocketRequest(String sequenceNumber) {
        if (isEmptyReqQueue() || TextUtils.isEmpty(sequenceNumber)) {
            return;
        }

        LogUtil.d(TAG, "delete form requestQueue where sequenceNumber(pkg_id) = " + sequenceNumber);
        reqQueue.remove(sequenceNumber);
    }


    /**
     * 方法描述：发送消息 输入参数：@param socketRequest 返回类型：void： 备注：
     */
    public boolean send(SocketRequest request) {

        if (!isConnected()) {
            return false;
        }
        boolean result = false;
        try {

            // TODO
            result = sendMessage(request);
            if (request.isNeedRsp()) {
                addSocketRequest(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 方法描述：加入请求到队列中 输入参数：@param socketRequest
     * 返回类型：void： 备注：
     */
    private void addSocketRequest(SocketRequest request) {
        if (null == request || null == reqQueue) {
            return;
        }
        reqQueue.put(request.getSequenceNumber(), request);
    }

    /**
     * 方法描述：发送消息 输入参数：@param message 输入参数：@throws
     * IOException 返回类型：void： 备注：
     */
    private boolean sendMessage(SocketRequest request) throws IOException {
        if (null == request) {
            throw new IOException();
        }
        // TODO
        return sendMessage(TcpMessageParser.socketRequest2Message(request));
    }

    /**
     * 发送消息
     *
     * @param message message
     */
    private boolean sendMessage(String message) {
        return connection.sendMessage(message);
    }

    /**
     * 消息解析
     *
     * @param message message
     */
    private void parseMessage(String message) {
        try {
            JSONObject object = new JSONObject(message);
            if (!object.has("pkg_type")) {
                return;
            }
            String pkg_type = object.getString("pkg_type");

            switch (pkg_type) {

                // 绑定不成功
                case MessageId.PKG_DEVICE_BIND:
                    startReBindClient();
                    break;

                // 绑定成功
                case MessageId.PKG_DEVICE_BIND_ACK:
                    if (object.has("respcd")) {
                        stopReBindClient();
                        startHeartBeat();
                    }
                    break;
                // 收到推送
                case MessageId.PKG_DEVICE_PUSH:
                    parseBody(object);
                    break;
                default:
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * body 数据解析
     *
     * @param object object
     * @throws JSONException
     */
    private void parseBody(JSONObject object) throws JSONException {
        if (!object.has("msg_id")) {
            LogUtil.d(TAG, "-------> 数据无效！");
            return;
        }
        sendPushResponse(object);
        if (!object.has("msg")) {
            return;
        }
        String msg = object.getString("msg");
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        try {
            blockingQueue.put(msg);
            connectPrint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送推送响应
     *
     * @param object object
     * @throws JSONException
     */
    private void sendPushResponse(JSONObject object) throws JSONException {
            PushResponse response = new PushResponse();
            response.setMsgid(object.getString("msg_id"));
            String appType = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_APP_TYPE, Constant.app_type, false);
            String secretKey = SPUtil.getInstance(mContext).getString(Constant.SPKey.KEY_SECRET_KEY, Constant.secret_key, false);
            response.setApptype(appType);
            Constant.secret_key = secretKey;
            response.setSign(response.getSign());
            sendMessage(gson.toJson(response));
    }

    /**
     * 连接打印机
     */
    public void connectPrint() {
        if (mContext == null || isPrinting) {
            LogUtil.d(TAG, "mContext == null");
            return;
        }
        isStartByMe = true;
        PrinterConnection.getInstance().connect();
    }

    /**
     * 断开打印机
     */
    public void disConnectPrint() {
        if (mContext == null) {
            LogUtil.d(TAG, "mContext == null");
            return;
        }
        PrinterConnection.getInstance().disConnect();
    }

    /**
     * 开始打印
     */
    public void startPrintMsg() {
        LogUtil.d(TAG, "isPrinting = " + isPrinting + "  isStartByMe = " + isStartByMe);
        if (isPrinting || !isStartByMe) {
            return;
        }
        AsyncTaskExecutors.executeTask(new PrintRunnable());
    }

    /**
     * 打印队列里面的数据
     */
    public void printMsg() throws RemoteException, InterruptedException {
        if (!checkEnvironment())
            return;
        // TODO
        // 取出队列头部数据，不移除
        String text = blockingQueue.peek();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        boolean success = PrinterConnection.getInstance().printText(text);
        if (!success) {
            LogUtil.d(TAG, "打印失败-------> " + text);
            return;
        }
        blockingQueue.poll();
        LogUtil.e(TAG, "打印成功-------> " + text + "\n" + Thread.currentThread().getName());
        EscCommand esc = new EscCommand();
        esc.addText("\n\n\n\n");   //  换行
        esc.addCutPaper();
        Vector<Byte> data = esc.getCommand(); //发送数据
        Byte[] Bytes = data.toArray(new Byte[data.size()]);
        byte[] bytes = Utils.toPrimitive(Bytes);
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        PrinterConnection.getInstance().printText(str);
        if (blockingQueue.size() > 0 && isPrinting) {
            Thread.sleep(50);
            printMsg();
        }
    }

    /**
     * 打印机状态
     */
    public void getPrinterStatus() {
        PrinterConnection.getInstance().getPrinterStatus();
    }


    public void clearPrintQueue() {
        LogUtil.d(TAG, "clearPrintQueue");
        blockingQueue.clear();
    }

    /**
     * 打印线程
     */
    private final class PrintRunnable implements Runnable {

        @Override
        public void run() {
            try {
                isPrinting = true;
                printMsg();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isPrinting = false;
                isStartByMe = false;
                disConnectPrint();
            }
        }
    }

    /**
     * 连接打印机失败
     */
    private void connPrintFail() {
//        if (isStartByMe) {
//            // TODO
//        }
        LogUtil.w(TAG, "打印机断开！");
        isStartByMe = false;
    }

    /**
     * 停止打印
     */
    public void stopPrintMsg() {
        LogUtil.d(TAG, "stopPrintMsg");
        isPrinting = false;
    }

    public void startPrintThread() {
        if (mContext == null || printThread == null || !printThread.isAlive()) {
            printThread = new PrintThread();
            printThread.connect();
        }
    }

    public void stopPrintThread() {
        if (printThread != null) {
            printThread.disConnect();
        }
    }

    private boolean checkEnvironment() {
        return mContext != null;
    }

}
