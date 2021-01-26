package okhttp3;

import java.io.IOException;

/**
 * Created by chidehang on 2021/1/26
 */
public interface Authenticator {

    Request authenticate(Route route, Response response) throws IOException;
}
