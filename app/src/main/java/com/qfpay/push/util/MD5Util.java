package com.qfpay.push.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String toMd5(String data) {
        MessageDigest md5;
        byte[] m = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(data.getBytes());
            m = md5.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
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
        return hex.toString().toUpperCase();
    }
}
