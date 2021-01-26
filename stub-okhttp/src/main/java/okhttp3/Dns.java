package okhttp3;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by chidehang on 2021/1/26
 */
public interface Dns {
    List<InetAddress> lookup(String hostname);
}
