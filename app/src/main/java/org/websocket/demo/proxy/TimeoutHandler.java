package org.websocket.demo.proxy;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;

import org.websocket.demo.util.LogUtil;
import org.websocket.demo.VtdService;
import org.websocket.demo.request.Constant;

public class TimeoutHandler implements ITimerHandler {
    @SuppressLint("UseSparseArrays")
    static HashMap<Short, String> requestMap = new HashMap<>();

    @Override
    public void timeoutHandle(short sequenceNumber, int status) {


        SocketRequest socketRequest = VtdService.getService()
                .getSocketRequest(sequenceNumber);

        if (null == socketRequest) {
            LogUtil.w("TimeoutHandler", "Request doesn't exist");
            return;
        }

        if(!(socketRequest instanceof Request)){
            LogUtil.w("TimeoutHandler", "!(socketRequest instanceof Request)");
            return;
        }

        Request request = (Request) socketRequest;
        if (status == Constant.REQUEST_TIMEOUT) {

            RequestParam param = request.getParam();
            if (null == param) {
                return;
            }
            String action = requestMap.get(param.getMessageId());

            request.setNeedResend(false);

            Http.sendRequest(request);

            if (action != null) {
                Intent intent = new Intent();
                intent.setAction(action);
                intent.putExtra(Constant.SERVICE_RESPONSE_RESULT, status);
                intent.putExtra(Constant.MESSAGE_BODY, request.getTcpMessage()
                        .getBody());
                VtdService.getService().sendBroadcast(intent);
            }
            LogUtil.d("TimeoutHandler",
                    "requestID = " + param.getMessageId() + ",action="
                            + action);
        } else if (status == Constant.REQUEST_SHUTDOWN) {
            // TODO
        }

    }
}
