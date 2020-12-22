package com.cdh.okone.connection;

import com.cdh.okone.connection.callback.PreConnectCallback;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Address;
import okhttp3.CertificatePinner;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.RealConnectionPool;
import okhttp3.internal.connection.RouteSelector;

/**
 * Created by chidehang on 2020/12/22
 */
public class PreConnectRunnable implements Runnable {

    private static final String THREAD_NAME_PREFIX = "pre-connect-";

    private OkHttpClient mClient;
    private String mUrl;
    private PreConnectCallback mPreConnectCallback;

    public PreConnectRunnable(OkHttpClient client, String url, PreConnectCallback preConnectCallback) {
        this.mClient = client;
        this.mUrl = url;
        this.mPreConnectCallback = preConnectCallback;
    }

    @Override
    public void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(THREAD_NAME_PREFIX + mUrl);
        innerRun();
        Thread.currentThread().setName(oldName);
    }

    private void innerRun() {
        try {
            HttpUrl httpUrl = createHttpUrl(mUrl);
            if (httpUrl == null) {
                callConnectFailed(mPreConnectCallback, new IllegalArgumentException("unexpected url: " + mUrl));
                return;
            }

            Address address = createAddress(mClient, httpUrl);
            List<Route> routes = selectRoutes(mClient, address);
            if (routes == null || routes.size() == 0) {
                callConnectFailed(mPreConnectCallback, new IOException("No route available."));
                return;
            }

            RealConnectionPool realConnectionPool = findRealConnectionPool(mClient);
            if (hasPooledConnection(realConnectionPool, address, routes, false)) {
                callConnectFailed(mPreConnectCallback, new IllegalStateException("There is already a connection with the same address."));
                return;
            }

            Route route = routes.get(0);
            RealConnection connection = new RealConnection(realConnectionPool, route);
            // 开始连接，如果失败，内部将抛异常
            connection.connect(
                    mClient.connectTimeoutMillis(),
                    mClient.readTimeoutMillis(),
                    mClient.writeTimeoutMillis(),
                    mClient.pingIntervalMillis(),
                    false,
                    null,
                    EventListener.NONE
            );
            mClient.getRouteDatabase().connected(connection.route());

            if (hasPooledConnection(realConnectionPool, address, routes, true)) {
                try {
                    connection.socket().close();
                } catch (Throwable t) {
                }
                callConnectFailed(mPreConnectCallback, new IllegalStateException("There is already a connection with the same address."));
            }

            synchronized (connection) {
                realConnectionPool.put(connection);
            }

            callConnectCompleted(mPreConnectCallback, mUrl);
        } catch (Throwable t) {
            t.printStackTrace();
            callConnectFailed(mPreConnectCallback, t);
        }
    }

    private static HttpUrl createHttpUrl(String url) {
        if (url.regionMatches(true, 0, "ws:", 0, 3)) {
            url = "http:" + url.substring(3);
        } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
            url = "https:" + url.substring(4);
        }
        return HttpUrl.parse(url);
    }

    private static Address createAddress(OkHttpClient client, HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = client.sslSocketFactory();
            hostnameVerifier = client.hostnameVerifier();
            certificatePinner = client.certificatePinner();
        }
        return new Address(
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
        );
    }

    private static List<Route> selectRoutes(OkHttpClient client, Address address) throws IOException {
        RouteSelector routeSelector = new RouteSelector(address, client.getRouteDatabase(), null, EventListener.NONE);
        RouteSelector.Selection selection = routeSelector.hasNext()? routeSelector.next() : null;
        return selection == null? null : selection.getRoutes();
    }

    /**
     * 检查是否已经有缓存的相同地址的连接
     */
    private static boolean hasPooledConnection(RealConnectionPool realConnectionPool, Address address, List<Route> routes, boolean requireMultiplexed) {
        try {
            Field connectionsField = realConnectionPool.getClass().getField("connections");
            connectionsField.setAccessible(true);
            ConcurrentLinkedQueue<RealConnection> connections = (ConcurrentLinkedQueue<RealConnection>) connectionsField.get(realConnectionPool);

            // 遍历现存connection
            for (RealConnection connection : connections) {
                synchronized (connection) {
                    if (requireMultiplexed) {
                        Field field = connection.getClass().getDeclaredField("isMultiplexed");
                        field.setAccessible(true);
                        boolean value = (boolean) field.get(connection);
                        if (!value) {
                            continue;
                        }
                    }

                    Method method = connection.getClass().getDeclaredMethod("isEligible", Address.class, List.class);
                    method.setAccessible(true);
                    boolean result = (boolean) method.invoke(connection, address, routes);
                    if (!result) {
                        continue;
                    }

                    // 已经存在connection
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Does not support the current version of okhttp.");
        }
    }

    private static RealConnectionPool findRealConnectionPool(OkHttpClient client) throws NoSuchFieldException, IllegalAccessException {
        Field delegateField = client.connectionPool().getClass().getDeclaredField("delegate");
        delegateField.setAccessible(true);
        RealConnectionPool delegate = (RealConnectionPool) delegateField.get(client.connectionPool());
        return delegate;
    }

    private static void callConnectCompleted(PreConnectCallback callback, String url) {
        if (callback != null) {
            callback.connectCompleted(url);
        }
    }

    private static void callConnectFailed(PreConnectCallback callback, Throwable t) {
        if (callback != null) {
            callback.connectFailed(t);
        }
    }
}
