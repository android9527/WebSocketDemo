package org.websocket.demo.request;

import android.util.Log;

import org.websocket.demo.proxy.MessageId;
import org.websocket.demo.util.LogUtil;
import org.websocket.demo.util.Utils;

/**
 * Created by chenfeiyue on 16/7/18.
 * <p/>
 * 心跳数据包
 */
public class HeartbeatRequest extends BaseRequest {

    public HeartbeatRequest() {
        super();
    }

    /**
     * pkg_type : 00
     * pkg_id : 客户端数据包 id
     * apptype : 绑定的 apptype
     * sign : 签名
     */

    private String pkg_type = MessageId.PKG_HEART;
    private String apptype = Constant.apptype;
    private String sign = "";

    public String getPkg_type() {
        return pkg_type;
    }

    public void setPkg_type(String pkg_type) {
        this.pkg_type = pkg_type;
    }

    public String getApptype() {
        return apptype;
    }

    public void setApptype(String apptype) {
        this.apptype = apptype;
    }

    public String getSign() {

//        apptype=412&deviceid=123456&pkg_id=1&pkg_type=04&platform=android&platform_ver=6.0.1&sdk=pushcore&userid=123abcdef

        String signature = "apptype=" + apptype + "&"
                + "pkg_id=" + pkg_id + "&"
                + "pkg_type=" + pkg_type
                + Constant.secret_key;

        LogUtil.d("HeartbeatRequest", "before signature " + signature);
        sign = Utils.toMd5(signature);
        LogUtil.d("HeartbeatRequest", "after signature " + sign);

        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
