package come.live.decodelib.video;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import come.live.decodelib.VideoSizeChangeListener;
import come.live.decodelib.utils.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author         hengyang.lxb
 * date           2021/01/04
 * Version:       1.0
 * Description:   参考类
 */
public class MirrorContext {

    private static final String TAG = "Mirroring";
    private static final boolean VERBOSE = true;
    private static int mStat;

    private MediaCodec mCodec;
    private SurfaceHolder mDisplay;
    private Surface mSurface;
    private MediaFormat mFormat;

    private ByteBuffer[] mInputBuffers;
    private byte[] spsppsBuffer;
    private byte[] firstFrameBuffer;
    private PlayerThread mThread = null;

    public static final int MIRROR_IDLE = 0;
    public static final int MIRROR_INIT = 1;
    public static final int MIRROR_STOPPED = 2;
    public static final int MIRROR_PLAYING = 3;
    public static final int MIRROR_CODEC_ERROR = 4;

    private boolean mFirstFrame = true;
    private static boolean renderThreadRunning = false;
    private VideoSizeChangeListener mListener;

    private static int  CONFIGURE_FLAG_LOWLAT  = 0x2;//OMX low lantency flag

    private static Handler myHandler;
    private static int downloadSpeed = 0;
    private static int frameRate = 0;
    public static final int MESSAGE_VIDEO_REFRESH_INFO = 0x2;
    public static final int MESSAGE_VIDEO_START_STATUS = 0x3;
    public static final int MIRROR_PLAYER_ERROR_MEDIACODEC_ERR = 0x10010;

    private static int maxFramerate = 0;
    private static int frameCount = 0;
    private static int zeroContinuousTime = 0;
    private static String resoluton = "";

    private boolean isHisiPlatform = false;
    private static boolean mDebug = false;

    public MirrorContext(VideoSizeChangeListener listener, Handler handler,boolean debug) {
        mStat = MIRROR_IDLE;
        mCodec = null;
        mDisplay = null;
        mSurface = null;
        mListener = listener;
        myHandler = handler;
        maxFramerate = 0;
        frameCount = 0;
        zeroContinuousTime = 0;
        mDebug = debug;
    }

    public int getFrameCount(){
        return frameCount;
    }

    public int getMaxFramerate(){
        return maxFramerate;
    }

    //return seconds
    public int getZeroFrContinuousTime(){
        return zeroContinuousTime;
    }

    public String getResolution(){
        return resoluton;
    }

    public void setSurface(SurfaceHolder holder) {
        // TODO: release previous
        mDisplay = holder;


        if(mStat != MIRROR_PLAYING)
            mStat = MIRROR_INIT;
    }

    public void setSurface(Surface surface) {
        // TODO: release previous
        mSurface = surface;

        if(mStat != MIRROR_PLAYING)
            mStat = MIRROR_INIT;
    }

    public void setupCodec(int width, int height, byte[] spspps, double timestamp) {

        LogUtils.v("mirror setup codec:"+width+"x"+height+" state:"+mStat + " mDisplay:" + mDisplay);

        resoluton = width +"x"+ height;

        if(mStat == MIRROR_PLAYING){
            LogUtils.v("---------->rotate or resolution changed");
            destroy();
        }

        while (mStat == MIRROR_IDLE) {
            //LogEx.i(TAG,"MIRROR IDLE");
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int i = 0;
        int spslen = 0;
        int ppslen =0;

        if(spspps.length > 8){
            for(i = 4;i+4<spspps.length;i++){
                if((spspps[i] & 0xFF) == 0x0 && (spspps[i+1] & 0xFF) == 0x0 &&
                    (spspps[i+2] & 0xFF) == 0x0 && (spspps[i+3] & 0xFF) == 0x1)
                    break;
            }
        }
        //LogEx.i(TAG,"--------------------sps len:" + i);
        spslen = i;
        ppslen = spspps.length - i;
        //fileUtils = new FileUtils();

        try {
            mCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);

        byte[] sps = new byte[spslen];
        System.arraycopy(spspps,0,sps,0,spslen);
        mFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        byte[] pps = new byte[ppslen];
        System.arraycopy(spspps,spslen,pps,0,ppslen);
        mFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));

        spsppsBuffer = new byte[spspps.length];
        System.arraycopy(spspps,0,spsppsBuffer,0,spspps.length);
        //fileUtils.writeDataToFile(spspps,spspps.length);

        LogUtils.v("A mlistener:" + mListener);
        i = 0;
        while(mListener == null && ++i < 100 ){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.v("B mlistener:" + mListener);

        if(mListener != null){
            mListener.onVideoSizeChange(null);
        }

        checkSetting_l();
    }

    private void checkSetting_l() {
        Message message = new Message();

        assert(mStat == MIRROR_INIT);
        //		int i = 0;

        if(mFormat == null)
            LogUtils.v("mformat == null");

        if(mDisplay == null)
            LogUtils.v("mDisplay == null");

        if(mSurface == null)
            LogUtils.v("mSurface == null");

        if (mFormat != null && (mDisplay != null || mSurface != null)) {
            int flags = 0;

            //Hisi（Hisi3751V551）某些平台设置此参数可能会导致codec reset
            //if(!isHisiPlatform) {
            //    flags |= CONFIGURE_FLAG_LOWLAT;//mstar
            //}

            //mFormat.setInteger("low-latency",1);
            //mFormat.setInteger("vdec-lowlatency",1);
            //mFormat.setInteger("vendor.low-latency.enable",1);

            try {

                if (mDisplay != null)
                    mCodec.configure(mFormat, mDisplay.getSurface(), null, flags);
                else
                    mCodec.configure(mFormat, mSurface, null, flags);

            }catch (Throwable r){
                r.printStackTrace();
            }

            mCodec.start();

            mThread = new PlayerThread(mCodec);
            renderThreadRunning = true;

            if(mThread != null)
                mThread.start();

            mInputBuffers = mCodec.getInputBuffers();
            if (VERBOSE) {
                LogUtils.v("--------->Codec configured");
            }

            mStat = MIRROR_PLAYING;

        } else{
            LogUtils.v("---------->create codec err");
            mStat = MIRROR_CODEC_ERROR;

            message.what = MESSAGE_VIDEO_START_STATUS;
            message.arg1 = 0x1;
            message.arg2 = MIRROR_PLAYER_ERROR_MEDIACODEC_ERR;
        }

        myHandler.sendMessage(message);
    }

