package com.cdh.okone.connection.callback

/**
 * 预建联回调
 * Created by chidehang on 2020/12/22
 */
interface PreConnectCallback {
    /**
     * 连接完成
     */
    fun connectCompleted(url: String)

    /**
     * 连接失败
     */
    fun connectFailed(t: Throwable)
}