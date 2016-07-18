package org.wlf.websocket.request;

import android.util.Log;

import org.wlf.websocket.Utils;

import java.util.Map;

/**
 * Created by chenfeiyue on 16/7/18.
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
    private String pkg_type="04";
    private String apptype = "602";
    private String sign;
    private String userid = "1217856";
    private String deviceid = "352584060461735";
    private String platform = "android";
    private String platform_ver = android.os.Build.VERSION.RELEASE;
    private String sdk = "pushcore";

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

        String signature = "apptype=" + apptype + "&" +
                "deviceid="+deviceid + "&" +
                "pkg_id=" + pkg_id + "&" +
                "pkg_type=" + pkg_type + "&" +
                "platform=" + platform + "&" +
                "platform_ver=" + platform_ver + "&" +
                "sdk=" + sdk + "&" +
                "userid=" + userid + "hBnZahNgjWEG7AAvHXes5oK1StGVj7yA";

        Log.e(TAG, "-----------> before signature " + signature);
        sign = Utils.toMd5(signature);
        Log.e(TAG, "-----------> after signature " + sign);

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
