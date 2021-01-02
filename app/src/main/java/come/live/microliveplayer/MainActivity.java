package come.live.microliveplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import come.live.decodelib.MsgCenterMgr;
import come.live.decodelib.VideoSizeChangeListener;
import come.live.decodelib.utils.LogUtils;

public class MainActivity extends BaseActivity implements VideoSizeChangeListener {

    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MsgCenterMgr msgCenterMgr;
    private final int mWidth = 720;
    private final int mHeight = 1280;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void initView(){
        surfaceView = findViewById(R.id.surfaceView1);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                msgCenterMgr = new MsgCenterMgr();
                msgCenterMgr.setVideoSizeChangeListener(MainActivity.this);
                msgCenterMgr.setConfig(mWidth,mHeight,holder.getSurface());
                msgCenterMgr.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtils.v("width:"+width+" height:"+height);
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(msgCenterMgr != null){
                    msgCenterMgr.shutDown();
                    msgCenterMgr = null;
                }
            }
        });

        findViewById(R.id.btn_switch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.v("开始切换屏幕");
                int videoWidth = 1280;
                int videoHeight = 720;
                changeVideoSize(videoWidth,videoHeight);
            }
        });

        adjustScreenSize();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(msgCenterMgr != null){
            msgCenterMgr.shutDown();
            msgCenterMgr = null;
        }
    }

    private void adjustScreenSize() {

        DisplayMetrics dm = getScreenInfo(this);
        if (dm != null) {
            int widthPixels = dm.widthPixels;
            int heightPixels = dm.heightPixels;
            if (widthPixels > heightPixels) {
                LogUtils.v("当前屏幕为横屏模式,因为发送端默认为竖屏模式，需要调整");
                changeVideoSize(heightPixels,widthPixels);
            }else {
                changeVideoSize(widthPixels,heightPixels);
            }
        }

    }


    /**
     * 修改预览View的大小,以用来适配屏幕
     */
    public void changeVideoSize(int videoWidth, int videoHeight) {
        int deviceWidth = getResources().getDisplayMetrics().widthPixels;
        int deviceHeight = getResources().getDisplayMetrics().heightPixels;
        float devicePercent = 0;
        LogUtils.v("切换尺寸");
        //下面进行求屏幕比例,因为横竖屏会改变屏幕宽度值,所以为了保持更小的值除更大的值.
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) { //竖屏
            devicePercent = (float) deviceWidth / (float) deviceHeight; //竖屏状态下宽度小与高度,求比
        }else { //横屏
            devicePercent = (float) deviceHeight / (float) deviceWidth; //横屏状态下高度小与宽度,求比
        }

        if (videoWidth > videoHeight){ //判断视频的宽大于高,那么我们就优先满足视频的宽度铺满屏幕的宽度,然后在按比例求出合适比例的高度
            videoWidth = deviceWidth;//将视频宽度等于设备宽度,让视频的宽铺满屏幕
            videoHeight = (int)(deviceWidth*devicePercent);//设置了视频宽度后,在按比例算出视频高度

        }else {  //判断视频的高大于宽,那么我们就优先满足视频的高度铺满屏幕的高度,然后在按比例求出合适比例的宽度
            if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {//竖屏
                videoHeight = deviceHeight;
                /**
                 * 接受在宽度的轻微拉伸来满足视频铺满屏幕的优化
                 */
                float videoPercent = (float) videoWidth / (float) videoHeight;//求视频比例 注意是宽除高 与 上面的devicePercent 保持一致
                float differenceValue = Math.abs(videoPercent - devicePercent);//相减求绝对值
                if (differenceValue < 0.3){ //如果小于0.3比例,那么就放弃按比例计算宽度直接使用屏幕宽度
                    videoWidth = deviceWidth;
                }else {
                    videoWidth = (int)(videoWidth/devicePercent);//注意这里是用视频宽度来除
                }

            }else { //横屏
                videoHeight = deviceHeight;
                videoWidth = (int)(deviceHeight*devicePercent);

            }

        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        layoutParams.width = videoWidth;
        layoutParams.height = videoHeight;
        surfaceView.setLayoutParams(layoutParams);

    }

    @Override
    public void onVideoSizeChange(final int width, final int height) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DisplayMetrics dm = getScreenInfo(MainActivity.this);
                if (dm != null) {
                    int widthPixels = dm.widthPixels;
                    int heightPixels = dm.heightPixels;
                    if (width > height) {
                        LogUtils.v("当前屏幕为横屏模式,因为发送端默认为竖屏模式，需要调整");
                        changeVideoSize(widthPixels,heightPixels);
                    }else {
                        changeVideoSize(heightPixels,widthPixels);
                    }
                }
            }
        });
    }

    public DisplayMetrics getScreenInfo(Context context) {
        if (context instanceof Activity) {
            DisplayMetrics dm = new DisplayMetrics();
            Activity activity = (Activity)context;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            return dm;
        }
        return null;
    }
}