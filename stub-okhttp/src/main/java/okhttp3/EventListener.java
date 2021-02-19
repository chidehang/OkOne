package okhttp3;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * Created by chidehang on 2021/1/26
 */
public abstract class EventListener {

    public void cacheConditionalHit(@NotNull Call call, @NotNull Response cachedResponse) {
    }

    
    public void cacheHit(@NotNull Call call, @NotNull Response response) {
    }

    
    public void cacheMiss(@NotNull Call call) {
    }

    
    public void callEnd(@NotNull Call call) {
    }

    
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
    }

    
    public void callStart(@NotNull Call call) {
    }

    
    public void canceled(@NotNull Call call) {
    }

    
    public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
    }

    
    public void connectFailed(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol, @NotNull IOException ioe) {
    }

    
    public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
    }

    
    public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
    }

    
    public void connectionReleased(@NotNull Call call, @NotNull Connection connection) {
    }

    
    public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
    }

    
    public void dnsStart(@NotNull Call call, @NotNull String domainName) {
    }

    
    public void proxySelectEnd(@NotNull Call call, @NotNull HttpUrl url, @NotNull List<Proxy> proxies) {
    }

    
    public void proxySelectStart(@NotNull Call call, @NotNull HttpUrl url) {
    }

    
    public void requestBodyEnd(@NotNull Call call, long byteCount) {
    }

    
    public void requestBodyStart(@NotNull Call call) {
    }

    
    public void requestFailed(@NotNull Call call, @NotNull IOException ioe) {
    }

    
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
    }

    
    public void requestHeadersStart(@NotNull Call call) {
    }

    
    public void responseBodyEnd(@NotNull Call call, long byteCount) {
    }

    
    public void responseBodyStart(@NotNull Call call) {
    }

    
    public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
    }

    
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
    }

    
    public void responseHeadersStart(@NotNull Call call) {
    }

    
    public void satisfactionFailure(@NotNull Call call, @NotNull Response response) {
    }

    
    public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
    }

    
    public void secureConnectStart(@NotNull Call call) {
    }

    public static final EventListener NONE = new EventListener() {
    };

    public interface Factory {
        EventListener create(Call call);
    }
}
