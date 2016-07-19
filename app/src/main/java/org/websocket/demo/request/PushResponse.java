package org.websocket.demo.request;

import android.os.SystemClock;
import android.util.Log;

import org.websocket.demo.Utils;

/**
 * Created by chenfeiyue on 16/7/18.
 */
public class PushResponse extends BaseRequest {
    /**
     * pkg_type : 07
     * apptype : 绑定的 apptype
     * sign : 签名
     * msgid : 消息 id
     * arrival_time : 收到消息时间, 10 位时间戳
     */

    private String pkg_type = "07";
    private String apptype = "602";
    private String sign;
    private String msgid;
    private String arrival_time = SystemClock.currentThreadTimeMillis() / 1000 + "";

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
        String signature = "apptype=" + apptype + "&" +
                "deviceid="+"352584060461735" + "&" +
                "pkg_id=" + pkg_id + "&" +
                "pkg_type=" + pkg_type + "&" +
                "platform=" + "android" + "&" +
                "platform_ver=" + android.os.Build.VERSION.RELEASE + "&" +
                "sdk=" + "pushcore" + "&" +
                "userid=" + "1217856" + "hBnZahNgjWEG7AAvHXes5oK1StGVj7yA";

        Log.e("HeartbeatRequest", "-----------> before signature " + signature);
        sign = Utils.toMd5(signature);
        Log.e("HeartbeatRequest", "-----------> after signature " + sign);
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
