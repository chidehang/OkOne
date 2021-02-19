package com.cdh.okone.monitor

import okhttp3.Call
import okhttp3.EventListener

/**
 * Created by chidehang on 2021/2/18
 */
class GlobalEventListenerFactory(
        private val delegate: EventListener.Factory
) : EventListener.Factory {

    override fun create(call: Call?): EventListener {
        // 全局拦截设置的EventListener
        val host = MonitorRegistry.hostEventListener
        // 业务方设置的原始EventListener
        val source = delegate.create(call)
        return if (host == null) {
            // 未设置全局EventListener，直接返回原始EventListener
            source
        } else {
            UniteEventListener(host, source)
        }
    }
}