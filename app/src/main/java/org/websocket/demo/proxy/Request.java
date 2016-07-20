package org.websocket.demo.proxy;

import android.util.Log;

import org.websocket.demo.proxy.SocketRequest;

/**
 * 类名称：Request 作者： lining 类描述：请求对象 修改时间：
 */
public final class Request extends SocketRequest {

    /**
     * HTTP
     */
    public static final String TYPE_HTTP = "0";

    /**
     * TCP
     */
    public static final String TYPE_TCP = "1";

    /**
     * 发送次数
     */
    private int sendNum = 0;

    /**
     * 全局流水号
     */
    private static short sequence = 0;

    /**
     * 获取当前的sequence
     *
     * @return
     */
    public static short getSequence() {
        return sequence;
    }

    /**
     * 在网络异常后，是否自动重连
     */
    private boolean isReconnect = true;

    /**
     * 请求参数
     */
    private RequestParam param = null;

    /**
     * 请求对象
     */
    private Http httpObject = null;

    public Http getHttp() {
        return httpObject;
    }

    public void setHttp(Http httpObject) {
        this.httpObject = httpObject;
    }

    /**
     * 作者：lining 方法描述：构造方法 输入参数：@param actionType 输入参数：@param data 输入参数：@param
     * handler 输入参数：@param showCancel 输入参数：@param showTimeoutTip 输入参数：@param
     * timerHandler 输入参数：@param useWait 备注：
     */
    public Request(RequestParam param, TcpMessage msg) {
        this.param = param;
        setTcpMessage(msg);
        if (sequence++ > MAX_SEQUENCE) {
            sequence = 0;
        }
        setSequenceNumber(sequence);
    }

    /**
     * 方法名称：getParam 作者：lining 方法描述：获取param 返回类型：@return the param 备注：
     */
    public final RequestParam getParam() {
        return param;
    }

    /**
     * 方法名称：setParam 作者：lining 方法描述：设置param 输入参数： @param param 返回类型： void 备注：
     */
    public final void setParam(RequestParam param) {
        this.param = param;
    }

    public int getSendNum() {
        return sendNum;
    }

    public void setSendNum(int sendNum) {
        this.sendNum = sendNum;
    }

    public void addSendNum() {
        this.sendNum++;
    }

    /**
     * 方法名称：isReconnect 作者：lining 方法描述： 输入参数：@return 返回类型：boolean： 备注：
     */
    public boolean isReconnect() {
        return isReconnect;
    }

    /**
     * 方法名称：setReconnect 作者：lining 方法描述：设置重连标志 输入参数：@param reconnect 返回类型：void：
     * 备注：
     */
    public void setReconnect(boolean reconnect) {
        this.isReconnect = reconnect;
    }

    /**
     * 方法名称：getConnectType 作者：lining 方法描述：获取connectType 返回类型：@return the
     * connectType 备注：
     */
    public static String getConnectType() {
        return TYPE_TCP;
    }
}
