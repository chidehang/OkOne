package okhttp3.internal.connection;

import java.net.Socket;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.EventListener;
import okhttp3.Route;

/**
 * Created by chidehang on 2021/1/25
 */
public class RealConnection {

    public RealConnection(RealConnectionPool connectionPool, Route route) {
        throw new RuntimeException("Stub!");
    }

    public void connect(int connectTimeout, int readTimeout, int writeTimeout,
                        int pingIntervalMillis, boolean connectionRetryEnabled, Call call,
                        EventListener eventListener) {
        throw new RuntimeException("Stub!");
    }

    public Route route() {
        throw new RuntimeException("Stub!");
    }

    public Socket socket() {
        throw new RuntimeException("Stub!");
    }
}
