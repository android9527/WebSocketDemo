package com.qfpay.push.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {
    private static SPUtil spUtil;
    private final static int MODE = Context.MODE_WORLD_READABLE
            | Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS;
    private static SharedPreferences sp, remoteSp;

    public static SPUtil getInstance(Context context) {
        if (spUtil == null) {
            spUtil = new SPUtil();
            sp = context.getSharedPreferences("shared", MODE);
        }

        if (remoteSp == null) {
            try {
                LogUtil.w("SPUtil", "package name = " + context.getPackageName());
                Context remoteContext = context.createPackageContext(context.getPackageName(),
                        Context.CONTEXT_IGNORE_SECURITY);
                remoteSp = remoteContext.getSharedPreferences("shared",
                        MODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return spUtil;
    }

    public void clearAll() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }


    public void save(String key, String value) {
        save(key, value, true);
    }

    public void save(String key, String value, boolean isPrivate) {
        SharedPreferences.Editor editor = isPrivate ? sp.edit() : remoteSp
                .edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void save(String key, long value) {
        save(key, value, true);
    }

    public void save(String key, long value, boolean isPrivate) {
        SharedPreferences.Editor editor = isPrivate ? sp.edit() : remoteSp
                .edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void save(String key, int value) {
        save(key, value, true);
    }

    public void save(String key, int value, boolean isPrivate) {
        SharedPreferences.Editor editor = isPrivate ? sp.edit() : remoteSp
                .edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void save(String key, float value) {
        save(key, value, true);
    }

    public void save(String key, float value, boolean isPrivate) {
        SharedPreferences.Editor editor = isPrivate ? sp.edit() : remoteSp
                .edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public void save(String key, boolean value) {
        save(key, value, true);
    }

    public void save(String key, boolean value, boolean isPrivate) {
        SharedPreferences.Editor editor = isPrivate ? sp.edit() : remoteSp
                .edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return getBoolean(key, defValue, true);
    }

    public boolean getBoolean(String key, boolean defValue, boolean isPrivate) {
        return isPrivate ? sp.getBoolean(key, defValue) : remoteSp
                .getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return getInt(key, defValue, true);
    }

    public int getInt(String key, int defValue, boolean isPrivate) {
        return isPrivate ? sp.getInt(key, defValue) : remoteSp.getInt(key,
                defValue);
    }

    public long getLong(String key, long defValue) {
        return getLong(key, defValue, true);
    }

    public long getLong(String key, long defValue, boolean isPrivate) {
        return isPrivate ? sp.getLong(key, defValue) : remoteSp.getLong(
                key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return getFloat(key, defValue, true);
    }

    public float getFloat(String key, float defValue, boolean isPrivate) {
        return isPrivate ? sp.getFloat(key, defValue) : remoteSp.getFloat(
                key, defValue);
    }

    public String getString(String key, String defValue) {
        return getString(key, defValue, true);
    }

    public String getString(String key, String defValue, boolean isPrivate) {
        return isPrivate ? sp.getString(key, defValue) : remoteSp
                .getString(key, defValue);
    }

}
