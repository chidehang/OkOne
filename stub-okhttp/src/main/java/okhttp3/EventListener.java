package okhttp3;

/**
 * Created by chidehang on 2021/1/26
 */
public abstract class EventListener {

    public static final EventListener NONE = new EventListener() {
    };

    public interface Factory {
        EventListener create(Call call);
    }
}
