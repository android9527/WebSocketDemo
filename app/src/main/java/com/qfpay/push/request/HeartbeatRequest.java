package com.qfpay.push.request;

import com.qfpay.push.proxy.MessageId;
import com.qfpay.push.util.Constant;
import com.qfpay.push.util.Utils;

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
    private String apptype = Constant.app_type;
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

        String signature = "apptype=" + apptype + "&"
                + "pkg_id=" + pkg_id + "&"
                + "pkg_type=" + pkg_type
                + Constant.secret_key;

        sign = Utils.toMd5(signature);
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
