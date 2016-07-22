package org.websocket.demo.proxy;

/**
 * 类名称：Request 类描述：请求对象 修改时间：
 */
public final class Request {
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

    /**
     * 最大流水号
     */
    public static final short MAX_SEQUENCE = Short.MAX_VALUE;

    /**
     * TCP消息
     */
    private TcpMessage tcpMessage;

    /**
     * TCP请求超时器
     */
    private ISocketTimer socketTimer;

    /**
     * 是否需要服务器响应
     */
    private boolean isNeedRsp = true;

    /**
     * 是否是重发消息
     */
    private boolean isNeedResend = false;

    public boolean isNeedResend()
    {
        return isNeedResend;
    }

    public void setNeedResend(boolean isResend)
    {
        this.isNeedResend = isResend;
    }

    public boolean isNeedRsp()
    {
        return isNeedRsp;
    }

    public void setNeedRsp(boolean isNeedRsp)
    {
        this.isNeedRsp = isNeedRsp;
    }

    /**
     * 方法描述：获取sequenceNumber 返回类型：@return the
     * sequenceNumber 备注：
     */
    public short getSequenceNumber()
    {
        return tcpMessage.getSequenceId();
    }

    /**
     * 方法描述：设置sequenceNumber 输入参数： @param
     * sequenceNumber 返回类型： void 备注：
     */
    public void setSequenceNumber(short sequenceNumber)
    {
        tcpMessage.setSequenceId(sequenceNumber);
    }

    /**
     * 方法描述：获取messageType 返回类型：@return the
     * messageType 备注：
     */
    public int getMessageType()
    {
        return tcpMessage.getMessageId();
    }

    public TcpMessage getTcpMessage()
    {
        return tcpMessage;
    }

    public void setTcpMessage(TcpMessage tcpMessage)
    {
        this.tcpMessage = tcpMessage;
    }

    /**
     * 方法描述：获取socketTimer 返回类型：@return the
     * socketTimer 备注：
     */
    public ISocketTimer getSocketTimer()
    {
        return socketTimer;
    }

    /**
     * 方法描述：设置socketTimer 输入参数： @param socketTimer
     * 返回类型： void 备注：
     */
    public void setSocketTimer(ISocketTimer socketTimer)
    {
        this.socketTimer = socketTimer;
    }
}
