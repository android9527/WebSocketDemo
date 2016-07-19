/**
 * 
 */
package org.websocket.demo.proxy;




/**
 * ����ƣ�TimerHandler
 * ���ߣ� lining
 * ��������
 * �޸�ʱ�䣺
 * 
 */
public interface ITimerHandler
{

    /**
     * ��ʱ����ص����������������ʵ��
     * <������ϸ����>
     * 
     * @param controller
     *            ���Ƽ�ʱ����Ķ���
     * @see [�ࡢ��#��������#��Ա]
     */
    public void timeoutHandle(short sequenceNumber, int status);

}
