package okhttp3;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.internal.connection.RouteDatabase;

/**
 * Created by chidehang on 2021/1/25
 */
public class OkHttpClient {

    public OkHttpClient(OkHttpClient.Builder builder) {
        throw new RuntimeException("Stub!");
    }

    public ConnectionPool connectionPool() {
        throw new RuntimeException("Stub!");
    }

    public Dispatcher dispatcher() {
        throw new RuntimeException("Stub!");
    }

    public int connectTimeoutMillis() {
        throw new RuntimeException("Stub!");
    }

    public int readTimeoutMillis() {
        throw new RuntimeException("Stub!");
    }

    public int writeTimeoutMillis() {
        throw new RuntimeException("Stub!");
    }

    public int pingIntervalMillis() {
        throw new RuntimeException("Stub!");
    }

    public RouteDatabase getRouteDatabase() {
        throw new RuntimeException("Stub!");
    }

    public SSLSocketFactory sslSocketFactory() {
        throw new RuntimeException("Stub!");
    }

    public HostnameVerifier hostnameVerifier() {
        throw new RuntimeException("Stub!");
    }

    public CertificatePinner certificatePinner() {
        throw new RuntimeException("Stub!");
    }

    public Dns dns() {
        throw new RuntimeException("Stub!");
    }

    public SocketFactory socketFactory() {
        throw new RuntimeException("Stub!");
    }

    public Authenticator proxyAuthenticator() {
        throw new RuntimeException("Stub!");
    }

    public Proxy proxy() {
        throw new RuntimeException("Stub!");
    }

    public List<Protocol> protocols() {
        throw new RuntimeException("Stub!");
    }

    public List<ConnectionSpec> connectionSpecs() {
        throw new RuntimeException("Stub!");
    }

    public ProxySelector proxySelector() {
        throw new RuntimeException("Stub!");
    }

    public class Builder {

        public java.util.TreeMap oConfigMap;

        public boolean equivalentTo(OkHttpClient.Builder builder) {
            throw new RuntimeException("Stub!");
        }
    }
}
