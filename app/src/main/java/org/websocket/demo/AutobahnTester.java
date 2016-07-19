/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.websocket.demo;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Version;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.ws.WebSocket.BINARY;
import static okhttp3.ws.WebSocket.TEXT;

/**
 * Exercises the web socket implementation against the <a
 * href="http://autobahn.ws/testsuite/">Autobahn Testsuite</a>.
 */
public final class AutobahnTester {
  private static final String HOST = "ws://push.qfpay.com";
//    static final String HOST = "ws://172.100.111.41:8887";
    static OkHttpClient okHttpClient;

    private static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
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
    private static X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};

    private static OkHttpClient init() {
        try {

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[0], xtmArray, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(socketFactory)
                    .hostnameVerifier(HOSTNAME_VERIFIER);
            okHttpClient = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }


    public static void main(String... args) throws IOException {
//    new AutobahnTester().run();
        init();
        Request request = new Request.Builder().url(HOST).build();
        WebSocketCall call = WebSocketCall.create(okHttpClient, request);
        call.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("onOpen");
            }

            @Override
            public void onFailure(IOException e, Response response) {
                e.printStackTrace();
                System.out.println("onFailure" + "   " + response.message());
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                System.out.println("onMessage");
            }

            @Override
            public void onPong(Buffer payload) {

            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("onClose");
            }
        });
    }

    final OkHttpClient client = new OkHttpClient();

    private WebSocketCall newWebSocket(String path) {
        Request request = new Request.Builder().url(HOST + path).build();
        return WebSocketCall.create(client, request);
    }

    public void run() throws IOException {
        try {
            long count = getTestCount();
            System.out.println("Test count: " + count);

            for (long number = 1; number <= count; number++) {
                runTest(number, count);
            }

            updateReports();
        } finally {
            client.dispatcher().executorService().shutdown();
        }
    }

    private void runTest(final long number, final long count) throws IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong startNanos = new AtomicLong();
        newWebSocket("/runCase?case=" + number + "&agent=okhttp") //
                .enqueue(new WebSocketListener() {
                    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();
                    private WebSocket webSocket;

                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        this.webSocket = webSocket;

                        System.out.println("Executing test case " + number + "/" + count);
                        startNanos.set(System.nanoTime());
                    }

                    @Override
                    public void onMessage(final ResponseBody message) throws IOException {
                        final RequestBody response;
                        if (message.contentType() == TEXT) {
                            response = RequestBody.create(TEXT, message.string());
                        } else {
                            BufferedSource source = message.source();
                            response = RequestBody.create(BINARY, source.readByteString());
                            source.close();
                        }
                        sendExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    webSocket.sendMessage(response);
                                } catch (IOException e) {
                                    e.printStackTrace(System.out);
                                }
                            }
                        });
                    }

                    @Override
                    public void onPong(Buffer payload) {
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        sendExecutor.shutdown();
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(IOException e, Response response) {
                        e.printStackTrace(System.out);
                        latch.countDown();
                    }
                });
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test " + number + " to finish.");
            }
        } catch (InterruptedException e) {
            throw new AssertionError();
        }

        long endNanos = System.nanoTime();
        long tookMs = TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos.get());
        System.out.println("Took " + tookMs + "ms");
    }

    private long getTestCount() throws IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong countRef = new AtomicLong();
        final AtomicReference<IOException> failureRef = new AtomicReference<>();
        newWebSocket("/getCaseCount").enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                countRef.set(message.source().readDecimalLong());
                message.close();
            }

            @Override
            public void onPong(Buffer payload) {
            }

            @Override
            public void onClose(int code, String reason) {
                latch.countDown();
            }

            @Override
            public void onFailure(IOException e, Response response) {
                failureRef.set(e);
                latch.countDown();
            }
        });
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for count.");
            }
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
        IOException failure = failureRef.get();
        if (failure != null) {
            throw failure;
        }
        return countRef.get();
    }

    private void updateReports() {
        final CountDownLatch latch = new CountDownLatch(1);
        newWebSocket("/updateReports?agent=" + Version.userAgent()).enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
            }

            @Override
            public void onPong(Buffer payload) {
            }

            @Override
            public void onClose(int code, String reason) {
                latch.countDown();
            }

            @Override
            public void onFailure(IOException e, Response response) {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for count.");
            }
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }
}
