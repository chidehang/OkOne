package com.cdh.okone;

import com.cdh.okone.connection.BuildConnectionProcessor;
import com.cdh.okone.connection.callback.PreConnectCallback;
import com.cdh.okone.util.LogUtils;

import okhttp3.OkHttpClient;

/**
 * Created by chidehang on 2020/11/24
 */
public class OkOne {

    /**
     * 是否启用全局统一OkHttpClient
     */
    public static volatile boolean useGlobalClient = true;

    public static void setLogEnable(boolean enable) {
        LogUtils.setEnableLog(enable);
    }

    /**
     * 预建连
     */
    public static void preBuildConnection(OkHttpClient client, String url, PreConnectCallback callback) {
        BuildConnectionProcessor.buildConnection(client, url, callback);
    }
}
