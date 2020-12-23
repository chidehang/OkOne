package com.cdh.okonedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cdh.okone.OkOne;
import com.cdh.okone.connection.callback.PreConnectCallback;
import com.cdh.okone.util.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealCall;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final String URL_FOR_TEST = "https://stackoverflow.com/";
    private static final String URL_JUEJIN = "https://juejin.cn/";
    private static final String URL_ZHIHU = "https://www.zhihu.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_build_client_1).setOnClickListener(this);
        findViewById(R.id.btn_build_client_2).setOnClickListener(this);
        findViewById(R.id.btn_pre_connect_stackoverflow).setOnClickListener(this);
        findViewById(R.id.btn_pre_connect_juejin).setOnClickListener(this);
        findViewById(R.id.btn_pre_connect_zhihu).setOnClickListener(this);
        findViewById(R.id.btn_build_client_3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_build_client_1:
                OkHttpClient.Builder builder1 = createBuilder1();
                testRequestServer(builder1);
                break;

            case R.id.btn_build_client_2:
                OkHttpClient.Builder builder2 = createBuilder2();
                testRequestServer(builder2);
                break;

            case R.id.btn_build_client_3:
                OkHttpClient.Builder builder3 = createBuilder3();
                testRequestServer(builder3);
                break;

            case R.id.btn_pre_connect_stackoverflow:
                testPreBuildConnection(createBuilder3(), URL_FOR_TEST);
                break;

            case R.id.btn_pre_connect_juejin:
                testPreBuildConnection(createBuilder3(), URL_JUEJIN);
                break;

            case R.id.btn_pre_connect_zhihu:
                testPreBuildConnection(createBuilder3(), URL_ZHIHU);
                break;
        }
    }

    /**
     * 创建配方一
     */
    private OkHttpClient.Builder createBuilder1() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .eventListener(mEventListener);
    }

    /**
     * 创建配方二
     */
    private OkHttpClient.Builder createBuilder2() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048)
                .eventListener(mEventListener);
    }

    /**
     * 创建配方三
     */
    private OkHttpClient.Builder createBuilder3() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048)
                .eventListener(mEventListener);
    }

    /**
     * 测试预建连
     */
    private void testPreBuildConnection(OkHttpClient.Builder builder, String url) {
        OkOne.preBuildConnection(builder.build(), url, new PreConnectCallback() {
            @Override
            public void connectCompleted(String url) {
                Log.d(TAG, "预建连成功: " + url);
            }

            @Override
            public void connectFailed(Throwable t) {
                Log.e(TAG, "预建连失败", t);
            }
        });
    }

    /**
     * 测试不同配方的接口请求
     */
    private void testRequestServer(OkHttpClient.Builder builder) {
        OkHttpClient client = builder.build();
        Log.d(TAG, "创建OkHttpClient: " + client);

        String api = URL_FOR_TEST;
        Request request = new Request.Builder()
                .url(api)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtils.d(TAG, "onFailure() called with: e = [" + e + "]");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                LogUtils.d(TAG, "onResponse() called with: response = [" + response + "]");
            }
        });
    }

    private EventListener mEventListener = new EventListener() {
        final String TAG = "okflow";

        @Override
        public void callStart(@NotNull Call call) {
            LogUtils.d(TAG, "callStart-> " + ((RealCall) call).request().toString());
        }

        @Override
        public void dnsStart(@NotNull Call call, @NotNull String domainName) {
            LogUtils.d(TAG, "dnsStart-> domainName = [" + domainName + "]");
        }

        @Override
        public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
            LogUtils.d(TAG, "dnsEnd-> inetAddressList = [" + inetAddressList + "]");
        }

        @Override
        public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
            LogUtils.d(TAG, "connectStart-> inetSocketAddress = [" + inetSocketAddress + "], proxy = [" + proxy + "]");
        }

        @Override
        public void secureConnectStart(@NotNull Call call) {
            LogUtils.d(TAG, "secureConnectStart->");
        }

        @Override
        public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
            LogUtils.d(TAG, "secureConnectEnd-> handshake = [" + handshake + "]");
        }

        @Override
        public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
            LogUtils.d(TAG, "connectEnd->  protocol = [" + protocol + "]");
        }

        @Override
        public void connectFailed(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol, @NotNull IOException ioe) {
            LogUtils.d(TAG, "connectFailed-> protocol = [" + protocol + "], ioe = [" + ioe + "]");
        }

        @Override
        public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
            LogUtils.d(TAG, "connectionAcquired-> connection = [" + connection + "]");
        }

        @Override
        public void connectionReleased(@NotNull Call call, @NotNull Connection connection) {
            LogUtils.d(TAG, "connectionReleased-> connection = [" + connection + "]");
        }

        @Override
        public void requestHeadersStart(@NotNull Call call) {
            LogUtils.d(TAG, "requestHeadersStart->");
        }

        @Override
        public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
            LogUtils.d(TAG, "requestHeadersEnd-> request = [" + request + "]");
        }

        @Override
        public void requestBodyStart(@NotNull Call call) {
            LogUtils.d(TAG, "requestBodyStart->");
        }

        @Override
        public void requestBodyEnd(@NotNull Call call, long byteCount) {
            LogUtils.d(TAG, "requestBodyEnd-> byteCount = [" + byteCount + "]");
        }

        @Override
        public void requestFailed(@NotNull Call call, @NotNull IOException ioe) {
            LogUtils.d(TAG, "requestFailed-> ioe = [" + ioe + "]");
        }

        @Override
        public void responseHeadersStart(@NotNull Call call) {
            LogUtils.d(TAG, "responseHeadersStart->");
        }

        @Override
        public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
            LogUtils.d(TAG, "responseHeadersEnd-> response = [" + response + "]");
        }

        @Override
        public void responseBodyStart(@NotNull Call call) {
            LogUtils.d(TAG, "responseBodyStart->");
        }

        @Override
        public void responseBodyEnd(@NotNull Call call, long byteCount) {
            LogUtils.d(TAG, "responseBodyEnd-> byteCount = [" + byteCount + "]");
        }

        @Override
        public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
            LogUtils.d(TAG, "responseFailed-> ioe = [" + ioe + "]");
        }

        @Override
        public void callEnd(@NotNull Call call) {
            LogUtils.d(TAG, "callEnd->");
        }

        @Override
        public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
            LogUtils.d(TAG, "callFailed-> ioe = [" + ioe + "]");
        }
    };
}