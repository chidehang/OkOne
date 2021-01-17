package com.cdh.okone;

import com.cdh.okone.connection.BuildConnectionProcessor;
import com.cdh.okone.connection.callback.PreConnectCallback;
import com.cdh.okone.priority.RequestPriorityProcessor;
import com.cdh.okone.util.LogUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

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

    /**
     * 是否启用请求优先级
     */
    public static void enableRequestPriority(boolean enable) {
        RequestPriorityProcessor.enableRequestPriority = enable;
    }

    /**
     * 设置请求的优先级[-10~10]
     * 数值越大优先级越高
     */
    public static void setRequestPriority(Request request, int priority) {
        RequestPriorityProcessor.setRequestPriority(request, priority);
    }

    /**
     * 获取请求的优先级，默认0
     */
    public static int getRequestPriority(Request request) {
        return RequestPriorityProcessor.getRequestPriority(request);
    }
}
