package com.cdh.okone;

import android.util.Log;

import com.cdh.okone.util.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by chidehang on 2020/12/17
 */
public class InjectHelper {

    private static final String TAG = "InjectHelper";

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
            HashMap<String, Object[]> map = findOConfigMapField(builder);
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
        HashMap<String, Object[]> selfMap = findOConfigMapField(self);
        HashMap<String, Object[]> otherMap = findOConfigMapField(other);
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
        Iterator iterator = selfMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object[]> entry = (Map.Entry<String, Object[]>) iterator.next();
            Object[] otherObjs = otherMap.get(entry.getKey());
            if (entry.getValue() == otherObjs) {
                continue;
            }

            if (entry.getValue() == null || otherObjs == null) {
                return false;
            }
            if (entry.getValue().length != otherObjs.length) {
                return false;
            }

            // 遍历比较每个配置项的属性是否一样
            for (int i = 0; i < otherObjs.length; i++) {
                Object obj1 = entry.getValue()[i];
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
    private static HashMap<String, Object[]> findOConfigMapField(OkHttpClient.Builder builder) {
        try {
            Field field = builder.getClass().getDeclaredField("oConfigMap");
            field.setAccessible(true);
            HashMap<String, Object[]> map = (HashMap<String, Object[]>) field.get(builder);
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
