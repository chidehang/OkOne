package com.cdh.okone.connection.callback;

/**
 * 预建联回调
 * Created by chidehang on 2020/12/22
 */
public interface PreConnectCallback {

    /**
     * 连接完成
     */
    void connectCompleted(String url);

    /**
     * 连接失败
     */
    void connectFailed(Throwable t);
}
