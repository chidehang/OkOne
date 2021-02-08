package help;

import okhttp3.OkHttpClient;

/**
 * Created by chidehang on 2021/2/4
 */
public class AsmDiff implements Comparable {

    public int compareTo(java.lang.Object o) {
        return com.cdh.okone.InjectHelper.AsyncCallHooker.hookCompareTo(this, o);
    }
}
