package com.cdh.okone.connection;

import android.text.TextUtils;

import com.cdh.okone.connection.callback.PreConnectCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

/**
 * Created by chidehang on 2020/12/22
 */
public class BuildConnectionProcessor {

    public static final Call NONE_CALL = new Call() {
        @Override
        public Request request() {
            return null;
        }

        @Override
        public Response execute() throws IOException {
            return null;
        }

        @Override
        public void enqueue(@NotNull Callback callback) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Timeout timeout() {
            return null;
        }

        @Override
        public Call clone() {
            return null;
        }
    };

    /**
     * 预建连
     */
    public static void buildConnection(OkHttpClient client, String url, PreConnectCallback callback) {
        if (client == null || TextUtils.isEmpty(url)) {
            if (callback != null) {
                callback.connectFailed(new IllegalArgumentException("Client or url is null."));
            }
            return;
        }

        if (client.connectionPool().idleConnectionCount() >= 5) {
            // 空闲连接数达到5个
            if (callback != null) {
                callback.connectFailed(new IllegalStateException("The idle connections reached the upper limit<5>."));
            }
            return;
        }

        PreConnectRunnable runnable = new PreConnectRunnable(client, url, callback);
        client.dispatcher().executorService().execute(runnable);
    }
}
