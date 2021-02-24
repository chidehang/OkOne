package com.cdh.okonedemo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cdh.okone.GlobalOkHttpClientManager;
import com.cdh.okone.OkOne;
import com.cdh.okone.connection.callback.PreConnectCallback;
import com.cdh.okone.priority.PriorityArrayDeque;
import com.cdh.okone.priority.RequestPriorityProcessor;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by chidehang on 2021/2/23
 */
@RunWith(AndroidJUnit4.class)
public class OkOneTest {

    private OkHttpClient.Builder createBuilder1() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor());
    }

    private OkHttpClient.Builder createBuilder2() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048);
    }

    private OkHttpClient.Builder createBuilder3() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048);
    }

    @Before
    public void configOkOne() {
        OkOne.useGlobalClient = true;
        OkOne.enableRequestPriority(true);
    }

    @Test
    public void disableReuseOkHttpClient() {
        OkOne.useGlobalClient = false;
        OkHttpClient client1 = createBuilder1().build();
        OkHttpClient client2 = createBuilder1().build();
        assertNotSame(client1, client2);
    }

    @Test
    public void reuseOkHttpClient() {
        OkHttpClient client1 = createBuilder1().build();
        OkHttpClient client2 = createBuilder1().build();
        assertSame(client1, client2);
    }

    @Test
    public void differentOkHttpClientBuilder() {
        OkHttpClient client1 = createBuilder1().build();
        OkHttpClient client2 = createBuilder2().build();
        assertNotSame(client1, client2);
    }

    @Test
    public void differentOkHttpClientBuilder2() {
        OkHttpClient.Builder builder1 = createBuilder1().readTimeout(11, TimeUnit.SECONDS);
        OkHttpClient.Builder builder2 = createBuilder1().readTimeout(12, TimeUnit.SECONDS);

        TreeMap map1;
        TreeMap map2;
        try {
            map1 = (TreeMap) builder1.getClass().getDeclaredField("okone_configMap").get(builder1);
            map2 = (TreeMap) builder2.getClass().getDeclaredField("okone_configMap").get(builder2);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        OkHttpClient client1 = builder1.build();
        OkHttpClient client2 = builder2.build();
        assertNotSame(client1, client2);
    }

    @Test
    public void preBuildConnection() {
        final CountDownLatch latch = new CountDownLatch(1);

        final String url = "https://stackoverflow.com/";
        final TestPreConnectEventListener eventListener = new TestPreConnectEventListener();
        final OkHttpClient client = createBuilder3().eventListener(eventListener).build();
        // 第一次预建连
        OkOne.preBuildConnection(client, url, new PreConnectCallback() {
            @Override
            public void connectCompleted(@NotNull String s) {
                // 第二次预建连
                OkOne.preBuildConnection(client, url, new PreConnectCallback() {
                    @Override
                    public void connectCompleted(@NotNull String s) {
                        fail("不应该重复预建连");
                    }

                    @Override
                    public void connectFailed(@NotNull Throwable throwable) {
                        // 发起接口请求
                        eventListener.shouldNotConnect = true;
                        Request request = new Request.Builder().url(url).build();
                        try {
                            client.newCall(request).execute();
                            latch.countDown();
                        } catch (IOException e) {
                            fail(e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void connectFailed(@NotNull Throwable throwable) {
                fail(throwable.getMessage());
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                fail("预建连测试超时");
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static class TestPreConnectEventListener extends EventListener {

        public volatile boolean shouldNotConnect = false;

        @Override
        public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
            if (shouldNotConnect) {
                fail("不应该再次触发connectStart");
            }
        }
    }

    @Test
    public void disableRequestPriority() {
        OkOne.enableRequestPriority(false);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Dispatcher dispatcher = client.dispatcher();
        try {
            Field field = dispatcher.getClass().getDeclaredField("readyAsyncCalls");
            field.setAccessible(true);
            Object obj = field.get(dispatcher);
            assertFalse(obj instanceof PriorityArrayDeque);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    @Test
    public void enableRequestPriority() {
        OkHttpClient client = new OkHttpClient.Builder().eventListener(new TestPriorityEventListener()).build();
        client.dispatcher().setMaxRequests(1);

        // 随机生成10个不同优先级的请求
        int N = 10;

        CountDownLatch latch = new CountDownLatch(N);

        Request[] requests = new Request[N];
        Random r = new Random(System.currentTimeMillis());
        for (int i=0; i<N; i++) {
            int priority = r.nextInt(20) - 10;
            requests[i] = new Request.Builder()
                    .url("https://stackoverflow.com/")
                    .build();
            OkOne.setRequestPriority(requests[i], priority);
        }

        for (int i=0; i<N; i++) {
            client.newCall(requests[i]).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    latch.countDown();
                }
            });
        }

        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("请求优先级测试超时");
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static class TestPriorityEventListener extends EventListener {
        // 记录上一次请求的优先级，正常应该逐个变小
        public volatile int lastPriority = RequestPriorityProcessor.PRIORITY_MAX + 1;

        @Override
        public void requestHeadersStart(@NotNull Call call) {
            if (lastPriority == RequestPriorityProcessor.PRIORITY_MAX+1) {
                // 这里是为了跳过前两个请求，在三个请求再进行比较
                lastPriority = RequestPriorityProcessor.PRIORITY_MAX;
            } else {
                int curPriority = OkOne.getRequestPriority(call.request());
                if (curPriority > lastPriority) {
                    fail("当次请求较前一次请求的优先级高");
                }
                lastPriority = curPriority;
            }
        }
    }

    @Test
    public void setGlobalEventListener() {
        final CountDownLatch latch = new CountDownLatch(6);

        EventListener globalEventListener = new EventListener() {
            @Override
            public void callStart(@NotNull Call call) {
                latch.countDown();
            }
        };
        OkOne.setGlobalEventListener(globalEventListener);

        OkHttpClient client1 = createBuilder1()
                .eventListener(new EventListener() {
                    @Override
                    public void callStart(@NotNull Call call) {
                        latch.countDown();
                    }
                }).build();
        OkHttpClient client2 = createBuilder1()
                .eventListener(new EventListener() {
                    @Override
                    public void callStart(@NotNull Call call) {
                        latch.countDown();
                    }
                }).build();
        OkHttpClient client3 = createBuilder1()
                .eventListener(new EventListener() {
                    @Override
                    public void callStart(@NotNull Call call) {
                        latch.countDown();
                    }
                }).build();

        assertNotSame(client1, client2);
        assertNotSame(client2, client3);
        assertNotSame(client1, client3);

        Request request = new Request.Builder().url("https://stackoverflow.com/").build();
        try {
            client1.newCall(request).execute();
            client2.newCall(request).execute();
            client3.newCall(request).execute();
            assertEquals(latch.getCount(), 0);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addGlobalInterceptor() {
        CountDownLatch latch = new CountDownLatch(2);

        Interceptor interceptor = new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                latch.countDown();
                return chain.proceed(chain.request());
            }
        };
        OkOne.addGlobalInterceptor(interceptor);

        OkHttpClient client1 = createBuilder1().connectTimeout(21, TimeUnit.SECONDS).build();
        OkHttpClient client2 = createBuilder1().connectTimeout(22, TimeUnit.SECONDS).build();
        assertNotSame(client1, client2);

        Request request = new Request.Builder().url("https://stackoverflow.com/").build();
        try {
            client1.newCall(request).execute();
            client2.newCall(request).execute();
            assertEquals(latch.getCount(), 0);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addGlobalNetworkInterceptor() {
        CountDownLatch latch = new CountDownLatch(2);

        Interceptor interceptor = new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                latch.countDown();
                return chain.proceed(chain.request());
            }
        };
        OkOne.addGlobalNetworkInterceptor(interceptor);

        OkHttpClient client1 = createBuilder1().writeTimeout(31, TimeUnit.SECONDS).build();
        OkHttpClient client2 = createBuilder1().writeTimeout(32, TimeUnit.SECONDS).build();
        assertNotSame(client1, client2);

        Request request = new Request.Builder().url("https://stackoverflow.com/").build();
        try {
            client1.newCall(request).execute();
            client2.newCall(request).execute();
            assertEquals(latch.getCount(), 0);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
