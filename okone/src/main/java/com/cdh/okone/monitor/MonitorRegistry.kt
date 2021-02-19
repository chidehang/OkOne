package com.cdh.okone.monitor

import okhttp3.EventListener

/**
 * Created by chidehang on 2021/2/18
 */
object MonitorRegistry {

    /**
     * 全局拦截的EventListener
     */
    var hostEventListener: EventListener?= null
}