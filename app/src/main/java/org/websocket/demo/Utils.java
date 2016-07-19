package org.websocket.demo;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * Created by chenfeiyue on 16/7/18.
 */
public class Utils {
    /**
     * 编码后进行验证的字符串
     */
    public static String getSignValue(List<Map.Entry<String, Object>> list) {
//        ArrayList<Map.Entry<String, Object>> l = new ArrayList<Map.Entry<String, Object>>(map.entrySet());

//        Collections.sort(l, new Comparator<Map.Entry<String, Object>>() {
//
//            @Override
//            public int compare(Map.Entry<String, Object> lhs, Map.Entry<String, Object> rhs) {
//                return lhs.getKey().compareToIgnoreCase(rhs.getKey());
//            }
//        });


        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : list) {
            sb.append(e.getKey());
            sb.append("=");
            sb.append(e.getValue().toString());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return Utils.toMd5(sb.toString());
    }


    public static String toMd5(String data) {
        MessageDigest md5;
        byte[] m = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(data.getBytes());
            m = md5.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }

        return byteArray2Hex(m);
    }

    static final String HEXES = "0123456789ABCDEF";

    public static String byteArray2Hex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString().toLowerCase();
    }
}
