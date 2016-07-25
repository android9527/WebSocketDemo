package org.websocket.demo.request;

import org.websocket.demo.proxy.MessageId;
import org.websocket.demo.util.Constant;
import org.websocket.demo.util.Utils;

/**
 * Created by chenfeiyue on 16/7/18.
 * BindRequest 绑定打印机请求数据体
 */
public class BindRequest extends BaseRequest {


    public BindRequest() {
        super();
    }

    /**
     * pkg_type : 04
     * pkg_id : 客户端数据包 id
     * apptype : 绑定的 apptype
     * sign : 签名
     * userid : 用户 id
     * deviceid : 设备唯一标示
     * platform : 平台类型, 如 android
     * platform_ver : 平台版本
     * sdk : 使用的推送 SDK, 如 pushcore
     */


    private static final String TAG = "BindRequest";
    private String pkg_type = MessageId.PKG_DEVICE_BIND;
    private String apptype = Constant.apptype;
    private String sign;
    private String userid = Constant.userid;
    private String deviceid = "";
    private String platform = Constant.platform;
    private String platform_ver = Constant.platform_ver;
    private String sdk = Constant.sdk;

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

        String signature = "apptype=" + apptype + "&" +
                "deviceid=" + deviceid + "&" +
                "pkg_id=" + pkg_id + "&" +
                "pkg_type=" + pkg_type + "&" +
                "platform=" + platform + "&" +
                "platform_ver=" + platform_ver + "&" +
                "sdk=" + sdk + "&" +
                "userid=" + userid + Constant.secret_key;

        sign = Utils.toMd5(signature);
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform_ver() {
        return platform_ver;
    }

    public void setPlatform_ver(String platform_ver) {
        this.platform_ver = platform_ver;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    @Override
    public String toString() {
        return "BindRequest{" +
                "pkg_type='" + pkg_type + '\'' +
                ", pkg_id=" + pkg_id +
                ", apptype='" + apptype + '\'' +
                ", sign='" + sign + '\'' +
                ", userid='" + userid + '\'' +
                ", deviceid='" + deviceid + '\'' +
                ", platform='" + platform + '\'' +
                ", platform_ver='" + platform_ver + '\'' +
                ", sdk='" + sdk + '\'' +
                '}';
    }
}
