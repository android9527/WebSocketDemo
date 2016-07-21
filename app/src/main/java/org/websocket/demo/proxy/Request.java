package org.websocket.demo.proxy;

/**
 * 类名称：Request 类描述：请求对象 修改时间：
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
     */
    public final RequestParam getParam() {
        return param;
    }

    /**
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

    public boolean isReconnect() {
        return isReconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.isReconnect = reconnect;
    }

    public static String getConnectType() {
        return TYPE_TCP;
    }
}
