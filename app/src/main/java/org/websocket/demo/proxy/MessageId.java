package org.websocket.demo.proxy;

/**
 * Created by chenfeiyue on 16/7/20.
 * MessageId pkg_type respcd
 */
public class MessageId {

    /**
     * 客户端发送心跳
     */
    public static final String PKG_HEART = "00";

    /**
     * 服务端响应心跳
     */
    public static final String PKG_HEART_ACK = "01";

    /**
     * 客户端设备检查
     */
    public static final String PKG_DEVICE_CHECK = "02";

    /**
     * 服务端响应设备检查
     */
    public static final String PKG_DEVICE_CHECK_ACK = "03";

    /**
     * 客户端设备绑定
     */
    public static final String PKG_DEVICE_BIND = "04";

    /**
     * 服务端响应设备绑定
     */
    public static final String PKG_DEVICE_BIND_ACK = "05";

    /**
     * 服务端推送消息
     */
    public static final String PKG_DEVICE_PUSH = "06";

    /**
     * 客户端推送消息回执
     */
    public static final String PKG_DEVICE_PUSH_ACK = "07";


    /**
     * 返回码
     */
    /**
     * 请求成功
     */
    public static final String ACK_OK = "0";

    /**
     * 设备禁止
     */
    public static final String ACK_DEVICE_FORBIDDEN = "-1";

    /**
     * 请求参数错误
     */
    public static final String ACK_PARAM_ERR = "-2";

    /**
     * 设备未绑定
     */
    public static final String ACK_TOKEN_ERR = "-3";

    /**
     * 签名错误
     */
    public static final String ACK_SIGN_ERR = "-4";
}
