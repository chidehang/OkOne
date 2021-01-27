package com.cdh.okonedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cdh.okone.OkOne
import com.cdh.okone.connection.callback.PreConnectCallback
import okhttp3.*
import okhttp3.EventListener
import okhttp3.internal.connection.RealCall
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.btn_build_client_1).setOnClickListener(this)
        findViewById<View>(R.id.btn_build_client_2).setOnClickListener(this)
        findViewById<View>(R.id.btn_pre_connect_stackoverflow).setOnClickListener(this)
        findViewById<View>(R.id.btn_pre_connect_juejin).setOnClickListener(this)
        findViewById<View>(R.id.btn_pre_connect_zhihu).setOnClickListener(this)
        findViewById<View>(R.id.btn_build_client_3).setOnClickListener(this)
        findViewById<View>(R.id.btn_test_priority).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_build_client_1 -> {
                val builder1 = createBuilder1()
                testRequestServer(builder1)
            }
            R.id.btn_build_client_2 -> {
                val builder2 = createBuilder2()
                testRequestServer(builder2)
            }
            R.id.btn_build_client_3 -> {
                val builder3 = createBuilder3()
                testRequestServer(builder3)
            }
            R.id.btn_pre_connect_stackoverflow -> testPreBuildConnection(createBuilder3(), URL_FOR_TEST)
            R.id.btn_pre_connect_juejin -> testPreBuildConnection(createBuilder3(), URL_JUEJIN)
            R.id.btn_pre_connect_zhihu -> testPreBuildConnection(createBuilder3(), URL_ZHIHU)
            R.id.btn_test_priority -> testCallPriority()
        }
    }

    /**
     * 创建配方一
     */
    private fun createBuilder1(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor())
                .eventListener(mEventListener)
    }

    /**
     * 创建配方二
     */
    private fun createBuilder2(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048)
                .eventListener(mEventListener)
    }

    /**
     * 创建配方三
     */
    private fun createBuilder3(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor())
                .retryOnConnectionFailure(true)
                .minWebSocketMessageToCompress(2048)
                .eventListener(mEventListener)
    }

    /**
     * 测试预建连
     */
    private fun testPreBuildConnection(builder: OkHttpClient.Builder, url: String) {
        OkOne.preBuildConnection(builder.build(), url, object : PreConnectCallback {
            override fun connectCompleted(url: String) {
                Log.d(TAG, "预建连成功: $url")
            }

            override fun connectFailed(t: Throwable) {
                Log.e(TAG, "预建连失败", t)
            }
        })
    }

    /**
     * 测试不同配方的接口请求
     */
    private fun testRequestServer(builder: OkHttpClient.Builder) {
        val client = builder.build()
        Log.d(TAG, "创建OkHttpClient: $client")
        val api = URL_FOR_TEST
        val request = Request.Builder()
                .url(api)
                .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure() called with: e = [$e]")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "onResponse() called with: response = [$response]")
            }
        })
    }

    private val mEventListener: EventListener = object : EventListener() {
        val TAG = "okflow"
        override fun callStart(call: Call) {
            Log.d(TAG, "callStart-> " + (call as RealCall).request().toString())
        }

        override fun dnsStart(call: Call, domainName: String) {
            Log.d(TAG, "dnsStart-> domainName = [$domainName]")
        }

        override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
            Log.d(TAG, "dnsEnd-> inetAddressList = [$inetAddressList]")
        }

        override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
            Log.d(TAG, "connectStart-> inetSocketAddress = [$inetSocketAddress], proxy = [$proxy]")
        }

        override fun secureConnectStart(call: Call) {
            Log.d(TAG, "secureConnectStart->")
        }

        override fun secureConnectEnd(call: Call, handshake: Handshake?) {
            Log.d(TAG, "secureConnectEnd-> handshake = [$handshake]")
        }

        override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
            Log.d(TAG, "connectEnd->  protocol = [$protocol]")
        }

        override fun connectFailed(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?, ioe: IOException) {
            Log.d(TAG, "connectFailed-> protocol = [$protocol], ioe = [$ioe]")
        }

        override fun connectionAcquired(call: Call, connection: Connection) {
            Log.d(TAG, "connectionAcquired-> connection = [$connection]")
        }

        override fun connectionReleased(call: Call, connection: Connection) {
            Log.d(TAG, "connectionReleased-> connection = [$connection]")
        }

        override fun requestHeadersStart(call: Call) {
            Log.d(TAG, "requestHeadersStart->")
        }

        override fun requestHeadersEnd(call: Call, request: Request) {
            Log.d(TAG, "requestHeadersEnd-> request = [$request]")
        }

        override fun requestBodyStart(call: Call) {
            Log.d(TAG, "requestBodyStart->")
        }

        override fun requestBodyEnd(call: Call, byteCount: Long) {
            Log.d(TAG, "requestBodyEnd-> byteCount = [$byteCount]")
        }

        override fun requestFailed(call: Call, ioe: IOException) {
            Log.d(TAG, "requestFailed-> ioe = [$ioe]")
        }

        override fun responseHeadersStart(call: Call) {
            Log.d(TAG, "responseHeadersStart->")
        }

        override fun responseHeadersEnd(call: Call, response: Response) {
            Log.d(TAG, "responseHeadersEnd-> response = [$response]")
        }

        override fun responseBodyStart(call: Call) {
            Log.d(TAG, "responseBodyStart->")
        }

        override fun responseBodyEnd(call: Call, byteCount: Long) {
            Log.d(TAG, "responseBodyEnd-> byteCount = [$byteCount]")
        }

        override fun responseFailed(call: Call, ioe: IOException) {
            Log.d(TAG, "responseFailed-> ioe = [$ioe]")
        }

        override fun callEnd(call: Call) {
            Log.d(TAG, "callEnd->")
        }

        override fun callFailed(call: Call, ioe: IOException) {
            Log.d(TAG, "callFailed-> ioe = [$ioe]")
        }
    }

    private fun testCallPriority() {
        val TAG = "RequestPriority"

        val N = 10
        val requests = arrayOfNulls<Request>(N)
        val r = Random(System.currentTimeMillis())
        for (i in 0 until N) {
            // 随机设置请求优先级
            val priority = r.nextInt(20) - 10
            Log.d(TAG, "$i => $priority")
            requests[i] = Request.Builder()
                    .url(URL_FOR_TEST)
                    .tag(TagEntity(i + 1, priority))
                    .build()
            OkOne.setRequestPriority(requests[i], priority)
        }

        val client = OkHttpClient.Builder().eventListener(object : EventListener() {
            override fun requestHeadersStart(call: Call) {
                val tag = call.request().tag() as TagEntity?
                Log.d(TAG, "requestHeadersStart: $tag")
            }
        }).build()
        // 设置最大请求数为1，仅为了方便验证
        client.dispatcher.maxRequests = 1

        for (i in 0 until N) {
            client.newCall(requests[i]!!).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                }
            })
        }
    }

    private class TagEntity(var id: Int, var priority: Int) {
        override fun toString(): String {
            return "TagEntity{" +
                    "id=" + id +
                    ", priority=" + priority +
                    '}'
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val URL_FOR_TEST = "https://stackoverflow.com/"
        private const val URL_JUEJIN = "https://juejin.cn/"
        private const val URL_ZHIHU = "https://www.zhihu.com/"
    }
}