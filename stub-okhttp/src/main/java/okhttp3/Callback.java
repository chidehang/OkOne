package okhttp3;

import java.io.IOException;

/**
 * Created by chidehang on 2021/1/25
 */
public interface Callback {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response response) throws IOException;
}
