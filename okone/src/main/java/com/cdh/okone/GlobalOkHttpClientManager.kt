package com.cdh.okone

import com.cdh.okone.util.LogUtils
import com.cdh.okone.util.LogUtils.d
import okhttp3.OkHttpClient
import java.util.*

/**
 * Created by chidehang on 2021/2/10
 */
class GlobalOkHttpClientManager private constructor() {

    companion object {
        private val DEBUG = LogUtils.isEnabled
        private const val TAG = "GlobalOkHttpClientManager"

        fun getInstance(): GlobalOkHttpClientManager {
            return InnerHolder.INSTANCE
        }
    }

    private object InnerHolder {
        internal val INSTANCE = GlobalOkHttpClientManager()
    }

    private val mOkHttpClientCache = HashMap<OkHttpClient.Builder, OkHttpClient>()

    fun buildOkHttpClient(builder: OkHttpClient.Builder): OkHttpClient {
        if (DEBUG) d(TAG, "buildOkHttpClient() called with: builder = [$builder]")
        if (!OkOne.useGlobalClient) {
            return OkHttpClient(builder)
        }

        var client: OkHttpClient?
        synchronized(mOkHttpClientCache) {
            // 查找可复用的缓存的OkHttpClient
            client = retrieveOkHttpClient(builder)
            if (client != null) {
                if (DEBUG) d(TAG, "命中缓存，可复用OkHttpClient")
            } else {
                if (DEBUG) d(TAG, "未命中缓存，新建OkHttpClient")
                // 未找到则新建，添加进缓存
                client = OkHttpClient(builder)
                mOkHttpClientCache[builder] = client!!
            }
        }
        return client!!
    }

    /**
     * 和缓存实例比较，获取可复用的OkHttpClient
     */
    private fun retrieveOkHttpClient(builder: OkHttpClient.Builder): OkHttpClient? {
        synchronized(mOkHttpClientCache) {
            try {
                val iterator: Iterator<*> = mOkHttpClientCache.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next() as Map.Entry<OkHttpClient.Builder, OkHttpClient>
                    // 进行比较
                    if (builder.okone_equivalentTo(entry.key)) {
                        // 比较结果相等，返回复用实例
                        return entry.value
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return null
    }
}