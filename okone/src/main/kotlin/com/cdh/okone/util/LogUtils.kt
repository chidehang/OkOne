package com.cdh.okone.util

import android.os.Process
import android.util.Log

/**
 * LogUtils
 * @author business
 */
object LogUtils {
    private const val GLOBAL_TAG = "okone"

    /**
     * 打包更新时间，通过查看log的update_time可以检查测试安装的包是不是最新包
     */
    private const val MAKE_UPDATE_TIME = "1"
    private const val INDEX = 4

    @JvmField
    var isEnabled = true

    @JvmStatic
    fun setEnableLog(enable: Boolean) {
        isEnabled = enable
    }

    @JvmStatic
    @JvmOverloads
    fun v(tag: String, msg: String?, deep: Int = INDEX) {
        if (isEnabled) {
            Log.v(GLOBAL_TAG, formatMsg(tag, msg, deep))
        }
    }

    /**
     * 由于华为手机打印不出debug类型的日志，所以所有的Log.d的全部改为Log.i
     */
    @JvmStatic
    @JvmOverloads
    fun d(tag: String, msg: String?, deep: Int = INDEX) {
        if (isEnabled) {
            Log.d(GLOBAL_TAG, formatMsg(tag, msg, deep))
        }
    }

    @JvmStatic
    @JvmOverloads
    fun i(tag: String, msg: String?, deep: Int = INDEX) {
        if (isEnabled) {
            Log.i(GLOBAL_TAG, formatMsg(tag, msg, deep))
        }
    }

    @JvmStatic
    @JvmOverloads
    fun e(tag: String, msg: String?, deep: Int = INDEX) {
        if (isEnabled) {
            Log.e(GLOBAL_TAG, formatMsg(tag, msg, deep))
        }
    }

    @JvmStatic
    @JvmOverloads
    fun w(tag: String, msg: String?, deep: Int = INDEX) {
        if (isEnabled) {
            Log.w(GLOBAL_TAG, formatMsg(tag, msg, deep))
        }
    }

    @JvmStatic
    @JvmOverloads
    fun e(subTag: String, pMessage: String?, e: Throwable?, deep: Int = INDEX) {
        var pMessage = pMessage
        if (isEnabled) {
            if (pMessage == null) {
                pMessage = "noMsg"
            }
            if (e == null) {
                Log.e(GLOBAL_TAG, formatMsg(subTag, pMessage, deep))
            } else {
                Log.e(GLOBAL_TAG, formatMsg(subTag, pMessage, deep), e)
            }
        }
    }

    private fun formatMsg(tag: String, msg: String?, deep: Int): String {
        return String.format("%s[%s][%s][%s]%s%s", MAKE_UPDATE_TIME, Process.myPid(), Thread.currentThread(), tag, msg, getTrace(deep))
    }

    private fun getTrace(index: Int): String {
        var index = index
        val stacks = Throwable().stackTrace
        if (index <= 0) {
            index = INDEX
        }
        return if (stacks.size <= index) {
            ""
        } else {
            String.format("(%s:%d)", stacks[index].fileName, stacks[index].lineNumber)
        }
    }

    @JvmStatic
    fun printStackTrace(t: Throwable?) {
        if (isEnabled && t != null) {
            t.printStackTrace()
            e(GLOBAL_TAG, t.message, INDEX)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String?, time: Long) {
        if (isEnabled) {
            Log.i(GLOBAL_TAG, formatMsg(tag, String.format("%s [t1=%d][t2=%d]", msg, time, System.currentTimeMillis() - time), INDEX))
        }
    }
}