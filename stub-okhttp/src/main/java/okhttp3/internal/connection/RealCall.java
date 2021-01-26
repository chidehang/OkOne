package okhttp3.internal.connection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

/**
 * Created by chidehang on 2021/1/25
 */
public class RealCall implements Call {

    @Override
    public Request request() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Response execute() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void enqueue(Callback callback) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void cancel() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean isExecuted() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean isCanceled() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Timeout timeout() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Call clone() {
        throw new RuntimeException("Stub!");
    }

    public class AsyncCall implements Runnable, Comparable {

        public Request getRequest() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public int compareTo(Object o) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public void run() {
            throw new RuntimeException("Stub!");
        }
    }
}
