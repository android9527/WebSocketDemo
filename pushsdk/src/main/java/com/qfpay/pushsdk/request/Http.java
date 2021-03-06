package com.qfpay.pushsdk.request;

import com.qfpay.pushsdk.WebSocketService;
import com.qfpay.pushsdk.scheduletask.ScheduleTask;
import com.qfpay.pushsdk.scheduletask.ScheduleTaskService;
import com.qfpay.pushsdk.util.AsyncTaskExecutors;
import com.qfpay.pushsdk.util.Constant;
import com.qfpay.pushsdk.util.LogUtil;
import com.qfpay.pushsdk.util.SPUtil;

/**
 * HTTP连接工具类 修改时间：
 */
public class Http implements Runnable {

    private static final String TAG = "Http";

    /**
     * 当前http连接的请求对象
     */
    private SocketRequest currentRequest = null;

    /**
     * HeartBeatService引用
     */
    private static WebSocketService mService;

    /**
     * 设置HeartBeatService引用
     */
    public static void setMService(WebSocketService service) {
        mService = service;
    }

    private boolean sendMessageToServer() {
        boolean bRet = false;

        LogUtil.d(TAG, "begin send request to server");
        if (null == mService) {
            return false;
        }
        if (!mService.send(currentRequest)) {
            ITimerHandler timerHandler = currentRequest.getParam()
                    .getTimeHandler();
            if (null != timerHandler) {
                timerHandler.timeoutHandle(
                        currentRequest.getSequenceNumber(),
                        Constant.REQUEST_SHUTDOWN);
            }
        } else {
            if (currentRequest.isNeedResend()) {
                bRet = true;
            }
        }
        return bRet;
    }

    /**
     * 发送请求之前，先做检查
     */
    private static boolean check(SocketRequest request) {
        return request != null;
    }

    /**
     * 获得空闲的http连接线程
     */
    private static Http getFreeHttp() {
        return new Http();
    }

    /**
     * 发送request请求
     */
    public static synchronized Http sendRequest(SocketRequest request) {
        // 在发送请求之前，做一些检查工作，不通过则不发送请求
        if (!check(request)) {
            return null;
        }

        Http http = getFreeHttp();

        request.setHttp(http);
        // 设置http参数
        http.currentRequest = request;

        AsyncTaskExecutors.executeTask(http);

        return http;
    }

    public void cancel() {
        cancelRequest();
    }

    /**
     * 取消请求操作
     */
    private void cancelRequest() {
        stopReSend();
        if (null == mService) {
            return;
        }
        mService.delSocketRequest(currentRequest);
    }

    @Override
    public void run() {
        LogUtil.e(TAG, Thread.currentThread().getName());
        if (sendMessageToServer()) {
            ScheduleTaskService
                    .getInstance().getScheduleTaskManager()
                    .startSchedule(callback,
                            currentRequest.getParam().getTimeout());
        }
    }

    /**
     * 停止超时重发
     */
    private void stopReSend() {
        ScheduleTaskService
                .getInstance()
                .getScheduleTaskManager()
                .stopSchedule(callback);
    }

    ScheduleTask.Callback callback = new ScheduleTask.Callback() {

        @Override
        public long doSchedule() {
            LogUtil.d(TAG,
                    "Execute request timeout,requestID:"
                            + currentRequest.getSequenceNumber());
            int timeout = currentRequest.getParam().getTimeout();
            currentRequest.addSendNum();
            // 重发超过限定次数，不再保存请求对象
            if (currentRequest.getSendNum() >
                    SPUtil.getInstance(mService).getInt(Constant.SPKey.KEY_RESEND_COUNT, Constant.DEFAULT_RESEND_COUNT, false)) {
                cancelRequest();
                return 0;
            }
            ITimerHandler timerHandler = currentRequest.getParam()
                    .getTimeHandler();
            if (null != timerHandler) {

                timerHandler.timeoutHandle(currentRequest.getSequenceNumber(),
                        Constant.REQUEST_TIMEOUT);
            }

//            timeout = timeout * (currentRequest.getSendNum() + 1);
//            currentRequest.getParam().setTimeout(timeout);
            return timeout;
        }
    };
}
