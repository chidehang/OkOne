package com.cdh.okone;

import com.cdh.okone.util.LogUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by chidehang on 2020/11/24
 */
public class GlobalOkHttpClientManager {

    private static final boolean DEBUG = LogUtils.isEnabled;
    private static final String TAG = "GlobalOkHttpClientManager";

    private static HashMap<OkHttpClient.Builder, OkHttpClient> sOkHttpClientCache = new HashMap<>();

    private GlobalOkHttpClientManager() {
    }

    public static GlobalOkHttpClientManager getInstance() {
        return InnerHolder.INSTANCE;
    }

    private static final class InnerHolder {
        private static final GlobalOkHttpClientManager INSTANCE = new GlobalOkHttpClientManager();
    }

    public OkHttpClient buildOkHttpClient(OkHttpClient.Builder builder) {
        if (DEBUG) LogUtils.d(TAG, "buildOkHttpClient() called with: builder = [" + builder + "]");
        if (!OkOne.useGlobalClient) {
            return new OkHttpClient(builder);
        }

        OkHttpClient client;
        synchronized (sOkHttpClientCache) {
            // 查找可复用的缓存的OkHttpClient
            client = retrieveOkHttpClient(builder);
            if (client == null) {
                LogUtils.d(TAG, "未命中缓存，新建OkHttpClient");
                // 未找到则新建，添加进缓存
                client = new OkHttpClient(builder);
                sOkHttpClientCache.put(builder, client);
            } else {
                LogUtils.d(TAG, "命中缓存，可复用OkHttpClient");
            }
        }
        return client;
    }

    /**
     * 和缓存实例比较，获取可复用的OkHttpClient
     */
    private OkHttpClient retrieveOkHttpClient(OkHttpClient.Builder builder) {
        synchronized (sOkHttpClientCache) {
            try {
                Iterator iterator = sOkHttpClientCache.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<OkHttpClient.Builder, OkHttpClient> entry = (Map.Entry<OkHttpClient.Builder, OkHttpClient>) iterator.next();
                    // 进行比较
                    if (builder.equivalentTo(entry.getKey())) {
                        // 比较结果相等，返回复用实例
                        return entry.getValue();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }
}
