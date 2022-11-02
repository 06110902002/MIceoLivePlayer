package come.live.microliveplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import come.live.decodelib.MsgCenterMgr;
import come.live.decodelib.VideoSizeChangeListener;
import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.UIUtils;
import come.live.microliveplayer.peer.discovering.DevicesBroadcast;

public class MainActivity extends BaseActivity implements VideoSizeChangeListener {

    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder mSurfaceHolder2;
    private MsgCenterMgr msgCenterMgr;
    private final int mWidth = 720;
    private final int mHeight = 1280;
    private int screenWidth;
    private int screenHeight;
    private final int CONFIG_MEDIACODEC = 1102;
    private MainHandler mainHandler;
    private RelativeLayout layout2;
    private Button btn1;
    private Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mainHandler = new MainHandler();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        //LogUtils.v("217--------width = " + screenWidth + " height :" + screenHeight + " displayMetrics.densityDpi :" +displayMetrics.densityDpi);
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

//                msgCenterMgr = new MsgCenterMgr();
//                msgCenterMgr.setVideoSizeChangeListener(MainActivity.this);
//                msgCenterMgr.setConfig(surfaceView.getWidth(),surfaceView.getHeight(),holder.getSurface());
//                msgCenterMgr.start();

                surfaceView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
//                        return msgCenterMgr.touchevent(
//                                event,
//                                surfaceView.getWidth(),
//                                surfaceView.getHeight(),
//                                1280,
//                                1504);
                        msgCenterMgr.sendEvent(event,surfaceView.getWidth(), surfaceView.getHeight(), surfaceView.getWidth(), surfaceView.getHeight(),1);
                        return true;

                    }
                });

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

        layout2 = findViewById(R.id.layout2);
        surfaceView2 = findViewById(R.id.surfaceView2);
        mSurfaceHolder2 = surfaceView2.getHolder();
        mSurfaceHolder2.addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                surfaceView2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

//                        return msgCenterMgr.touchevent2(
//                                event,
//                                surfaceView2.getWidth(),
//                                surfaceView2.getHeight(),
//                                1280,
//                                1504);
                        LogUtils.v("217--------width = " + screenWidth + " height :" + screenHeight +
                                " surfaceView.getWidth() :" +surfaceView.getWidth() + " surfaceView.getHeight():" + surfaceView.getHeight());

                        msgCenterMgr.sendEvent(event,surfaceView.getWidth(), surfaceView.getHeight(), surfaceView.getWidth(), surfaceView.getHeight(),2);
                        return true;

                    }
                });

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


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                msgCenterMgr = new MsgCenterMgr();
                msgCenterMgr.setVideoSizeChangeListener(MainActivity.this);
                msgCenterMgr.setConfig(surfaceView.getWidth(),surfaceView.getHeight(),
                        surfaceView.getHolder().getSurface(),
                        surfaceView2.getHolder().getSurface());
                msgCenterMgr.setConnectListener(new MsgCenterMgr.ConnectListener() {
                    @Override
                    public void onStatus(int code, final String msg) {
                        if (code == MsgCenterMgr.CONNECT_SUCCESS) {

//                            LiveEntity liveEntity = new LiveEntity();
//                            liveEntity.setType(ByteUtil.int2Bytes(100));
//                            int width = surfaceView.getWidth();
//                            int height = surfaceView.getHeight();
//                            String config = width + ":" + height;
//                            LogUtils.v("传给 客户端  width = " + width + " height :" + height);
//                            byte[] content = config.getBytes(StandardCharsets.UTF_8);
//                            liveEntity.setContentLength(ByteUtil.int2Bytes(content.length));
//                            liveEntity.setContent(content);
                            //msgCenterMgr.sendMsg(liveEntity);
                        } else if (code == MsgCenterMgr.DISCONNECTED) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                msgCenterMgr.start();
            }
        },1000);

        btn1 = findViewById(R.id.btn_back1);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                msgCenterMgr.sendKeyCode(4,1);
            }
        });
        btn2 = findViewById(R.id.btn_back2);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                msgCenterMgr.sendKeyCode(4,2);
            }
        });


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

    private class MainHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONFIG_MEDIACODEC:
                    if (msgCenterMgr != null) {
                        int paegCount = msg.arg1;
                        msgCenterMgr.startDecodec(paegCount);
                    }
                    break;
            }
        }
    }

    @Override
    public void onVideoSizeChange(String jsonString) {
        if (!TextUtils.isEmpty(jsonString)) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
                int width = jsonObject.optInt("width");
                int height = jsonObject.optInt("height");
                final int pageCount = jsonObject.optInt("page_count");
                boolean isPad = jsonObject.optBoolean("isPad");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
                        layoutParams.width = pageCount != 1 ? dp2px(MainActivity.this,360) : RelativeLayout.LayoutParams.MATCH_PARENT;
                        layoutParams.height = pageCount != 1 ? dp2px(MainActivity.this,640) : RelativeLayout.LayoutParams.MATCH_PARENT;
                        surfaceView.setLayoutParams(layoutParams);
                        btn1.setVisibility(View.VISIBLE);
                        if (pageCount == 1) {
                            layout2.setVisibility(View.GONE);
                        } else {
                            layout2.setVisibility(View.VISIBLE);
                            btn2.setVisibility(View.VISIBLE);
                            surfaceView2.setLayoutParams(layoutParams);
                        }

                        Message msg = mainHandler.obtainMessage();
                        msg.what = CONFIG_MEDIACODEC;
                        msg.arg1 = pageCount;
                        mainHandler.sendMessage(msg);


                        //回传客户端已经调整好了，可以启动编码
                        LiveEntity liveEntity = new LiveEntity();
                        liveEntity.setType(ByteUtil.int2Bytes(LiveEntity.STRAT_CODEC));
                        int width = surfaceView.getWidth();
                        int height = surfaceView.getHeight();
                        String config = width + ":" + height;
                        LogUtils.v("回传客户端已经调整好了，可以启动编码  width = " + width + " height :" + height);
                        byte[] content = config.getBytes(StandardCharsets.UTF_8);
                        liveEntity.setContentLength(ByteUtil.int2Bytes(content.length));
                        liveEntity.setContent(content);
                        msgCenterMgr.sendMsg(liveEntity);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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
    public static int getScreenWidth(Activity context) {
        WindowManager manager = context.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity context) {
        WindowManager manager = context.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}