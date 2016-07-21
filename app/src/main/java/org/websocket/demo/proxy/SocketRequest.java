package org.websocket.demo.proxy;


public abstract class SocketRequest
{
    /**
     * 最大流水号
     */
    public static final short MAX_SEQUENCE = Short.MAX_VALUE;

    /**
     * ip
     */
    private String ip = "";

    /**
     * port
     */
    private String port = "";

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
     * 方法描述：获取ip 返回类型：@return the ip 备注：
     */
    public String getIp()
    {
        return ip;
    }

    /**
     * 方法描述：设置ip 输入参数： @param ip 返回类型： void 备注：
     */
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    /**
     * 方法描述：获取port 返回类型：@return the port 备注：
     */
    public String getPort()
    {
        return port;
    }

    /**
     * 方法描述：设置port 输入参数： @param port 返回类型： void 备注：
     */
    public void setPort(String port)
    {
        this.port = port;
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
