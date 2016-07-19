package org.websocket.demo.proxy;

public class TcpMessage
{

    /**
     * 标志位
     */
    public static final byte MESSAGE_TAG = 0x7e;

    /**
     * 消息id
     */
    private short messageId = 0;

    /**
     * 消息体属性,是否分包
     */
    private boolean isMultiMessage = false;

    /**
     * 消息体属性,是否加密
     */
    private boolean isCrypt = false;

    /**
     * 消息体属性,消息体长度
     */
    private short bodyLength = 0;

    /**
     * 终端手机号
     */
    private String phoneNum = "";

    /**
     * 消息流水号
     */
    private short sequenceId = -1;

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    private short answerSequenceId = -1;

    /**
     * 消息包封装项内容,消息包总数
     */
    private short packageCount = 0;

    /**
     * 消息包封装项内容，包序号
     */
    private short packageNum = 1;

    private byte[] body = new byte[0];

    public short getMessageId()
    {
        return messageId;
    }

    public void setMessageId(short messageId)
    {
        this.messageId = messageId;
    }

    public boolean isMultiMessage()
    {
        return isMultiMessage;
    }

    public void setMultiMessage(boolean isMultiMessage)
    {
        this.isMultiMessage = isMultiMessage;
    }

    public boolean isCrypt()
    {
        return isCrypt;
    }

    public void setCrypt(boolean isCrypt)
    {
        this.isCrypt = isCrypt;
    }

    public int getBodyLength()
    {
        return bodyLength;
    }

    public void setBodyLength(short bodyLength)
    {
        this.bodyLength = bodyLength;
    }

    public short getSequenceId()
    {
        return sequenceId;
    }

    public void setSequenceId(short sequenceId)
    {
        this.sequenceId = sequenceId;
    }

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    public short getAnswerSequenceId()
    {
        return answerSequenceId;
    }

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    public void setAnswerSequenceId(short answerSequenceId)
    {
        this.answerSequenceId = answerSequenceId;
    }

    public short getPackageCount()
    {
        return packageCount;
    }

    public void setPackageCount(short packageCount)
    {
        this.packageCount = packageCount;
    }

    public short getPackageNum()
    {
        return packageNum;
    }

    public void setPackageNum(short packageNum)
    {
        this.packageNum = packageNum;
    }

    public byte[] getBody()
    {
        return body;
    }

    public void setBody(byte[] body)
    {
        if (body != null)
        {
            this.body = body;
            this.bodyLength = (short) body.length;
        }
    }
}