    public void destroy(){
        LogUtils.v("---------->mediacodec destroy");

        renderThreadRunning = false;
        mStat = MIRROR_STOPPED;

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if(mCodec != null){
                mCodec.stop();
                mCodec.release();
                mCodec = null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized boolean writeData(ByteBuffer buffer,double timestamp) {
        try {
            if (mStat != MIRROR_PLAYING) {
                LogUtils.v("writeData: invalid state:" + mStat);
                return true;
            }

            if (buffer.array().length == 0)
                LogUtils.v( "mirror data error");

            if (mFirstFrame) {
                firstFrameBuffer = new byte[buffer.array().length + spsppsBuffer.length];
                System.arraycopy(spsppsBuffer, 0, firstFrameBuffer, 0, spsppsBuffer.length);
                System.arraycopy(buffer.array(), 0, firstFrameBuffer, spsppsBuffer.length, buffer.array().length);
            }

            //fileUtils.writeDataToFile(buffer.array(),buffer.array().length);
            if(mDebug)
                LogUtils.v( "write data:"+buffer.array().length);

            downloadSpeed += buffer.array().length;

            for (; ; ) {
                int idx = mCodec.dequeueInputBuffer(10000);//10ms
                if (idx >= 0) {
                    ByteBuffer input = mInputBuffers[idx];
                    int length = buffer.remaining();

                    input.clear();

                    if (mFirstFrame) {
                        input.put(firstFrameBuffer);
                        LogUtils.v( "First Buffer:" + firstFrameBuffer.length);
                        mFirstFrame = false;
                        length = firstFrameBuffer.length;
                        mCodec.queueInputBuffer(idx, 0, length, 0, 0);
                    } else {
                        input.put(buffer);
                        mCodec.queueInputBuffer(idx, 0, length, 0, 0);

                    }

                    if(mDebug)
                        LogUtils.v( "queueInputBuffer: length=" + length);

                    break;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    LogUtils.v( "queueInputBuffer failed: idx=" + idx);
                }
            }
            return true;
        } catch (Throwable e){
            LogUtils.v("writeData error! " + e);
            mStat = MIRROR_CODEC_ERROR;
            return false;
        }
    }

    public void releaseAll(){
        try {
            if (mStat == MIRROR_PLAYING) {
                LogUtils.v("---------->releaseAll MIRROR_PLAYING");
                destroy();
            }
            mCodec = null;
            mDisplay = null;
            mSurface = null;
            mFormat = null;
            mInputBuffers = null;
            spsppsBuffer = null;
            firstFrameBuffer = null;

        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    private static class PlayerThread extends Thread {

        private final MediaCodec mCodec;
        private ByteBuffer[] mOutputBuffers;

        public PlayerThread(MediaCodec codec) {
            super("Player");
            mCodec = codec;
            mOutputBuffers = mCodec.getOutputBuffers();
        }

        @Override
        public void run() {
            super.run();
            boolean firstRender = true;
            long aTime=System.currentTimeMillis();
            long bTime = aTime;
            long lastFrameTime = System.currentTimeMillis();
            long currentFrameTime = lastFrameTime;

            BufferInfo info = new BufferInfo();
            try {
                while (renderThreadRunning) {

                    if (mStat != MIRROR_PLAYING) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    bTime = System.currentTimeMillis();
                    if (bTime - aTime >= 1000) {
                        aTime = bTime;
                        Message message = new Message();
                        message.what = MESSAGE_VIDEO_REFRESH_INFO;
                        message.arg1 = downloadSpeed;
                        message.arg2 = frameRate;

                        if (frameRate > maxFramerate)
                            maxFramerate = frameRate;

                        if (frameRate == 0) {
                            zeroContinuousTime++;
                        }

                        downloadSpeed = 0;
                        frameRate = 0;
                        myHandler.sendMessage(message);
                    }

                    int idx = mCodec.dequeueOutputBuffer(info, 50000);
                    switch (idx) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            LogUtils.v( "INFO_OUTPUT_BUFFERS_CHANGED");
                            mOutputBuffers = mCodec.getOutputBuffers();
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            LogUtils.v( "New format " + mCodec.getOutputFormat());
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            if(mDebug)
                                LogUtils.v( "dequeueOutputBuffer timed out!");
                            Thread.yield();
                            break;
                        default:
                            if(mDebug)
                                LogUtils.v("dequeue output idx:"+idx);
                            ByteBuffer buffer = mOutputBuffers[idx];
                            //LogEx.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                            frameRate++;
                            frameCount++;
                            lastFrameTime = currentFrameTime;

                            mCodec.releaseOutputBuffer(idx, true);

                            if (firstRender == true) {
                                //++count;
                                //log.v("Frame Render:"+count);
                                //firstRender =false;
                            }
                            break;
                    }
                }
            } catch (Throwable e){
                Log.e(TAG, "render Thread Running error ", e);
            }
        }
    }
}
