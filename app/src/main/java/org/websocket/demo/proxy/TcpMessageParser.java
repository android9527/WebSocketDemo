package org.websocket.demo.proxy;

public class TcpMessageParser
{

    private static final String TAG = "TcpMessageParser";

    /**
     * 标志位
     */
    public static final byte MESSAGE_TAG = 0x7e;

    public static TcpMessage bytes2TcpMsg(byte[] bytes)
    {

        /**
         * 消息id
         */
        short messageId = 0;

        /**
         * 消息体属性,是否有分包
         */
        boolean hasMorePkg = false;

        /**
         * 消息体属性,消息体长度
         */
        short bodyLength = 0;

        /**
         * 消息流水号
         */
        short sequenceId = 0;

        byte[] body = new byte[0];

        TcpMessage msg = new TcpMessage();
        // 包前后是否都有标志位
        if (bytes[0] == MESSAGE_TAG && bytes[bytes.length - 1] == MESSAGE_TAG)
        {

            msg.setMessageId(messageId);
            msg.setMultiMessage(hasMorePkg);
            msg.setBodyLength(bodyLength);
            msg.setSequenceId(sequenceId);
            msg.setBody(body);
        }
        else
        {
            // 标志位错误
        }
        return msg;

    }

    public static byte[] TcpMsg2Bytes(TcpMessage msg)
    {
//        byte[] head = getHead(msg);
//        byte[] parsedBody = msg.getBody();
//        // 是否加密
//        if (msg.isCrypt())
//        {
//            parsedBody = TcpByteUtil.encryptData(msg.getBody());
//        }
//        // 消息头+消息体
//        byte[] headBody = TcpByteUtil.arraysJoin(head, parsedBody);
//        byte crc = TcpByteUtil.getCRC(headBody);
//
//        // 消息头+消息体转义
//        byte[] res = TcpByteUtil.escape(TcpByteUtil.arraysJoin(headBody,
//                new byte[] {crc }));
//
//        ByteBuffer result = TcpByteUtil.allocate(res.length + 2);
//        result.put(MESSAGE_TAG);
//        result.put(res);
//        result.put(MESSAGE_TAG);
//        return result.array();
        return null;
    }

}
