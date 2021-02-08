package help;

import okhttp3.OkHttpClient;

/**
 * Created by chidehang on 2021/2/4
 */
public class AsmDiff {

    public boolean equivalentTo(OkHttpClient.Builder other) {
        return com.cdh.okone.InjectHelper.BuilderHooker.injectBuilderEquivalentTo(null, other);
    }
}
