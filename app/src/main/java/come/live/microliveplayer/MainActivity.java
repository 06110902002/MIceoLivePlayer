package come.live.microliveplayer;

import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.RequiresApi;
import come.live.decodelib.MsgCenterMgr;

public class MainActivity extends BaseActivity {

    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MsgCenterMgr msgCenterMgr;
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
                int mWidth = 1280;
                int mHeight = 720;
                msgCenterMgr.setConfig(mWidth,mHeight,holder.getSurface());
                msgCenterMgr.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(msgCenterMgr != null){
            msgCenterMgr.shutDown();
            msgCenterMgr = null;
        }
    }
}