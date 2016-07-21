package org.websocket.demo.proxy;

public class RequestParam {

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求时间回调
     */
    private ITimerHandler timeHandler;

    /**
     * 请求Id
     */
    private short messageId;

    /**
     * 设定超时时长
     */
    private int timeout = 5;

    public RequestParam() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public ITimerHandler getTimeHandler() {
        return timeHandler;
    }

    public void setTimeHandler(ITimerHandler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public short getMessageId() {
        return messageId;
    }

    public void setMessageId(short messageId) {
        this.messageId = messageId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
