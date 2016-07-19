package org.websocket.demo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil
{
    private static SPUtil saver;
    private final static int MODE = Context.MODE_WORLD_READABLE
            | Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS;
    private static SharedPreferences sp;

    public static SPUtil getInstance(Context context)
    {
        if (saver == null)
        {
            saver = new SPUtil();
            sp = context.getSharedPreferences("shared", MODE);
        }
        
        return saver;
    }

    public void clearAll()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    public void save(String key, String value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void save(String key, long value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void save(String key, int value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void save(String key, float value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public void save(String key, boolean value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public boolean getBoolean(String key, boolean defValue)
    {
        return sp.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue)
    {
        return sp.getInt(key, defValue);
    }

    public long getLong(String key, long defValue)
    {
        return sp.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue)
    {
        return sp.getFloat(key, defValue);
    }

    public String getString(String key, String defValue)
    {
        return sp.getString(key, defValue);
    }

}
