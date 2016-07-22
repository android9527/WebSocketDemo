package org.websocket.demo.proxy;

import org.websocket.demo.request.BaseRequest;

public class TcpMessage
{
    private BaseRequest request;

    public TcpMessage() {
    }

    public BaseRequest getRequest() {
        return request;
    }

    public void setRequest(BaseRequest request) {
        this.request = request;
        setSequenceId(Short.parseShort(request.getPkg_id()));
    }

    /**
     * 消息id
     */
    private short messageId = 0;

    /**
     * 消息体属性,是否分包
     */
    private boolean isMultiMessage = false;

    /**
     * 消息体属性,消息体长度
     */
    private short bodyLength = 0;

    /**
     * 消息流水号
     */
    private short sequenceId = -1;

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    private short answerSequenceId = -1;

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
