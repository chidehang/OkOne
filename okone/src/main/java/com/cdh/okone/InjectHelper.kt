package com.cdh.okone

import com.cdh.okone.monitor.GlobalEventListenerFactory
import com.cdh.okone.monitor.MonitorRegistry
import com.cdh.okone.priority.PriorityArrayDeque
import com.cdh.okone.priority.RequestPriorityProcessor
import com.cdh.okone.util.LogUtils
import com.cdh.okone.util.LogUtils.d
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.connection.RealCall
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by chidehang on 2021/2/10
 */
object InjectHelper {

    private const val TAG = "InjectHelper"

    object BuilderHooker {

        /**
         * 在Builder的所有配置方法体开头拦截
         */
        @JvmStatic
        fun hookBuilderSetConfig(builder: OkHttpClient.Builder, methodName: String, arg1: Any?) {
            recordBuilderConfig(builder, methodName, arg1)
        }

        @JvmStatic
        fun hookBuilderSetConfig(builder: OkHttpClient.Builder, methodName: String, arg1: Boolean) {
            recordBuilderConfig(builder, methodName, arg1)
        }

        @JvmStatic
        fun hookBuilderSetConfig(builder: OkHttpClient.Builder, methodName: String, arg1: Any?, arg2: Any?) {
            recordBuilderConfig(builder, methodName, arg1, arg2)
        }

        @JvmStatic
        fun hookBuilderSetConfig(builder: OkHttpClient.Builder, methodName: String, arg1: Long, arg2: Any?) {
            recordBuilderConfig(builder, methodName, arg1, arg2)
        }

        @JvmStatic
        fun hookBuilderSetConfig(builder: OkHttpClient.Builder, methodName: String, arg1: Long) {
            recordBuilderConfig(builder, methodName, arg1)
        }

        /**
         * 记录配置项
         */
        private fun recordBuilderConfig(builder: OkHttpClient.Builder, methodName: String, vararg args: Any?) {
            if (LogUtils.isEnabled) d(TAG, "recordBuilderConfig() called with: builder = [" + builder + "], methodName = [" + methodName + "], args = [" + Arrays.toString(args) + "]")
            try {
                // 获取oConfigMap成员，用于存储配置项
                val map = findOConfigMapField(builder)
                if (map != null) {
                    if (methodName.startsWith("add") && map.containsKey(methodName)) {
                        // 兼容addInterceptor和addNetworkInterceptor方法
                        val original = map[methodName]
                        // 在后面追加
                        val array = ArrayList<Any?>(original!!.size + args.size)
                        for (obj in original) {
                            array.add(obj)
                        }
                        for (obj in args) {
                            array.add(obj)
                        }
                        map[methodName] = array.toTypedArray()
                    } else {
                        // 直接覆盖
                        map[methodName] = args
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        /**
         * 给Builder注入equivalentTo方法
         */
        @JvmStatic
        fun injectBuilderEquivalentTo(self: OkHttpClient.Builder?, other: OkHttpClient.Builder?): Boolean {
            if (self === other) {
                return true
            }
            if (self == null || other == null) {
                return false
            }

            // 比较两者的oConfigMap的成员
            val selfMap = findOConfigMapField(self)
            val otherMap = findOConfigMapField(other)
            if (selfMap == null || otherMap == null) {
                return false
            }
            if (selfMap.size == 0 && otherMap.size == 0) {
                // 未对Builder自定义配置
                return true
            }
            if (selfMap.size != otherMap.size) {
                return false
            }

            // 遍历配置项，比较是否进行一样的配置。有一项不一致就返回false。
            val selfIter: Iterator<*> = selfMap.entries.iterator()
            val otherIter: Iterator<*> = otherMap.entries.iterator()
            // 按字典序遍历
            while (selfIter.hasNext() && otherIter.hasNext()) {
                val selfEntry = selfIter.next() as Map.Entry<String, Array<*>?>
                val otherEntry = otherIter.next() as Map.Entry<String, Array<*>?>
                if (selfEntry.key != otherEntry.key) {
                    // 方法名称不同
                    return false
                }

                val otherObjs = otherEntry.value
                if (selfEntry.value === otherObjs) {
                    continue
                }
                if (selfEntry.value == null || otherObjs == null) {
                    return false
                }
                if (selfEntry.value!!.size != otherObjs.size) {
                    return false
                }

                // 遍历比较每个配置项的属性是否一样
                for (i in otherObjs.indices) {
                    val obj1 = selfEntry.value!![i]
                    val obj2 = otherObjs[i]
                    if (obj1 === obj2) {
                        continue
                    }
                    if (obj1 == null || obj2 == null) {
                        return false
                    }
                    if (obj1 == obj2) {
                        continue
                    }
                    if (obj1.javaClass.name != obj2.javaClass.name) {
                        // 比较是否是同一个类（可以是不同实例）
                        return false
                    }
                }
            }
            return true
        }

        /**
         * 获取插入的oConfigMap成员
         */
        private fun findOConfigMapField(builder: OkHttpClient.Builder): TreeMap<String, Array<*>?>? {
            try {
                return builder.okone_configMap as TreeMap<String, Array<*>?>
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return null
        }

        /**
         * hook Builder#build方法
         */
        @JvmStatic
        fun hookBuildOkHttpClient(builder: OkHttpClient.Builder): OkHttpClient {
            return GlobalOkHttpClientManager.getInstance().buildOkHttpClient(builder)
        }
    }

    object AsyncCallHooker {
        @JvmStatic
        fun hookCompareTo(selfObj: Any, otherObj: Any): Int {
            try {
                if (selfObj is RealCall.AsyncCall && otherObj is RealCall.AsyncCall) {
                    val self = selfObj.request
                    val other = otherObj.request
                    return self.okone_priority - other.okone_priority
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return 0
        }
    }

    object DispatcherHooker {
        @JvmStatic
        fun hookReadyAsyncCalls(): ArrayDeque<out Any> {
            return if (RequestPriorityProcessor.enableRequestPriority) {
                PriorityArrayDeque()
            } else {
                ArrayDeque()
            }
        }
    }

    object ClientHooker {
        @JvmStatic
        fun hookEventListenerFactory(source: EventListener.Factory): EventListener.Factory {
            return GlobalEventListenerFactory(source)
        }

        @JvmStatic
        fun hookInterceptors(source: List<Interceptor>): List<Interceptor> {
            // 添加全局Interceptor
            return source + MonitorRegistry.globalInterceptors
        }

        @JvmStatic
        fun hookNetworkInterceptors(source: List<Interceptor>): List<Interceptor> {
            // 添加全局NetworkInterceptor
            return source + MonitorRegistry.globalNetworkInterceptors
        }
    }
}