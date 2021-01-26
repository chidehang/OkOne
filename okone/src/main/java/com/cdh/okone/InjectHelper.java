package com.cdh.okone;

import android.util.Log;

import com.cdh.okone.priority.PriorityArrayDeque;
import com.cdh.okone.priority.RequestPriorityProcessor;
import com.cdh.okone.util.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.internal.connection.RealCall;

/**
 * Created by chidehang on 2020/12/17
 */
public class InjectHelper {

    private static final String TAG = "InjectHelper";

    public static final class BuilderHooker {
        /**
         * 在Builder的所有配置方法体开头拦截
         */
        public static void hookBuilderSetConfig(OkHttpClient.Builder builder, String methodName, Object arg1) {
            recordBuilderConfig(builder, methodName, arg1);
        }

        public static void hookBuilderSetConfig(OkHttpClient.Builder builder, String methodName, boolean arg1) {
            recordBuilderConfig(builder, methodName, arg1);
        }

        public static void hookBuilderSetConfig(OkHttpClient.Builder builder, String methodName, Object arg1, Object arg2) {
            recordBuilderConfig(builder, methodName, arg1, arg2);
        }

        public static void hookBuilderSetConfig(OkHttpClient.Builder builder, String methodName, long arg1, Object arg2) {
            recordBuilderConfig(builder, methodName, arg1, arg2);
        }

        public static void hookBuilderSetConfig(OkHttpClient.Builder builder, String methodName, long arg1) {
            recordBuilderConfig(builder, methodName, arg1);
        }

        /**
         * 记录配置项
         */
        private static void recordBuilderConfig(OkHttpClient.Builder builder, String methodName, Object... args) {
            LogUtils.d(TAG, "recordBuilderConfig() called with: builder = [" + builder + "], methodName = [" + methodName + "], args = [" + Arrays.toString(args) + "]");
            try {
                // 获取oConfigMap成员，用于存储配置项
                TreeMap<String, Object[]> map = findOConfigMapField(builder);
                if (map != null) {
                    if (methodName.startsWith("add") && map.containsKey(methodName)) {
                        // 兼容addInterceptor和addNetworkInterceptor方法
                        Object[] original = map.get(methodName);
                        // 在后面追加
                        ArrayList<Object> array = new ArrayList<>(original.length + args.length);
                        for (Object obj : original) {
                            array.add(obj);
                        }
                        for (Object obj : args) {
                            array.add(obj);
                        }
                        map.put(methodName, array.toArray());
                    } else {
                        // 直接覆盖
                        map.put(methodName, args);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        /**
         * 给Builder注入equivalentTo方法
         */
        public static boolean injectBuilderEquivalentTo(OkHttpClient.Builder self, OkHttpClient.Builder other) {
            if (self == other) {
                return true;
            }
            if (self == null || other == null) {
                return false;
            }
            if (self.equals(other)) {
                return true;
            }

            // 比较两者的oConfigMap的成员
            TreeMap<String, Object[]> selfMap = findOConfigMapField(self);
            TreeMap<String, Object[]> otherMap = findOConfigMapField(other);
            if (selfMap == null || otherMap == null) {
                return false;
            }

            if (selfMap.size() == 0 && otherMap.size() == 0) {
                // 未对Builder自定义配置
                return true;
            }
            if (selfMap.size() != otherMap.size()) {
                return false;
            }

            // 遍历配置项，比较是否进行一样的配置。有一项不一致就返回false。
            Iterator selfIter = selfMap.entrySet().iterator();
            Iterator otherIter = otherMap.entrySet().iterator();
            // 按字典序遍历
            while (selfIter.hasNext() && otherIter.hasNext()) {
                Map.Entry<String, Object[]> selfEntry = (Map.Entry<String, Object[]>) selfIter.next();
                Map.Entry<String, Object[]> otherEntry = (Map.Entry<String, Object[]>) otherIter.next();
                if (!selfEntry.getKey().equals(otherEntry.getKey())) {
                    // 方法名称不同
                    return false;
                }

                Object[] otherObjs = otherEntry.getValue();
                if (selfEntry.getValue() == otherObjs) {
                    continue;
                }

                if (selfEntry.getValue() == null || otherObjs == null) {
                    return false;
                }
                if (selfEntry.getValue().length != otherObjs.length) {
                    return false;
                }

                // 遍历比较每个配置项的属性是否一样
                for (int i = 0; i < otherObjs.length; i++) {
                    Object obj1 = selfEntry.getValue()[i];
                    Object obj2 = otherObjs[i];

                    if (obj1 == obj2) {
                        continue;
                    }
                    if (obj1 == null || obj2 == null) {
                        return false;
                    }
                    if (obj1.equals(obj2)) {
                        continue;
                    }
                    if (!obj1.getClass().getName().equals(obj2.getClass().getName())) {
                        // 比较是否是同一个类（可以是不同实例）
                        return false;
                    }
                }
            }

            return true;
        }

        /**
         * 反射获取插入的oConfigMap成员
         */
        private static TreeMap<String, Object[]> findOConfigMapField(OkHttpClient.Builder builder) {
            try {
                TreeMap<String, Object[]> map = (TreeMap<String, Object[]>) builder.oConfigMap;
                return map;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        /**
         * hook Builder#build方法
         */
        public static OkHttpClient hookBuildOkHttpClient(OkHttpClient.Builder builder) {
            return GlobalOkHttpClientManager.getInstance().buildOkHttpClient(builder);
        }
    }

    public static final class AsyncCallHooker {
        public static int hookCompareTo(Object selfObj, Object otherObj) {
            try {
                if (selfObj instanceof RealCall.AsyncCall && otherObj instanceof RealCall.AsyncCall) {
                    RealCall.AsyncCall selfCall = (RealCall.AsyncCall) selfObj;
                    RealCall.AsyncCall otherCall = (RealCall.AsyncCall) otherObj;
                    Request self = selfCall.getRequest();
                    Request other = otherCall.getRequest();
                    int sp = (int) self.priority;
                    int op = (int) other.priority;
                    return sp - op;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return 0;
        }
    }

    public static final class DispatcherHooker {
        public static ArrayDeque hookReadyAsyncCalls() {
            if (RequestPriorityProcessor.enableRequestPriority) {
                return new PriorityArrayDeque<>();
            } else {
                return new ArrayDeque<>();
            }
        }
    }
}
