package com.qfpay.pushsdk.request;

import android.text.TextUtils;

import com.google.gson.Gson;

public class TcpMessage
{
    private BaseRequest request;

    private Gson gson = new Gson();
    public TcpMessage() {
    }

    public BaseRequest getRequest() {
        return request;
    }

    public void setRequest(BaseRequest request) {
        this.request = request;
        setSequenceId(request.getPkg_id());
        setBody(gson.toJson(request));
    }

    /**
     * 消息id
     */
//    private short messageId = 0;

    /**
     * 消息体属性,是否分包
     */
    private boolean isMultiMessage = false;

    /**
     * 消息体属性,消息体长度
     */
    private int bodyLength = 0;

    /**
     * 消息流水号 对应BaseRequest.pkg_id
     */
    private String sequenceId = "";

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    private String answerSequenceId = "";

    private String body = "";

//    public short getMessageId()
//    {
//        return messageId;
//    }
//
//    public void setMessageId(short messageId)
//    {
//        this.messageId = messageId;
//    }

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

    public void setBodyLength(int bodyLength)
    {
        this.bodyLength = bodyLength;
    }

    public String getSequenceId()
    {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId)
    {
        this.sequenceId = sequenceId;
    }

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    public String getAnswerSequenceId()
    {
        return answerSequenceId;
    }

    /**
     * 应答流水号(只有当消息为响应消息的时候才会有)
     */
    public void setAnswerSequenceId(String answerSequenceId)
    {
        this.answerSequenceId = answerSequenceId;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        if (!TextUtils.isEmpty(body))
        {
            this.body = body;
            this.bodyLength = body.getBytes().length;
        }
    }
}
