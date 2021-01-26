package okhttp3;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by chidehang on 2021/1/25
 */
public class Address {

    public Address(String uriHost, int uriPort, Dns dns, SocketFactory socketFactory,
                   SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                   CertificatePinner certificatePinner, Authenticator proxyAuthenticator,
                   Proxy proxy, List<Protocol> protocols, List<ConnectionSpec> connectionSpecs,
                   ProxySelector proxySelector) {
        throw new RuntimeException("Stub!");
    }
}
