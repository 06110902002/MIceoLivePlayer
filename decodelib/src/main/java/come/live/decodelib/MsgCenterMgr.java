package come.live.decodelib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.model.LiveHead;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.H264SPSParse;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.ReadMsgUtils;
import come.live.decodelib.video.MirrorContext;
import come.live.decodelib.video.VideoPlay;

/**
 * @author  hengyang.lxb
 * @date    2020/12/09
 * @Version: 1.0
 * @Description:
 * serverSocket读取消息线程,消息中心管理器
 * 1.负责消息读取-入队
 * 2.负责解码器的生命周期
 */
public class MsgCenterMgr {

    private final int RECONNECT = 23;
    private ReconnectHandler mHandler;
    public static final int PORT = 12580;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LinkedBlockingQueue<byte[]> oriH264Queue;
    private DecodeH264Thread decodeH264Thread;
    private boolean isRunning = false;
    private ReadMsgThread readMsgThread;
    private LinkedBlockingQueue<LiveEntity> streamQueue;
    private DecodeStreamMediaThread decodeStreamMediaThread;
    private int width;
    private int height;
    private Surface surface;
    private VideoSizeChangeListener videoSizeChangeListener;
    private MirrorContext mirrorContext;
    private byte[] sps;
    private byte[] pps;
    private VideoPlay videoPlay;
    private VideoPlay videoPlay2;
    private Surface surface2;
    private LinkedBlockingQueue<LiveEntity> waitSendQueue = new LinkedBlockingQueue<>();
    private SendThread sendThread;

    public MsgCenterMgr(){
        mHandler = new ReconnectHandler();
    }

