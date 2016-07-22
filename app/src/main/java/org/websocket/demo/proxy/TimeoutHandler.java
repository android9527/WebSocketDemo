package org.websocket.demo.proxy;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;

import org.websocket.demo.util.LogUtil;
import org.websocket.demo.WebSocketService;
import org.websocket.demo.request.Constant;

public class TimeoutHandler implements ITimerHandler {
    @SuppressLint("UseSparseArrays")
    static HashMap<Short, String> requestMap = new HashMap<>();

    @Override
    public void timeoutHandle(String sequenceNumber, int status) {

        SocketRequest request = WebSocketService.getService()
                .getSocketRequest(sequenceNumber);

        if (null == request) {
            LogUtil.w("TimeoutHandler", "Request doesn't exist");
            return;
        }

        if (status == Constant.REQUEST_TIMEOUT) {
            RequestParam param = request.getParam();
            if (null == param) {
                return;
            }
            request.setNeedResend(false);
            Http.sendRequest(request);

//            String action = requestMap.get(param.getMessageId());
//            if (action != null) {
//                Intent intent = new Intent();
//                intent.setAction(action);
//                intent.putExtra(Constant.SERVICE_RESPONSE_RESULT, status);
//                intent.putExtra(Constant.MESSAGE_BODY, request.getTcpMessage()
//                        .getBody());
//                WebSocketService.getService().sendBroadcast(intent);
//            }
//            LogUtil.d("TimeoutHandler",
//                    "requestID = " + param.getMessageId() + ",action="
//                            + action);
        } else if (status == Constant.REQUEST_SHUTDOWN) {
            // TODO
        }

    }
}
