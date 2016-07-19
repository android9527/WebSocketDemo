package org.websocket.demo.proxy;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;
import org.websocket.demo.request.Constant;
import org.websocket.demo.scheduletask.ScheduleTask;
import org.websocket.demo.scheduletask.ScheduleTaskService;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class ServiceProxy implements ScheduleTask.Callback
{
    private TimeoutHandler timeoutHandler = new TimeoutHandler();

    private static final String TAG = "serviceProxy";

    private Context mContext;

    /**
     * 网络状态监听器
     */
    private NetworkConnectivityListener mNetworkConnectivityListener;


    /**
     * 网络切换事件
     */
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;

    ServiceHandler mServiceHandler;

    /**
     * 消息请求队列
     */
    private static Hashtable<Short, SocketRequest> reqQueue = new Hashtable<>();

    private ServiceProxy(Context context)
    {
        mContext = context;
        initListener(mContext);
    }

    private void initListener(Context context) {
        mServiceHandler = new ServiceHandler();
        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.registerHandler(mServiceHandler,
                EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(context);
    }

    private static ServiceProxy serviceProxy;

    public static synchronized ServiceProxy getInstance(Context context)
    {
        if (serviceProxy == null)
        {
            serviceProxy = new ServiceProxy(context);
        }
        return serviceProxy;
    }

    /**
     * 是否已连接到Server
     *
     * @return 是否连接
     */
    public boolean isConnected()
    {
//        return VtdService.getService().isConnected();
        // TODO
        return true;
    }


    public void login()
    {
    }





    /**
     * 类名称：ServiceHandler 作者： lining 类描述：监听网络连接回调Handler 修改时间：
     *
     */
    private final class ServiceHandler extends Handler
    {
        public ServiceHandler()
        {
        }

        @Override
        public void handleMessage(Message msg)
        {
                LogUtil.d(TAG, "handleMessage");

            switch (msg.what)
            {
                case EVENT_NETWORK_STATE_CHANGED:
                    networkStateChanged();
                    break;
                default:
            }
        }
    }

    /**
     * 方法名称：networkStateChanged 方法描述：网络状态改变时回被调用 输入参数： 返回类型：void： 备注：
     */
    private void networkStateChanged()
    {
        if (mNetworkConnectivityListener == null)
        {
            LogUtil.d(TAG, "networkStateChanged: return");

            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        NetworkInfo.State state = networkInfo.getState();

        // Notify the connection that network type has changed. Note that this
        // only work for connected connections, we need to reestablish if it's
        // suspended.
        switch (state)
        {
            case CONNECTED:
            {
                try
                {
                    if (!isConnected())
                    {
                        // TODO
//                        connect();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            }
            case SUSPENDED:
            case DISCONNECTED:
            {
                LogUtil.d(TAG, "DISCONNECTED");
//                close();
                
                // ReconnectNotify(false);
                    connectedNotify(false);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 当连接到服务器会回调该方法
     */
//    @Override
    public void connectedNotify(boolean status) {
        LogUtil.d(TAG, "connectedNotify:" + "ConnectStatus:" + status);

        if (!status) {

            stopHeartBeat();

            cancelAllRequest();

            return;
        }

//        login();
        //TODO

        // bind();
    }

    /**
     * 发送无响应请求
     */
    public Http sendRequest(TcpMessage msg, boolean isNeedResponse,
                            boolean isNeedResend)
    {
        if (!couldSend(msg.getMessageId()))
        {
            return null;
        }

        Http http = null;
        try
        {
            RequestParam param = new RequestParam();
            param.setTimeout(SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_MSG_TIMEOUT, Constant.DEFAULT_TIMEOUT));
            param.setMessageId(msg.getMessageId());
            param.setTimeHandler(timeoutHandler);
            Request request = new Request(param, msg);
            request.setNeedRsp(isNeedResponse);
            request.setNeedResend(isNeedResend);
            http = Http.sendRequest(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            http = null;
        }

        return http;
    }

    /**
     * 发送分包的请求(指定sequence)
     */
    public Http sendMultiMsgRequest(TcpMessage tcpMessage, boolean isResponse,
                                    boolean isNeedResend, short sequence)
    {
        if (!couldSend(tcpMessage.getMessageId()))
        {
            return null;
        }
        Http http = null;
        try
        {
            RequestParam param = new RequestParam();
            param.setTimeout(SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_MSG_TIMEOUT, Constant.DEFAULT_TIMEOUT));
            param.setMessageId(tcpMessage.getMessageId());
            param.setTimeHandler(timeoutHandler);
            Request request = new Request(param, tcpMessage);
            request.setNeedRsp(isResponse);
            request.setNeedResend(isNeedResend);
            request.setSequenceNumber(sequence);
            http = Http.sendRequest(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            http = null;
        }

        return http;
    }

    /**
     * 发送多媒体事件信息
     *
     * @return sequenceId
     */
    public short sendMultiMediaRequest(TcpMessage tcpMessage)
    {
        short sequenceId = -1;
        if (!couldSend(tcpMessage.getMessageId()))
        {
            return sequenceId;
        }
        try
        {
            RequestParam param = new RequestParam();
            param.setTimeout(SPUtil.getInstance(mContext).getInt(Constant.SPKey.KEY_MSG_TIMEOUT, Constant.DEFAULT_TIMEOUT));
            param.setMessageId(tcpMessage.getMessageId());
            param.setTimeHandler(timeoutHandler);
            Request request = new Request(param, tcpMessage);
            request.setNeedRsp(true);
            request.setNeedResend(true);
            sequenceId = request.getSequenceNumber();
            Http.sendRequest(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sequenceId = -1;
        }

        return sequenceId;
    }

    /**
     * 是否可以发送消息
     *
     * @return 登录标识
     */
    private boolean couldSend(short msgId)
    {
        return false;
//        return Constant.isLogin || MessageId.AUTHEN == msgId
//                || MessageId.REGIST == msgId;
    }

    /**
     * 发送通用应答
     *
     * @param answerId
     *            对应的平台消息的ID
     * @param sequenceId
     *            对应的平台消息的流水号
     * @param result
     *            结果 CommonAnswerBody.SUCCESS, CommonAnswerBody.FAILED,
     *            CommonAnswerBody.ERROR, CommonAnswerBody.UNSUPPORT
     *            CommonAnswerBody.ALARM_CONFIRM
     */
    public Http sendCommonAnswer(int answerId, int sequenceId, byte result)
    {
        TcpMessage msg = new TcpMessage();
//        msg.setMessageId((short) MessageId.CLIENT_COMMON_ANSWER);
//
//        CommonAnswerBody body = new CommonAnswerBody();
//        body.setAnswerSequenceId((short) sequenceId);
//        body.setAnswerId((short) answerId);
//        body.setResult(result);
//
//        msg.setBody(body.getBytes());

        return sendRequest(msg, false, true);
    }


    /**
     * 方法名称：processRsp 作者：lining 方法描述：处理响应消息 输入参数：@param socketMessage
     * 返回类型：void： 备注：
     */
    private void processRsp(TcpMessage tcpMessage)
    {
        LogUtil.d(TAG, "processRsp enter");
        Request request = (Request) getSocketRequest(tcpMessage.getAnswerSequenceId());

        if (null == request)
        {
            LogUtil.d(TAG, "processRsp request == null");
            return;
        }
        request.getHttp().cancel();

        serviceProxy.doResponse(tcpMessage);

    }

    /**
     * 方法名称：getSocketRequest 作者：lining 方法描述：获取包含指定消息的请求 输入参数：@param
     * socketMessage 输入参数：@return 返回类型：SocketRequest： 备注：
     */
    public SocketRequest getSocketRequest(short sequenceId)
    {
        if (isEmptyReqQueue() || 0 == sequenceId)
        {
            return null;
        }
        return reqQueue.get(sequenceId);
    }

    /**
     * 请求服务器返回消息响应
     */
    public void doResponse(final TcpMessage msg)
    {
        if (msg == null)
        {
            return;
        }

        // 消息体
        byte[] body = msg.getBody();

        switch ((int) msg.getMessageId())
        {
//            case MessageId.SERVER_COMMON_ANSWER:
//                commonAnswer(msg);
//                break;
//
//            case MessageId.HEARTBEAT:
//                break;
//            case MessageId.AUTHEN:
//                if (result == CommonAnswerBody.SUCCESS)
//                {
//                    Log.d(TAG, "authentic SUCCESS ");
//                    // 鉴权成功，开始心跳
//                    VtdService.getService().startHeartBeat();
//                    Constant.isLogin = true;
//                    // 鉴权成功后才能发送消息
//                    startReport();
//
//                    // 发送断网期间保存的消息
//                    sendSavedMessages();
//                }
//                else
//                {
//                    // 未成功
//                    Log.e(TAG, "authentic failed , result = " + result);
//                }
//                break;
            default:
                break;
        }




    }

    /**
     * 处理心跳和推送
     */
    public void onPushMessage(TcpMessage msg)
    {
        if (msg == null)
        {
            LogUtil.d(TAG, "processPush return");
            return;
        }

        switch ((int) msg.getMessageId())
        {
            default:
                break;
        }
    }

    private void commonAnswer(TcpMessage msg)
    {

//        int result = TcpByteUtil.getByteValue(cab.getResult());
//        switch (cab.getAnswerId())
//        {
//            case MessageId.HEARTBEAT:
//                break;
//            case MessageId.AUTHEN:
//                if (result == CommonAnswerBody.SUCCESS)
//                {
//                    Log.d(TAG, "authentic SUCCESS ");
//                    // 鉴权成功，开始心跳
//                    startHeartBeat();
//                }
//                else
//                {
//                    // 鉴权未成功
//                    Log.e(TAG, "authentic failed , result = " + result);
//                }
//                break;
//            default:
//                break;
//        }

    }


    /**
     * 方法名称：startHeartBeat 方法描述：启动心跳 输入参数： 返回类型：void： 备注：
     */
    public void startHeartBeat()
    {
//        if (!stop)
//        {
//            LogUtil.log(TAG, "heartBeat stop is false");
//            return;
//        }

//        init();
        // 启动心跳
        ScheduleTaskService.getInstance()
                .getScheduleTaskManager()
                .startSchedule(this, getHeartBeatInterval());
    }
//    /**
//     */
//    private void init()
//    {
//        heartbeatInterval = getHeartBeatInterval();
//        heartMsg = new TcpMessage();
//        heartMsg.setMessageId((short) MessageId.HEARTBEAT);
//        heartMsg.setPhoneNum(PhoneUtils.getInstance().getPhoneNum(this));
//    }

    private long getHeartBeatInterval()
    {
        return SPUtil.getInstance(mContext).getLong(Constant.SPKey.KEY_HEARTBEAT, 1000 * 60);
    }
    public void stopHeartBeat()
    {
        try
        {
            ScheduleTaskService.getInstance()
                    .getScheduleTaskManager()
                    .stopSchedule(this);
            LogUtil.d(TAG, "Start Stop HeartBeat...leave");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 设置心跳时间间隔
     */
    public void setHeartbeatInterval(long value)
    {
        SPUtil.getInstance(mContext).save(Constant.SPKey.KEY_HEARTBEAT, value);
    }


    /**
     * 方法名称：sendHeartBeatRequest 作者：lining 方法描述：发送心跳请求 输入参数： 返回类型：void： 备注：
     */
    public void sendHeartBeatRequest()
    {
        LogUtil.d(TAG, "------->sendHeartBeatRequest");
        try
        {
//            // 构造心跳请求
//            RequestParam param = new RequestParam();
//            param.setMessageId(heartMsg.getMessageId());
//            param.setTimeHandler(this);
//            Request request = new Request(param, heartMsg);
//            // 设置不需要重连
//            request.setReconnect(false);
//            // 发送心跳请求
//            Http.sendRequest(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 取消所以请求
     */
    public void cancelAllRequest()
    {
        if (this.isEmptyReqQueue())
        {
            LogUtil.d(TAG, "cancelAllRequest return");
            return;
        }
        Enumeration<SocketRequest> elements = reqQueue.elements();
        for (Enumeration<SocketRequest> element = elements; elements.hasMoreElements();)
        {
            Request request = (Request) element.nextElement();

            if (null != request)
            {
                Http http = request.getHttp();

                if (http != null)
                {
                    http.cancel();
                }
            }
        }
        delAllSocketRequest();
    }
    /**
     * 方法名称：delAllSocketRequest 作者：lining 方法描述：删除所有请求 输入参数: 返回类型：void 备注：
     */
    private void delAllSocketRequest()
    {
        if (isEmptyReqQueue())
        {
            return;
        }

        reqQueue.clear();
    }

    private boolean isEmptyReqQueue()
    {
        return null == reqQueue || reqQueue.isEmpty();
    }

    /**
     * 方法名称：stopService 方法描述：停止服务 输入参数： 返回类型：void： 备注：
     */
    public void stopService()
    {
        clearAll();
//        VtdService.getService().stopService();
    }

    /**
     * 方法名称：clearAll 作者：lining 方法描述：清除内存数据 输入参数: 返回类型：void 备注：
     */
    private void clearAll()
    {
        // 清除缓存数据

        cancelAllRequest();
        ScheduleTaskService.getInstance().shutdown();
//        stopHeartBeat();

        if (null != mNetworkConnectivityListener)
        {
            LogUtil.d(TAG, "unregisterHandler");
            mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
            mNetworkConnectivityListener.stopListening();
            mNetworkConnectivityListener = null;
        }
        // TODO
//        close();

    }

    @Override
    public long doSchedule() {

//        if (tcpChannel.isShutdown())
//        {
//            return 0;
//        }


        if(!isConnected()){
            return 0;
        }

        sendHeartBeatRequest();

        return getHeartBeatInterval();
    }

    /**
     * 方法名称：delSocketRequest 作者：lining 方法描述：从请求队列中删除一个请求 输入参数:@param
     * socketRequest 返回类型：void 备注：
     */
    public void delSocketRequest(SocketRequest socketRequest)
    {
        if (null == socketRequest)
        {
            return;
        }
        delSocketRequest(socketRequest.getSequenceNumber());
    }

    /**
     * 方法名称：delSocketRequest 作者：lining 方法描述：删除一个流水号的请求 输入参数：@param
     * sequenceNumber 返回类型：void： 备注：
     */
    private void delSocketRequest(short sequenceNumber)
    {
        if (isEmptyReqQueue() || sequenceNumber < 0)
        {
            return;
        }
        reqQueue.remove(sequenceNumber);
    }


    /**
     * 方法名称：send 作者：lining 方法描述：发送消息 输入参数：@param socketRequest 返回类型：void： 备注：
     */
    public boolean send(SocketRequest socketRequest)
    {
        boolean result = false;

        if (!isConnected())
        {
            return result;
        }
        try
        {
            sendMessage(socketRequest);

            if (socketRequest.isNeedRsp())
            {
                addSocketRequest(socketRequest);
                result = true;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        socketRequest = null;
        return result;
    }

    /**
     * 方法名称：addSocketRequest 作者：lining 方法描述：加入请求到队列中 输入参数：@param socketRequest
     * 返回类型：void： 备注：
     */
    private void addSocketRequest(SocketRequest socketRequest)
    {
        if (null == socketRequest || null == reqQueue)
        {
            return;
        }
        reqQueue.put(socketRequest.getSequenceNumber(), socketRequest);
    }
    /**
     * 方法名称：sendMessage 作者：lining 方法描述：发送消息 输入参数：@param message 输入参数：@throws
     * IOException 返回类型：void： 备注：
     */
    private void sendMessage(SocketRequest request) throws IOException
    {
        if (null == request)
        {
            throw new IOException();
        }

        // TODO
//        this.tcpChannel.sendData(TcpMessageParser.TcpMsg2Bytes(request.getTcpMessage()));
    }

//    @Override
//    public void onSuccess(int msgId)
//    {
//        switch (msgId)
//        {
//            case MessageId.UPLOAD_MASS_LOCATIONS:
//                // 盲区补传成功后，清除保存的位置信息
//                LocationReportService.getInstance().clearUnreportedLocs();
//                break;
//            default:
//                break;
//        }
//
//    }
//
//    @Override
//    public void onFailed(int msgId)
//    {
//        switch (msgId)
//        {
//            case MessageId.UPLOAD_MASS_LOCATIONS:
//                break;
//            default:
//                break;
//        }
//
//    }
}
