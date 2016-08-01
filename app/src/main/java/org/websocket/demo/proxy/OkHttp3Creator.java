package org.websocket.demo.proxy;


import android.content.Context;

import org.websocket.demo.util.Constant;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttp3Creator {

    private static OkHttp3Creator instance;
    private OkHttpClient okHttpClient;
    //信任全部
    private static X509TrustManager xtm = new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    };

    private final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};

    private OkHttp3Creator(Context context) {
        init(context);
    }

    public static OkHttp3Creator instance(Context context) {
        if (instance == null) {
            synchronized (OkHttp3Creator.class) {
                if (instance == null) {
                    instance = new OkHttp3Creator(context);
                }
            }
        }
        return instance;
    }

    private OkHttpClient init(Context context) {
        try {
//            CookiesManager manager = new CookiesManager(context);
            // Allow all hostNames
            //设置信任所有
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[0], xtmArray, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(Constant.DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .writeTimeout(0, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(socketFactory)
                    .hostnameVerifier(HOSTNAME_VERIFIER);

            HttpLoggingInterceptor.Level level = /*ConstValue.DEBUG_MODE ? HttpLoggingInterceptor.Level.HEADERS : */HttpLoggingInterceptor.Level.BODY;

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new MyLogger()).setLevel(level);

            builder.addInterceptor(loggingInterceptor);    // log

            okHttpClient = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }

    public OkHttpClient getOkHttp3Client() {
        return okHttpClient;
    }

    public void setOkHttp3Client(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    private class MyLogger implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(String message) {
            System.err.println("OKHTTP3 ---->" + message);
        }
    }
}
