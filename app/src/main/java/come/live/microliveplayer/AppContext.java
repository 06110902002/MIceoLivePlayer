package come.live.microliveplayer;

import android.app.Application;
import android.content.Context;

/**
 * @author hengyang.lxb
 * @date 2020/12/19
 * @Version: 1.0
 * @Description:
 */
public class AppContext extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
