package com.cdh.okone.connection

import android.text.TextUtils
import com.cdh.okone.connection.callback.PreConnectCallback
import com.cdh.okone.util.LogUtils
import com.cdh.okone.util.LogUtils.d
import okhttp3.*
import okio.Timeout
import java.io.IOException

/**
 * Created by chidehang on 2020/12/22
 */
object BuildConnectionProcessor {

    private const val TAG = "BuildConnectionProcesso"

    @JvmField
    val NONE_CALL: Call = object : Call {
        override fun request(): Request {
            return null!!
        }

        @Throws(IOException::class)
        override fun execute(): Response {
            return null!!
        }

        override fun enqueue(callback: Callback) {}

        override fun cancel() {}

        override fun isExecuted(): Boolean {
            return false
        }

        override fun isCanceled(): Boolean {
            return false
        }

        override fun timeout(): Timeout {
            return null!!
        }

        override fun clone(): Call {
            return null!!
        }
    }

    /**
     * 预建连
     */
    @JvmStatic
    fun buildConnection(client: OkHttpClient?, url: String?, callback: PreConnectCallback?) {
        if (client == null || TextUtils.isEmpty(url)) {
            callback?.connectFailed(IllegalArgumentException("Client or url is null."))
            return
        }

        if (client.connectionPool().idleConnectionCount() >= 5) {
            if (LogUtils.isEnabled) d(TAG, "buildConnection: 空闲连接数达到5个")
            // 空闲连接数达到5个
            callback?.connectFailed(IllegalStateException("The idle connections reached the upper limit<5>."))
            return
        }

        val runnable = PreConnectRunnable(client, url!!, callback)
        client.dispatcher().executorService().execute(runnable)
    }
}