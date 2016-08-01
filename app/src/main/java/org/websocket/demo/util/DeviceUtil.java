package org.websocket.demo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 设备信息获取工具类
 * Created by yamlee on 15/11/19.
 */
public class DeviceUtil {
    private static String udid;

    private static String sID = "";
    private static final String UNIQUEID = UUID.randomUUID().toString();

    /**
     * 获取设备唯一标识号
     *
     * @param context
     * @return
     */
    public static String getDeviceID(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (TextUtils.isEmpty(udid)) {
            udid = tm.getDeviceId();
        }
        if (TextUtils.isEmpty(udid)) {
            udid = "0";
        }
        return udid;
    }

    /**
     * 获取唯一标识号
     *
     * @param context
     * @return
     */
    public static String getUniqueId(Context context) {


        // 取打印机设备号
        return SPUtil.getInstance(context).getString(Constant.SPKey.KEY_PRINT_DEVICE_ID, Constant.print_device_id, false);
//        if (context == null) {
//            return "";
//        }
//        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceId = "";
//
//        try {
//            deviceId = telephonyManager.getDeviceId();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (TextUtils.isEmpty(deviceId)) {
//            try {
//                deviceId = telephonyManager.getSimSerialNumber();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (TextUtils.isEmpty(deviceId)) {
//            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            String macAddress = wifiManager.getConnectionInfo().getMacAddress();
//            deviceId = md5(macAddress);
//        }
//        if (TextUtils.isEmpty(deviceId)) {
//            try {
//                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (TextUtils.isEmpty(deviceId)) {
//            deviceId = md5(handleUuid(context));
//        }
//        return deviceId;
    }

    private synchronized static String handleUuid(Context context) {
        if (sID == null) {
            File uniqueid = new File(context.getFilesDir(), UNIQUEID);
            try {
                if (!uniqueid.exists())
                    writeUniqueIdFile(uniqueid);
                sID = readUniqueIdFile(uniqueid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readUniqueIdFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeUniqueIdFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    private static String md5(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
//            System.out.println("result: " + buf.toString());//32位的加密
//            System.out.println("result: " + buf.toString().substring(8,24));//16位的加密
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取手机设备名称
     *
     * @return
     */
    public static String getDeviceName() {
        return Build.BRAND + " " + Build.MODEL;
    }

    /**
     * 获取系统版本
     *
     * @return
     */
    public static String getOsVersionStr() {
        return Build.VERSION.RELEASE;
    }

    /**
     * @return
     */
    public static int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static String getNetworkAccessMode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission("android.permission.ACCESS_NETWORK_STATE",
                    context.getPackageName()) != 0) {
                return "Unknown";
            }

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return "Unknown";
            }

            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            String typeName = info.getTypeName().toLowerCase(); // WIFI/MOBILE
            if (typeName.equals("wifi")) {
            } else {
                typeName = info.getExtraInfo().toLowerCase();
                // 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
            }
            if (typeName != null) {
                return typeName;
            }

            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static void setWifiNeverSleep(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
//        if (Build.VERSION.SDK_INT < 17) {
        int wifiSleepPolicy = Settings.System.getInt(resolver,
                android.provider.Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        System.out.println("---> 修改前的Wifi休眠策略值 WIFI_SLEEP_POLICY=" + wifiSleepPolicy);

        Settings.System.putInt(resolver,
                android.provider.Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_NEVER);

        wifiSleepPolicy = Settings.System.getInt(resolver,
                android.provider.Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        System.out.println("---> 修改后的Wifi休眠策略值 WIFI_SLEEP_POLICY=" + wifiSleepPolicy);
//        } else {
//            int wifiSleepPolicy = Settings.System.getInt(resolver,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
//            System.out.println("---> 17+修改前的Wifi休眠策略值 WIFI_SLEEP_POLICY=" + wifiSleepPolicy);
//            android.provider.Settings.Global.putInt(resolver,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY_NEVER);
//            wifiSleepPolicy = Settings.System.getInt(resolver,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY,
//                    android.provider.Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
//            System.out.println("---> 17+修改后的Wifi休眠策略值 WIFI_SLEEP_POLICY=" + wifiSleepPolicy);
//        }

    }

    public static String getAppType(Context context) {
        String appType = "";
        try {
            appType = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("app_type");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appType = Constant.app_type;
        }
        return appType;
    }

    public static String getSecretKey(Context context) {
        String appType = "";
        try {
            appType = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("secret_key");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appType = Constant.secret_key;
        }
        return appType;
    }

}
