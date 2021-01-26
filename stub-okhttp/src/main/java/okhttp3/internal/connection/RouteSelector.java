package okhttp3.internal.connection;

import java.io.IOException;
import java.util.List;

import okhttp3.Address;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Route;

/**
 * Created by chidehang on 2021/1/26
 */
public class RouteSelector {

    public RouteSelector(Address address, RouteDatabase routeDatabase, Call call, EventListener eventListener) {
        throw new RuntimeException("Stub!");
    }

    public boolean hasNext() {
        throw new RuntimeException("Stub!");
    }

    public Selection next() throws IOException {
        throw new RuntimeException("Stub!");
    }

    public static final class Selection {
        public List<Route> getRoutes() {
            throw new RuntimeException("Stub!");
        }
    }
}
