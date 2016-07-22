package org.websocket.demo.proxy;

import android.util.Log;

import org.websocket.demo.AsyncTaskExecutors;
import org.websocket.demo.WebSocketService;
import org.websocket.demo.request.Constant;
import org.websocket.demo.scheduletask.ScheduleTask;
import org.websocket.demo.scheduletask.ScheduleTaskService;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.SPUtil;

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

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            LogUtil.d(TAG, "begin send request to server");
        }

        if (null != mService) {
            LogUtil.d(TAG, "sendMesageToService:requestId = "
                    + currentRequest.getSequenceNumber());

            if (!mService.send(currentRequest)) {
                ITimerHandler timerHandler = currentRequest.getParam()
                        .getTimeHandler();
                if (null != timerHandler) {
                    timerHandler.timeoutHandle(
                            currentRequest.getSequenceNumber(),
                            Constant.REQUEST_SHUTDOWN);
                }

                LogUtil.d(TAG,
                        "sendMesageToService Error, requestId:"
                                + currentRequest.getSequenceNumber());
            } else {
                if (currentRequest.isNeedResend()) {
                    bRet = true;
                }
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
        stopReSend();
        cancelRequest();
    }

    /**
     * 取消请求操作
     */
    private void cancelRequest() {
        if (null != mService) {
            try {
                mService.delSocketRequest(currentRequest);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("http:cancelRequest exception", e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        if (sendMessageToServer()) {
            ScheduleTaskService
                    .getInstance()
                    .getScheduleTaskManager()
                    .startSchedule(callback,
                            currentRequest.getParam().getTimeout());
        }
    }

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
                    SPUtil.getInstance(mService).getInt(Constant.SPKey.KEY_RESEND_COUNT, Constant.DEFAULT_RESEND_COUNT)) {
                cancelRequest();
                return 0;
            }
            ITimerHandler timerHandler = currentRequest.getParam()
                    .getTimeHandler();
            if (null != timerHandler) {
                LogUtil.d(TAG,
                        "Execute request timeout,requestID:"
                                + currentRequest.getSequenceNumber());

                timerHandler.timeoutHandle(currentRequest.getSequenceNumber(),
                        Constant.REQUEST_TIMEOUT);
            }

//            timeout = timeout * (currentRequest.getSendNum() + 1);
//            currentRequest.getParam().setTimeout(timeout);
            return timeout;
        }
    };
}
