package come.live.microliveplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import come.live.decodelib.MsgCenterMgr;
import come.live.decodelib.DataParseListener;
import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.UIUtils;
import come.live.microliveplayer.config.Constants;
import come.live.microliveplayer.list.AppAdapter;
import come.live.microliveplayer.list.AppPageAdapter;
import come.live.microliveplayer.list.VerticalSpaceItemDecoration;
import come.live.microliveplayer.list.model.AppItemInfo;
import come.live.microliveplayer.list.model.AppPageItem;
import come.live.microliveplayer.sdp.AppItemSdp;

public class MainActivity extends BaseActivity implements DataParseListener {

    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder mSurfaceHolder2;
    private final int mWidth = 720;
    private final int mHeight = 1280;
    private int screenWidth;
    private int screenHeight;
    private final int CONFIG_MEDIACODEC = 1102;
    private final int UPDATE_SURFACE = 1103;
    private MainHandler mainHandler;
    private RelativeLayout layout2;
    private RelativeLayout layout1;
    private Button btn1;
    private Button btn2;
    private ImageView imgTest;
    protected RecyclerView mRightMenuList;
    protected RecyclerView mAppPageList;
    protected AppAdapter rightMenuAdapter;
    protected AppPageAdapter appPageListAdapter;
    private  int pageCount = 1;
    private MainBroadcast mReceiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        mReceiver = new MainBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.PAGE_IDX_ACTION);
        registerReceiver(mReceiver,intentFilter);
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
                surfaceView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        MsgCenterMgr.getInstance().sendEvent(event,surfaceView.getWidth(), surfaceView.getHeight(), surfaceView.getWidth(), surfaceView.getHeight(),1);
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
            }
        });

        layout2 = findViewById(R.id.layout2);
        layout1 = findViewById(R.id.layout1);
        surfaceView2 = findViewById(R.id.surfaceView2);
        mSurfaceHolder2 = surfaceView2.getHolder();
        mSurfaceHolder2.addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                surfaceView2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        MsgCenterMgr.getInstance().sendEvent(event,surfaceView2.getWidth(), surfaceView2.getHeight(),
                                surfaceView2.getWidth(), surfaceView2.getHeight(),0);
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
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MsgCenterMgr.getInstance().setVideoSizeChangeListener(MainActivity.this);
                int width1 = dp2px(MainActivity.this,360);
                int height1 = screenHeight -  dp2px(MainActivity.this,50) * 2;
                MsgCenterMgr.getInstance().setConfig(width1,height1,
                        surfaceView.getHolder().getSurface(),
                        2560,1504,
                        surfaceView2.getHolder().getSurface());
                MsgCenterMgr.getInstance().setConnectListener(new MsgCenterMgr.ConnectListener() {
                    @Override
                    public void onStatus(int code, final String msg) {
                        if (code == MsgCenterMgr.CONNECT_SUCCESS) {

                        } else if (code == MsgCenterMgr.DISCONNECTED) {
                            updateLayoutAttribute(1,0,0,1,1);
                            updateLayoutAttribute(0,0,0,1,1);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    MsgCenterMgr.getInstance().start();
                }
            }
        },1000);

        btn1 = findViewById(R.id.btn_back1);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MsgCenterMgr.getInstance().sendKeyCode(4,1);
            }
        });
        btn2 = findViewById(R.id.btn_back2);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MsgCenterMgr.getInstance().sendKeyCode(4,0);
            }
        });
        findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                updateLayoutAttribute(1,0,0,1,1);
            }
        });
        findViewById(R.id.btn_exit2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                updateLayoutAttribute(0,0,0,1,1);
            }
        });

        mRightMenuList = findViewById(R.id.right_menu_list);
        rightMenuAdapter = new AppAdapter(this);
        mRightMenuList.setLayoutManager(new LinearLayoutManager(this));
        mRightMenuList.setAdapter(rightMenuAdapter);
        VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration();
        int screenHeight = UIUtils.getScreenHeight(this) - UIUtils.dp2px(this,50) * 2;
        int itemHeight = UIUtils.dp2px(this,120);
        verticalSpaceItemDecoration.setScreenWidthHeight(screenHeight,itemHeight);
        mRightMenuList.addItemDecoration(verticalSpaceItemDecoration);

        mAppPageList = findViewById(R.id.app_remote_page_list);
        appPageListAdapter = new AppPageAdapter(this);
        mAppPageList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mAppPageList.setAdapter(appPageListAdapter);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private class MainHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONFIG_MEDIACODEC:
                    int pageIdx = msg.arg1;
                    MsgCenterMgr.getInstance().startDecodec(pageIdx);
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private class MainBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Constants.PAGE_IDX_ACTION)) {
                int pageIdx = intent.getIntExtra(Constants.PAGE_IDX_KEY,-1);
                if (pageIdx == 0) {
                    updateLayoutAttribute(0,0,0,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    updateLayoutAttribute(1,0,0,1,1);
                } else {
                    int width = dp2px(MainActivity.this,360);
                    int height = screenHeight -  dp2px(MainActivity.this,50) * 2;
                    int leftMargin = dp2px(MainActivity.this,50) +
                            dp2px(MainActivity.this,90) +
                            dp2px(MainActivity.this,50) +
                            dp2px(MainActivity.this,360) * (pageIdx - 1) +
                            dp2px(MainActivity.this,10) * (pageIdx - 1);

                    int topMargin = dp2px(MainActivity.this,50);
                    updateLayoutAttribute(1,leftMargin,topMargin,width,height);
                    updateLayoutAttribute(0,0,0,1,1);
                }
                MsgCenterMgr.getInstance().startDecodec(pageIdx);
            }
        }
    }

    @Override
    public void onVideoSizeChange(String jsonString) {
//        if (pageCount == 1) {
//            updateLayoutAttribute(1,0,0,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
//        }
    }

    @Override
    public void onParseData(int type,String jsonString) {
        if (type == LiveEntity.LAUNCHER_DATA) {
            if (!TextUtils.isEmpty(jsonString)) {
                final AppItemSdp appItemSdp = new Gson().fromJson(jsonString,AppItemSdp.class);
                int width = appItemSdp.getWidth();
                int height = appItemSdp.getHeight();
                int pageIdx = appItemSdp.getPageCount();
                boolean isPad = appItemSdp.isIsPad();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buildLauncherView(appItemSdp.getMenuData(),appItemSdp.getAppInfo().getPage1(),appItemSdp.getAppInfo().getPage2());
//                        Message msg = mainHandler.obtainMessage();
//                        msg.what = CONFIG_MEDIACODEC;
//                        msg.arg1 = pageCount;
//                        mainHandler.sendMessage(msg);
                        Toast.makeText(MainActivity.this,"数据加载成功",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (type == LiveEntity.BACK_TO_PAGE_HOME) {
            updateLayoutAttribute(1,0,0,1,1);
            updateLayoutAttribute(0,0,0,1,1);
        }
    }

    private void updateLayoutAttribute(final int pageIdx, final int leftMargin, final int topMargin , final int width, final int height) {

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pageIdx != 0) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout1.getLayoutParams();
                    layoutParams.width = width;
                    layoutParams.height = height;
                    layoutParams.leftMargin = leftMargin;
                    layoutParams.topMargin = topMargin;
                    layout1.setLayoutParams(layoutParams);
                } else {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout2.getLayoutParams();
                    layoutParams.width = width;
                    layoutParams.height = height;
                    layoutParams.leftMargin = leftMargin;
                    layoutParams.topMargin = topMargin;
                    layout2.setLayoutParams(layoutParams);
                }

            }
        },100);
    }


    private void buildLauncherView(List<AppItemInfo> menuDataDTOList, List<AppItemInfo> page1List, List<AppItemInfo> page2List) {
        if (menuDataDTOList != null && !menuDataDTOList.isEmpty()) {
            rightMenuAdapter.getDataList().clear();
            for (AppItemInfo info : menuDataDTOList) {
                info.setItemType(Constants.RIGHT_MENU_ITEM);
            }
            rightMenuAdapter.addData(menuDataDTOList);
            rightMenuAdapter.notifyDataSetChanged();
        }

        appPageListAdapter.getDataList().clear();
        if (page1List != null && !page1List.isEmpty()) {
            for (AppItemInfo info : page1List) {
                info.setItemType(Constants.APP_LIST_ITEM);
            }
            AppPageItem appPageItem1 = new AppPageItem();
            appPageItem1.setWholePage(false);
            appPageItem1.setAppItemInfo(page1List);
            appPageListAdapter.addData(appPageItem1);
        }
        if (page2List != null && !page2List.isEmpty()) {
            for (AppItemInfo info : page2List) {
                info.setItemType(Constants.APP_LIST_ITEM);
            }
            AppPageItem appPageItem2 = new AppPageItem();
            appPageItem2.setWholePage(false);
            appPageItem2.setAppItemInfo(page2List);
            appPageListAdapter.addData(appPageItem2);

        }
        appPageListAdapter.notifyDataSetChanged();


    }


    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}