package com.cdh.okone.priority

import com.cdh.okone.util.LogUtils.printStackTrace
import okhttp3.Request

/**
 * 设置Call请求的优先级
 * Created by chidehang on 2021/1/17
 */
object RequestPriorityProcessor {

    @JvmField
    @Volatile
    var enableRequestPriority = false

    const val PRIORITY_DEFAULT = 0
    const val PRIORITY_MIN = -10
    const val PRIORITY_MAX = 10

    @JvmStatic
    fun setRequestPriority(request: Request, priority: Int) {
        if (enableRequestPriority) {
            try {
                request.okone_priority = checkBounds(priority)
            } catch (t: Throwable) {
                printStackTrace(t)
            }
        }
    }

    @JvmStatic
    fun getRequestPriority(request: Request): Int {
        if (enableRequestPriority) {
            try {
                return request.okone_priority
            } catch (t: Throwable) {
                printStackTrace(t)
            }
        }
        return PRIORITY_DEFAULT
    }

    private fun checkBounds(priority: Int): Int {
        return when {
            priority > PRIORITY_MAX -> {
                PRIORITY_MAX
            }
            priority < PRIORITY_MIN -> {
                PRIORITY_MIN
            }
            else -> {
                priority
            }
        }
    }
}