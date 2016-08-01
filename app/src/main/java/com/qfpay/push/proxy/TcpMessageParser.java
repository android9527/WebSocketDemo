package com.qfpay.push.proxy;

import org.json.JSONObject;
import com.qfpay.push.request.SocketRequest;
import com.qfpay.push.request.TcpMessage;

public class TcpMessageParser {

    private static final String TAG = "TcpMessageParser";

    public static TcpMessage string2TcpMsg(String response) {

        /**
         * 消息体属性,是否有分包
         */
//        boolean hasMorePkg = false;

        /**
         * 消息体属性,消息体长度
         */
        int bodyLength = 0;

        /**
         * 消息流水号
         */
        String sequenceId = "";
        TcpMessage msg = new TcpMessage();
        bodyLength = response.getBytes().length;
        msg.setBodyLength(bodyLength);
        msg.setBody(response);
        try {
            JSONObject object = new JSONObject(response);
            if (object.has("pkg_ack_id")) {
                sequenceId = object.getString("pkg_ack_id");
                msg.setSequenceId(sequenceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;

    }

    public static String socketRequest2Message(SocketRequest socketRequest) {
        String msg = "";
        if (null == socketRequest) {
            return msg;
        }
        msg = socketRequest.getTcpMessage().getBody();
        return msg;
    }

}