    public void setVideoSizeChangeListener(VideoSizeChangeListener listener){
        this.videoSizeChangeListener = listener;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void start(){
        if(isRunning) {
            LogUtils.v("已经启动了，无需再次启动......");
            return;
        }
        if(surface == null || !surface.isValid()){
            LogUtils.v("渲染视频对象参数错误，请提供一个渲染解码后的surface,当前surface:"+surface);
            return;
        }
        readMsgThread = new ReadMsgThread();
        readMsgThread.start();

        streamQueue = new LinkedBlockingQueue<>();

        //decodeStreamMediaThread = new DecodeStreamMediaThread();
        //decodeStreamMediaThread.setStreamMediaQueue(streamQueue);
        //decodeStreamMediaThread.initVideoMediaCodec(width,height,surface);
        //decodeStreamMediaThread.start();

        videoPlay = new VideoPlay();
        videoPlay.initMediaCodec(width,height,surface);

        videoPlay2 = new VideoPlay();
        videoPlay2.initMediaCodec(width,height,surface2);
    }

    /**
     * 初始化 serverSocket
     */
    public void init(){
        try {
            LogUtils.v("开始监听连接......");
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            InetSocketAddress socketAddress = new InetSocketAddress(PORT);
            serverSocket.bind(socketAddress);
            serverSocket.setSoTimeout(600 * 1000);
            while (serverSocket != null){
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                if (socket.isConnected()) {
                    sendThread = new SendThread();
                    sendThread.start();
                    LogUtils.v("socket连接成功:"+socket.getInetAddress());
                    break;
                }else{
                    Thread.sleep(100);
                }
            }
            isRunning = true;
        } catch (IOException | InterruptedException e) {
            LogUtils.v("监听连接超时，系统将在3秒后重新启动，具体异常信息如下:");
            e.printStackTrace();
            isRunning = false;
            mHandler.sendEmptyMessageDelayed(RECONNECT,3000);
        }
    }

    /**
     * 初始化解码器
     * @param width    解码器输入画面宽度
     * @param height   解码器输入画面高度
     * @param surface  解码h264之后渲染画面
     */
    public void setConfig(int width,int height, Surface surface,Surface surface2){
        //测试单独解码视频
        //oriH264Queue = new LinkedBlockingQueue<>();
        //decodeH264Thread = new DecodeH264Thread(oriH264Queue,width,height,surface);
        //decodeH264Thread.start();

        //测试单独解码音频
        //new TestAudioThread(oriH264Queue).start();

        this.width = width;
        this.height = height;
        this.surface = surface;
        this.surface2 = surface2;

        //mirrorContext = new MirrorContext(null,mHandler,false);
        //mirrorContext.setSurface(surface);

    }

    /**
     * 将客户端发来的消息入队，等待解码
     */
    public void readMessage(){
        while(isRunning){
            try {
                byte[] header = ReadMsgUtils.readBytesByLength(inputStream, 12);
                if (header == null || header.length == 0) {
                    Thread.sleep(10);
                    continue;
                }
                //LiveHead liveHead = ReadMsgUtils.analysisHeader(header);
                //Log.v("msgCenterMgr","正在读取消息,协议类型："+liveHead.getType());
                //if(liveHead.getBuffSize() <= 0){
                //    Thread.sleep(10);
                //    continue;
                //}
                //byte[] receiveData = ReadMsgUtils.analysisDataWithHead(inputStream, liveHead);
                //if(receiveData == null || receiveData.length <= 0){
                //    Thread.sleep(10);
                //    continue;
                //}
                //oriH264Queue.put(receiveData);


                byte[] typeBuff = new byte[4];
                //协议类型
                System.arraycopy(header, 0, typeBuff, 0, 3);
                int type = ByteUtil.bytesToInt(typeBuff);

                //显示在哪个SurfaceView
                byte[] surfaceViewIdxBytes = new byte[4];
                System.arraycopy(header,4,surfaceViewIdxBytes,0,3);
                int surfaceViewIdx = ByteUtil.bytesToInt(surfaceViewIdxBytes);

                byte[] lengthByte = new byte[4];
                //报文长度
                System.arraycopy(header,8,lengthByte,0,3);
                int len = ByteUtil.bytesToInt(lengthByte);

                byte[] content = ReadMsgUtils.readBytesByLength(inputStream, len);

                if(type == LiveEntity.RESOLUTION){

                    String tmp = ByteUtil.bytes2String(content);
                    LogUtils.v("分辨率："+tmp);
                    if (videoSizeChangeListener != null && !TextUtils.isEmpty(tmp)){
                        String[] resolutions = tmp.split(":");
                        videoSizeChangeListener.onVideoSizeChange(Integer.parseInt(resolutions[0]),Integer.parseInt(resolutions[1]));
                    }

                } else if(type == 29) {
                    //sendLiveDate(typeBuff,lengthByte,content);
                }
                /*else if(type == LiveEntity.SPS) {
                    sps = content;
                    mirrorContext.setupCodec(this.width,this.height,content,System.currentTimeMillis());

                }else if (type == LiveEntity.PPS) {
                    pps = content;
                    byte[] newBuf = new byte[sps.length + pps.length];
                    System.arraycopy(sps, 0, newBuf, 0, sps.length);
                    System.arraycopy(pps, 0, newBuf, sps.length, pps.length);
                    mirrorContext.setupCodec(this.width,this.height,newBuf,System.currentTimeMillis());
                }*/
                else {
                    //LiveEntity liveEntity = new LiveEntity(type,content);
                    //streamQueue.put(liveEntity);
                   // mirrorContext.writeData(ByteUtil.byte2ByteBuffer(content),System.currentTimeMillis());
                    //方法二
                    //videoPlay.putH264InputBuffer(content);
                    //方法三 使用同步方式解码渲染 264
                    if (surfaceViewIdx == 1) {
                        videoPlay.addH264Packer(content);
                    } else {
                        videoPlay2.addH264Packer(content);
                    }


                }


            } catch (IOException e) {
                LogUtils.v("读取网络数据发生异常，请检查客户端是否存活，网络是否异常，系统将在3秒后重新启动，具体异常信息如下:");
                e.printStackTrace();
                shutDown();
                mHandler.sendEmptyMessageDelayed(RECONNECT,3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected(){
        return socket != null && socket.isConnected();
    }

    private class ReconnectHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECONNECT:
                    shutDown();
                    start();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 关闭消息中心
     */
    public void shutDown(){
        if(readMsgThread != null){
            readMsgThread.cancel();
            readMsgThread = null;
        }
        if(decodeStreamMediaThread != null){
            decodeStreamMediaThread.stopDecodec();
            decodeStreamMediaThread = null;
        }
        if(isConnected()){
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(serverSocket != null){
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(decodeH264Thread != null){
            decodeH264Thread.stopDecodeH264();
            decodeH264Thread = null;
        }

        if(oriH264Queue != null){
            oriH264Queue.clear();
            oriH264Queue = null;
        }

        if(videoPlay != null){
            videoPlay.release();
            videoPlay = null;
        }
        if(videoPlay2 != null){
            videoPlay2.release();
            videoPlay2 = null;
        }
        if (waitSendQueue != null) {
            waitSendQueue.clear();
        }
        if (sendThread != null) {
            sendThread = null;
        }

    }

    /**
     * 读取消息的线程
     */
    private class ReadMsgThread extends Thread{

        @Override
        public void run() {
            super.run();
            init();
            readMessage();
        }

        public void cancel(){
            isRunning = false;
            interrupt();
        }
    }

    /**
     * 发送live 报文
     */
    private void sendLiveDate(byte[] type, byte[] length, byte[] content) {

        if (outputStream != null) {
            try {
                outputStream.write(type, 0, type.length);
                outputStream.write(length, 0, length.length);
                outputStream.write(content, 0, content.length);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean touchevent(MotionEvent touch_event,
                              int displayW, int displayH,
                              int screenWidth, int screenHeight) {

        if (touch_event.getAction() == MotionEvent.ACTION_DOWN) {
            String point = (int) touch_event.getX() * screenWidth / displayW + ":" + (int) touch_event.getY() * screenHeight / displayH;
            LogUtils.v("point = " + point);
            byte[] pointByte = point.getBytes(StandardCharsets.UTF_8);
            byte[] type = ByteUtil.int2Bytes(1);
            byte[] length = ByteUtil.int2Bytes(pointByte.length);
            LiveEntity liveEntity = new LiveEntity();
            liveEntity.setType(type);
            liveEntity.setContentLength(length);
            liveEntity.setContent(pointByte);
            try {
                waitSendQueue.put(liveEntity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean touchevent2(MotionEvent touch_event,
                              int displayW, int displayH,
                              int screenWidth, int screenHeight) {

        if (touch_event.getAction() == MotionEvent.ACTION_DOWN && waitSendQueue != null) {
            String point = (int) touch_event.getX() * screenWidth / displayW + ":" + (int) touch_event.getY() * screenHeight / displayH;
            LogUtils.v("point = " + point + " x = " + touch_event.getX());
            byte[] pointByte = point.getBytes(StandardCharsets.UTF_8);
            byte[] type = ByteUtil.int2Bytes(2);
            byte[] length = ByteUtil.int2Bytes(pointByte.length);
            LiveEntity liveEntity = new LiveEntity();
            liveEntity.setType(type);
            liveEntity.setContentLength(length);
            liveEntity.setContent(pointByte);
            try {
                waitSendQueue.put(liveEntity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void sendKeyCode(int keyCode,int surfaceViewIdx) {
        byte[] keyCodeByte = (keyCode+"").getBytes(StandardCharsets.UTF_8);
        byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
        byte[] length = ByteUtil.int2Bytes(keyCodeByte.length);
        LiveEntity liveEntity = new LiveEntity();
        liveEntity.setType(type);
        liveEntity.setContentLength(length);
        liveEntity.setContent(keyCodeByte);
        try {
            waitSendQueue.put(liveEntity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class SendThread extends Thread {


        @Override
        public void run() {
            super.run();
            send();
        }
    }

    private void send() {
        while (isConnected()) {
            while (waitSendQueue != null && !waitSendQueue.isEmpty()) {
                LiveEntity liveEntity = waitSendQueue.poll();
                LogUtils.v("52------开始发送消息.....surfaceIdx " + ByteUtil.bytesToInt(liveEntity.getType()));
                if (liveEntity == null) {
                    continue;
                }
                sendLiveDate(liveEntity.getType(), liveEntity.getContentLength(), liveEntity.getContent());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
