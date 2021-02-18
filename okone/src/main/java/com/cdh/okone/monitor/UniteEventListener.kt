package com.cdh.okone.monitor

import okhttp3.EventListener

/**
 * 组合全局EventListener和原始EventListener，用于拦截转发
 * Created by chidehang on 2021/2/18
 */
class UniteEventListener(
        private val host : EventListener,
        private val source : EventListener
) : EventListener() {


}