package com.cdh.okone.monitor

import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * 组合全局EventListener和原始EventListener，用于拦截转发
 * Created by chidehang on 2021/2/18
 */
class UniteEventListener(
        private val host : EventListener?,
        private val source : EventListener?
) : EventListener() {

    override fun cacheConditionalHit(call: Call, cachedResponse: Response) {
        host?.cacheConditionalHit(call, cachedResponse)
        source?.cacheConditionalHit(call, cachedResponse)
    }

    override fun cacheHit(call: Call, response: Response) {
        host?.cacheHit(call, response)
        source?.cacheHit(call, response)
    }

    override fun cacheMiss(call: Call) {
        host?.cacheMiss(call)
        source?.cacheMiss(call)
    }

    override fun callEnd(call: Call) {
        host?.callEnd(call)
        source?.callEnd(call)
    }

    override fun callFailed(call: Call, ioe: IOException) {
        host?.callFailed(call, ioe)
        source?.callFailed(call, ioe)
    }

    override fun callStart(call: Call) {
        host?.callStart(call)
        source?.callStart(call)
    }

    override fun canceled(call: Call) {
        host?.canceled(call)
        source?.canceled(call)
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        host?.connectEnd(call, inetSocketAddress, proxy, protocol)
        source?.connectEnd(call, inetSocketAddress, proxy, protocol)
    }

    override fun connectFailed(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?, ioe: IOException) {
        host?.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
        source?.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        host?.connectStart(call, inetSocketAddress, proxy)
        source?.connectStart(call, inetSocketAddress, proxy)
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        host?.connectionAcquired(call, connection)
        source?.connectionAcquired(call, connection)
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        host?.connectionReleased(call, connection)
        source?.connectionReleased(call, connection)
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: MutableList<InetAddress>) {
        host?.dnsEnd(call, domainName, inetAddressList)
        source?.dnsEnd(call, domainName, inetAddressList)
    }

    override fun dnsStart(call: Call, domainName: String) {
        host?.dnsStart(call, domainName)
        source?.dnsStart(call, domainName)
    }

    override fun proxySelectEnd(call: Call, url: HttpUrl, proxies: MutableList<Proxy>) {
        host?.proxySelectEnd(call, url, proxies)
        source?.proxySelectEnd(call, url, proxies)
    }

    override fun proxySelectStart(call: Call, url: HttpUrl) {
        host?.proxySelectStart(call, url)
        source?.proxySelectStart(call, url)
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        host?.requestBodyEnd(call, byteCount)
        source?.requestBodyEnd(call, byteCount)
    }

    override fun requestBodyStart(call: Call) {
        host?.requestBodyStart(call)
        source?.requestBodyStart(call)
    }

    override fun requestFailed(call: Call, ioe: IOException) {
        host?.requestFailed(call, ioe)
        source?.requestFailed(call, ioe)
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        host?.requestHeadersEnd(call, request)
        source?.requestHeadersEnd(call, request)
    }

    override fun requestHeadersStart(call: Call) {
        host?.requestHeadersStart(call)
        source?.requestHeadersStart(call)
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        host?.responseBodyEnd(call, byteCount)
        source?.responseBodyEnd(call, byteCount)
    }

    override fun responseBodyStart(call: Call) {
        host?.responseBodyStart(call)
        source?.responseBodyStart(call)
    }

    override fun responseFailed(call: Call, ioe: IOException) {
        host?.responseFailed(call, ioe)
        source?.responseFailed(call, ioe)
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        host?.responseHeadersEnd(call, response)
        source?.responseHeadersEnd(call, response)
    }

    override fun responseHeadersStart(call: Call) {
        host?.responseHeadersStart(call)
        source?.responseHeadersStart(call)
    }

    override fun satisfactionFailure(call: Call, response: Response) {
        host?.satisfactionFailure(call, response)
        source?.satisfactionFailure(call, response)
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        host?.secureConnectEnd(call, handshake)
        source?.secureConnectEnd(call, handshake)
    }

    override fun secureConnectStart(call: Call) {
        host?.secureConnectStart(call)
        source?.secureConnectStart(call)
    }
}