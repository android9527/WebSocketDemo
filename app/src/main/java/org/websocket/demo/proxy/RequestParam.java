package org.websocket.demo.proxy;

public class RequestParam {

    /**
     * 请求时间回调
     */
    private ITimerHandler timeHandler;

    /**
     * 设定超时时长
     */
    private int timeout = 30 * 1000;

    public RequestParam() {
    }

    public ITimerHandler getTimeHandler() {
        return timeHandler;
    }

    public void setTimeHandler(ITimerHandler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
