package org.websocket.demo.request;

import android.os.SystemClock;
import android.util.Log;

import org.websocket.demo.proxy.MessageId;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.Utils;

/**
 * Created by chenfeiyue on 16/7/18.
 * 收到推送回复
 */
public class PushResponse extends BaseRequest {
    /**
     * pkg_type : 07
     * apptype : 绑定的 apptype
     * sign : 签名
     * msgid : 消息 id
     * arrival_time : 收到消息时间, 10 位时间戳
     */

    private String pkg_type = MessageId.PKG_DEVICE_PUSH_ACK;
    private String apptype = Constant.apptype;
    private String sign;
    private String msgid;
    private String arrival_time = System.currentTimeMillis() / 1000 + "";

    public PushResponse() {
        super();
    }

    public String getApptype() {
        return apptype;
    }

    public void setApptype(String apptype) {
        this.apptype = apptype;
    }

    public String getSign() {
        String signature = "apptype=" + apptype + "&"
                + "arrival_time=" + arrival_time + "&"
                + "msgid=" + msgid + "&"
                + "pkg_id=" + pkg_id + "&"
                + "pkg_type=" + pkg_type
                + Constant.secret_key;

        sign = Utils.toMd5(signature);
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
