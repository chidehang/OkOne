package com.cdh.okone.connection

import com.cdh.okone.connection.callback.PreConnectCallback
import com.cdh.okone.util.LogUtils
import com.cdh.okone.util.LogUtils.d
import okhttp3.*
import okhttp3.internal.connection.RealConnection
import okhttp3.internal.connection.RealConnectionPool
import okhttp3.internal.connection.RouteSelector
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

/**
 * Created by chidehang on 2021/2/10
 */
class PreConnectRunnable(
        val mClient: OkHttpClient,
        val mUrl: String,
        val mPreConnectCallback: PreConnectCallback?
) : Runnable {

    override fun run() {
        val oldName = Thread.currentThread().name
        Thread.currentThread().name = THREAD_NAME_PREFIX + mUrl
        innerRun()
        Thread.currentThread().name = oldName
    }

    private fun innerRun() {
        if (LogUtils.isEnabled) d(TAG, "PreConnectRunnable#innerRun() called")
        try {
            val httpUrl = createHttpUrl(mUrl)
            if (httpUrl == null) {
                callConnectFailed(mPreConnectCallback, IllegalArgumentException("unexpected url: $mUrl"))
                return
            }

            val address = createAddress(mClient, httpUrl)
            val routes = selectRoutes(mClient, address)
            if (routes == null || routes.isEmpty()) {
                callConnectFailed(mPreConnectCallback, IOException("No route available."))
                return
            }

            val realConnectionPool = findRealConnectionPool(mClient)
            if (hasPooledConnection(realConnectionPool, address, routes, false)) {
                callConnectFailed(mPreConnectCallback, IllegalStateException("There is already a connection with the same address.[1]"))
                return
            }

            val route = routes[0]
            val connection = RealConnection(realConnectionPool, route)
            // 开始连接，如果失败，内部将抛异常
            connection.connect(
                    mClient.connectTimeoutMillis(),
                    mClient.readTimeoutMillis(),
                    mClient.writeTimeoutMillis(),
                    mClient.pingIntervalMillis(),
                    false,
                    BuildConnectionProcessor.NONE_CALL,
                    EventListener.NONE
            )
            mClient.routeDatabase.connected(connection.route())

            if (hasPooledConnection(realConnectionPool, address, routes, true)) {
                try {
                    connection.socket().close()
                } catch (t: Throwable) {
                }
                callConnectFailed(mPreConnectCallback, java.lang.IllegalStateException("There is already a connection with the same address.[2]"))
                return
            }

            synchronized(connection) { realConnectionPool.put(connection) }

            callConnectCompleted(mPreConnectCallback, mUrl)
        } catch (t: Throwable) {
            t.printStackTrace();
            callConnectFailed(mPreConnectCallback, t);
        }
    }

    companion object {
        private const val TAG = "PreConnectRunnable"
        private const val THREAD_NAME_PREFIX = "pre-connect-"

        private fun createHttpUrl(url: String): HttpUrl? {
            var url = url
            if (url.regionMatches(0, "ws:", 0, 3, ignoreCase = true)) {
                url = "http:" + url.substring(3)
            } else if (url.regionMatches(0, "wss:", 0, 4, ignoreCase = true)) {
                url = "https:" + url.substring(4)
            }
            return HttpUrl.parse(url)
        }

        private fun createAddress(client: OkHttpClient, url: HttpUrl): Address {
            var sslSocketFactory: SSLSocketFactory? = null
            var hostnameVerifier: HostnameVerifier? = null
            var certificatePinner: CertificatePinner? = null
            if (url.isHttps) {
                sslSocketFactory = client.sslSocketFactory()
                hostnameVerifier = client.hostnameVerifier()
                certificatePinner = client.certificatePinner()
            }
            return Address(
                    url.host(),
                    url.port(),
                    client.dns(),
                    client.socketFactory(),
                    sslSocketFactory,
                    hostnameVerifier,
                    certificatePinner,
                    client.proxyAuthenticator(),
                    client.proxy(),
                    client.protocols(),
                    client.connectionSpecs(),
                    client.proxySelector()
            )
        }

        @Throws(IOException::class)
        private fun selectRoutes(client: OkHttpClient, address: Address): List<Route>? {
            val routeSelector = RouteSelector(address, client.routeDatabase, BuildConnectionProcessor.NONE_CALL, EventListener.NONE)
            val selection = if (routeSelector.hasNext()) routeSelector.next() else null
            return selection?.routes
        }

        /**
         * 检查是否已经有缓存的相同地址的连接
         */
        private fun hasPooledConnection(realConnectionPool: RealConnectionPool, address: Address, routes: List<Route>, requireMultiplexed: Boolean): Boolean {
            return try {
                val connectionsField = realConnectionPool.javaClass.getDeclaredField("connections")
                connectionsField.isAccessible = true
                val connections = connectionsField[realConnectionPool] as ConcurrentLinkedQueue<RealConnection>

                // 遍历现存connection
                for (connection in connections) {
                    synchronized(connection) {
                        var dup = true

                        if (requireMultiplexed) {
                            val isMultiplexed = connection.javaClass.getDeclaredMethod("isMultiplexed\$okhttp")
                            isMultiplexed.isAccessible = true
                            val value = isMultiplexed.invoke(connection) as Boolean
                            if (!value) {
                                dup = false
                            }
                        }

                        if (dup) {
                            val isEligible = connection.javaClass.getDeclaredMethod("isEligible\$okhttp", Address::class.java, MutableList::class.java)
                            isEligible.isAccessible = true
                            val result = isEligible.invoke(connection, address, routes) as Boolean
                            if (!result) {
                                dup = false
                            }
                        }

                        if (dup) {
                            // 已经存在connection
                            return true
                        }
                    }
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                throw UnsupportedOperationException("Does not support the current version of okhttp.")
            }
        }

        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        private fun findRealConnectionPool(client: OkHttpClient): RealConnectionPool {
            val delegateField = client.connectionPool().javaClass.getDeclaredField("delegate")
            delegateField.isAccessible = true
            return delegateField[client.connectionPool()] as RealConnectionPool
        }

        private fun callConnectCompleted(callback: PreConnectCallback?, url: String) {
            if (LogUtils.isEnabled) d(TAG, "callConnectCompleted() called with: callback = [$callback], url = [$url]")
            callback?.connectCompleted(url)
        }

        private fun callConnectFailed(callback: PreConnectCallback?, t: Throwable) {
            if (LogUtils.isEnabled) d(TAG, "callConnectFailed() called with: callback = [$callback], t = [$t]")
            callback?.connectFailed(t)
        }
    }
}