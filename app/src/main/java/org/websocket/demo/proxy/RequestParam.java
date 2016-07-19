package org.websocket.demo.proxy;


/**
 * 类名称：RequestParam 作者： lining 类描述：http请求参数 修改时间：
 *
 */
public class RequestParam
{

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

    public RequestParam()
    {
    }

    /**
     * 方法名称：getUrl 作者：lining 方法描述：获取url 返回类型：@return the url 备注：
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * 方法名称：setUrl 作者：lining 方法描述：设置url
     *
     * @param url
     *            the url to set 返回类型：@return void 备注：
     */
    public void setUrl(String url)
    {
        this.url = url;
    }


    /**
     * 方法名称：getTimeHandler 作者：lining 方法描述：获取timeHandler 返回类型：@return the
     * timeHandler 备注：
     */
    public ITimerHandler getTimeHandler()
    {
        return timeHandler;
    }

    /**
     * 方法名称：setTimeHandler 作者：lining 方法描述：设置timeHandler
     *
     * @param timeHandler
     *            the timeHandler to set 返回类型：@return void 备注：
     */
    public void setTimeHandler(ITimerHandler timeHandler)
    {
        this.timeHandler = timeHandler;
    }

    /**
     * 方法名称：getRequestId 作者：lining 方法描述：获取requestId 返回类型：@return the requestId
     * 备注：
     */
    public short getMessageId()
    {
        return messageId;
    }

    /**
     * 方法名称：setRequestId 作者：lining 方法描述：设置requestId
     *
     * @param messageId
     *            the requestId to set 返回类型：@return void 备注：
     */
    public void setMessageId(short messageId)
    {
        this.messageId = messageId;
    }

    /**
     * 方法名称：getTimeout 作者：lining 方法描述： 输入参数：@return 返回类型：int： 备注：
     */
    public int getTimeout()
    {
        return timeout;
    }

    /**
     * 方法名称：setTimeout 作者：lining 方法描述： 输入参数：@param timeout 返回类型：void： 备注：
     */
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
}
