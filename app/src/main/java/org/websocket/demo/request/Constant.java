package org.websocket.demo.request;

/**
 * Created by chenfeiyue on 16/7/18.
 */
public class Constant {

    public static short messageId = 1;

    public static String userid = "1217856";
    public static String deviceid = "352584060461735";
    public static String platform = "android";
    public static String platform_ver = android.os.Build.VERSION.RELEASE;
    public static String sdk = "pushcore";
//    public static String apptype = "602";
//    public static String secret_key = "hBnZahNgjWEG7AAvHXes5oK1StGVj7yA";
//    public static String URL = "ws://172.100.101.106:9997";


    public static String URL = "ws://push.qfpay.com";
    public static String apptype = "601";
    public static String secret_key = "123456";

    /**
     * 超时时间
     */
    public static final int DEFAULT_TIMEOUT = 30 * 1000;

    public static final int DEFAULT_RESEND_COUNT = 3;

    /**
     * 心跳间隔
     */
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 1000 * 30;

    /**
     * 请求成功
     */
    public static final int REQUEST_OK = 1;

    /**
     * 请求失败
     */
    public static final int REQUEST_FAIL = 0;
    /**
     * 请求超时
     */
    public static final int REQUEST_TIMEOUT = -1;

    /**
     * 请求错误 请求发送过程中连接被关闭
     */
    public static final int REQUEST_SHUTDOWN = -2;

    /**
     * MAA 服务器返回的 请求成功标志
     */
    public static final int REQUEST_SUCCESS = 0;


    /**
     * 请求 响应结果 true 成功 ,false 失败
     */
    public static final String SERVICE_RESPONSE_RESULT = "result";

    /**
     * 请求返回数据的 字符串
     */
    public static final String SERVICE_RESPONSE_DATA = "data";

    /**
     * 请求返回错误
     */
    public static final String SERVICE_ERROR_DATA = "error";

    /**
     * TcpMessage消息体
     */
    public static final String MESSAGE_BODY = "message_body";


    public static final class SPKey{
        public static final String KEY_HEARTBEAT = "heartbeat";
        public static final String KEY_MSG_TIMEOUT = "msg_timeout";

        public static final String KEY_RESEND_COUNT = "resend_count";
    }


}
