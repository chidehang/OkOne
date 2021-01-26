package okhttp3;

import java.io.IOException;

import okio.Timeout;

/**
 * Created by chidehang on 2021/1/25
 */
public interface Call extends Cloneable {

    Request request();

    Response execute() throws IOException;

    void enqueue(Callback callback);

    void cancel();

    boolean isExecuted();

    boolean isCanceled();

    Timeout timeout();

    Call clone();

    public interface Factory {
        Call newCall(Request request);
    }
}
