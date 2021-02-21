package com.cdh.okone.monitor

import okhttp3.EventListener
import okhttp3.Interceptor

/**
 * Created by chidehang on 2021/2/18
 */
object MonitorRegistry {

    /**
     * 全局拦截的EventListener
     */
    var globalEventListener: EventListener?= null

    /**
     * 全局Interceptor
     */
    val globalInterceptors: MutableList<Interceptor> = mutableListOf()

    /**
     * 全局NetworkInterceptor
     */
    val globalNetworkInterceptors: MutableList<Interceptor> = mutableListOf()
}