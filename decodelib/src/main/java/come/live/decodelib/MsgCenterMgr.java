package come.live.decodelib;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.ReadMsgUtils;
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
    public static final int CONNECT_SUCCESS  = 100;
    public static final int DISCONNECTED  = 101;
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
    private int width2;
    private int height2;
    private Surface surface;
    private DataParseListener videoSizeChangeListener;
    private byte[] sps;
    private byte[] pps;
    private VideoPlay videoPlay;
    private VideoPlay videoPlay2;
    private Surface surface2;
    private LinkedBlockingQueue<LiveEntity> waitSendQueue = new LinkedBlockingQueue<>();
    private SendThread sendThread;
    private int pageIdx = 0;
    private static MsgCenterMgr msgCenterMgr;

    public static MsgCenterMgr getInstance(){
        if(msgCenterMgr == null){
            synchronized (MsgCenterMgr.class){
                msgCenterMgr = new MsgCenterMgr();
            }
        }
        return msgCenterMgr;
    }

    private MsgCenterMgr(){
        mHandler = new ReconnectHandler();
    }

    public void setVideoSizeChangeListener(DataParseListener listener){
        this.videoSizeChangeListener = listener;
    }
    private ConnectListener connectListener;
    public void setConnectListener(ConnectListener listener) {
        this.connectListener = listener;
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
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startDecodec(int pageIdx) {
        this.pageIdx = pageIdx;
        if (pageIdx == 0) {
            if (videoPlay2 == null) {
                videoPlay2 = new VideoPlay();
                videoPlay2.initMediaCodec(width2,height2,surface2);
            }
        } else {
            if (videoPlay == null) {
                videoPlay = new VideoPlay();
                videoPlay.initMediaCodec(width,height,surface);
            }
        }
    }

    public void stopLastEncoder(int encodecIdx) {
        LiveEntity liveEntity = new LiveEntity();
        liveEntity.setType(ByteUtil.int2Bytes(LiveEntity.STOP_CODEC));
        byte[] content = ByteUtil.int2Bytes(encodecIdx);
        liveEntity.setContentLength(ByteUtil.int2Bytes(content.length));
        liveEntity.setContent(content);
        sendMsg(liveEntity);
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
                    if (connectListener != null) {
                        connectListener.onStatus(CONNECT_SUCCESS,"Success");
                    }
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
    public void setConfig(int width,int height, Surface surface,int width2,int height2,Surface surface2){
        this.width = width;
        this.height = height;
        this.width2 = width2;
        this.height2 = height2;
        this.surface = surface;
        this.surface2 = surface2;
    }

    /**
     * 将客户端发来的消息入队，等待解码
     */
    public void readMessage(){
        while(isRunning){
            try {
                byte[] header = ReadMsgUtils.readBytesByLength(inputStream, 8);
                if (header == null || header.length == 0) {
                    Thread.sleep(10);
                    continue;
                }
                byte[] typeBuff = new byte[4];
                System.arraycopy(header, 0, typeBuff, 0, 3);
                int type = ByteUtil.bytesToInt(typeBuff);

                byte[] lengthByte = new byte[4];
                System.arraycopy(header,4,lengthByte,0,3);
                int len = ByteUtil.bytesToInt(lengthByte);

                byte[] content = ReadMsgUtils.readBytesByLength(inputStream, len);

                if(type == LiveEntity.LAUNCHER_DATA){
                    String tmp = ByteUtil.bytes2String(content);
                    if (videoSizeChangeListener != null && !TextUtils.isEmpty(tmp)){
                        videoSizeChangeListener.onParseData(type,tmp);
                    }
                } else if (type == LiveEntity.BACK_TO_PAGE_HOME) {
                    if (videoSizeChangeListener != null ){
                        videoSizeChangeListener.onParseData(type,null);
                    }
                }
                else {
                    if (type == LiveEntity.SPS && videoSizeChangeListener != null) {
                        videoSizeChangeListener.onVideoSizeChange(null);
                    }
                    if (videoPlay != null && pageIdx != 0) {
                        videoPlay.putH264InputBuffer(content);
                    }
                    if (videoPlay2 != null && pageIdx == 0) {
                        videoPlay2.putH264InputBuffer(content);
                    }
                }


            } catch (IOException e) {
                LogUtils.v("读取网络数据发生异常，请检查客户端是否存活，网络是否异常，系统将在3秒后重新启动，具体异常信息如下:");
                e.printStackTrace();
                if (connectListener != null) {
                    connectListener.onStatus(DISCONNECTED,"连接断开");
                }
                shutDown();
                mHandler.sendEmptyMessage(RECONNECT);
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
                              int screenWidth, int screenHeight,
                              int surfaceViewIdx) {

//        if (touch_event.getAction() == MotionEvent.ACTION_DOWN) {
//            String point = (int) touch_event.getX() * screenWidth / displayW + ":" + (int) touch_event.getY() * screenHeight / displayH;
//            LogUtils.v("point = " + point);
//            byte[] pointByte = point.getBytes(StandardCharsets.UTF_8);
//            byte[] type = ByteUtil.int2Bytes(1);
//            byte[] length = ByteUtil.int2Bytes(pointByte.length);
//            LiveEntity liveEntity = new LiveEntity();
//            liveEntity.setType(type);
//            liveEntity.setContentLength(length);
//            liveEntity.setContent(pointByte);
//            try {
//                waitSendQueue.put(liveEntity);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        int[] buf = new int[] {
                touch_event.getAction(), touch_event.getButtonState(),
                (int) touch_event.getX() * screenWidth / displayW,
                (int) touch_event.getY() * screenHeight / displayH
        };
        final byte[] array = new byte[buf.length * 4];
        for (int j = 0; j < buf.length; j++) {
            int c = buf[j];
            array[j * 4] = (byte) ((c & 0xFF000000) >> 24);
            array[j * 4 + 1] = (byte) ((c & 0xFF0000) >> 16);
            array[j * 4 + 2] = (byte) ((c & 0xFF00) >> 8);
            array[j * 4 + 3] = (byte) (c & 0xFF);
        }

        byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
        byte[] length = ByteUtil.int2Bytes(array.length);
        LiveEntity liveEntity = new LiveEntity();
        liveEntity.setType(type);
        liveEntity.setContentLength(length);
        liveEntity.setContent(array);
        //sendLiveDate(type,length,array);
        try {
            waitSendQueue.put(liveEntity);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    float downX = 0;
    float downY = 0;
    float moveX = 0;
    float moveY = 0;
    long currentMS = 0;
    public void sendEvent(MotionEvent event,int displayW, int displayH,
                          int screenWidth, int screenHeight,int surfaceViewIdx) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                moveX = 0f;
                moveY = 0f;
                currentMS = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
//                moveX += Math.abs(event.getX() - downX);
//                moveY += Math.abs(event.getY() - downY);
//                downX = event.getX();
//                downY = event.getY();
//                String fromXtoX = (int) downX * screenWidth / displayW + ":" + (int) event.getX() * screenWidth / displayW;
//                String fromYtoY = (int) downY * screenHeight / displayH + ":" + (int) event.getY() * screenHeight / displayH;
//                String point = fromXtoX + "/" + fromYtoY;
//                LogUtils.v("fromXtoX = " + fromXtoX + " fromYtoY = " + fromYtoY);
//                byte[] pointByte = point.getBytes(StandardCharsets.UTF_8);
//                byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
//                byte[] length = ByteUtil.int2Bytes(pointByte.length);
//                LiveEntity liveEntity = new LiveEntity();
//                liveEntity.setType(type);
//                liveEntity.setContentLength(length);
//                liveEntity.setContent(pointByte);
//                try {
//                    waitSendQueue.put(liveEntity);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                break;
            case MotionEvent.ACTION_UP:
                moveX += Math.abs(event.getX() - downX);
                moveY += Math.abs(event.getY() - downY);
                long moveTime = System.currentTimeMillis() - currentMS;
                //发送滑动事件
                if (moveTime > 200 && (moveX > 50 || moveY > 50)) {
                    String fromXtoX = (int) downX * screenWidth / displayW + ":" + (int) event.getX() * screenWidth / displayW;
                    String fromYtoY = (int) downY * screenHeight / displayH + ":" + (int) event.getY() * screenHeight / displayH;
                    String point = fromXtoX + "/" + fromYtoY;
                    LogUtils.v("fromXtoX = " + fromXtoX + " fromYtoY = " + fromYtoY);
                    byte[] pointByte = point.getBytes(StandardCharsets.UTF_8);
                    byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
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
                } else { //发送点击事件
                    String point2 = (int) event.getX() * screenWidth / displayW + ":" + (int) event.getY() * screenHeight / displayH;
                    LogUtils.v("point = " + point2 + " x = " + event.getX());
                    byte[] pointByte2 = point2.getBytes(StandardCharsets.UTF_8);
                    byte[] type2 = ByteUtil.int2Bytes(surfaceViewIdx);
                    byte[] length2 = ByteUtil.int2Bytes(pointByte2.length);
                    LiveEntity liveEntity2 = new LiveEntity();
                    liveEntity2.setType(type2);
                    liveEntity2.setContentLength(length2);
                    liveEntity2.setContent(pointByte2);
                    try {
                        waitSendQueue.put(liveEntity2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                downX = event.getX();
                downY = event.getY();
                break;
        }

    }

    public void sendKeyCode(int keyCode,int surfaceViewIdx) {
//        byte[] keyCodeByte = (keyCode+"").getBytes(StandardCharsets.UTF_8);
//        byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
//        byte[] length = ByteUtil.int2Bytes(keyCodeByte.length);
//        LiveEntity liveEntity = new LiveEntity();
//        liveEntity.setType(type);
//        liveEntity.setContentLength(length);
//        liveEntity.setContent(keyCodeByte);
//        try {
//            waitSendQueue.put(liveEntity);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        int[] buf = new int[]{keyCode};
//
//        final byte[] array = new byte[buf.length * 4];   // https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
//        for (int j = 0; j < buf.length; j++) {
//            final int c = buf[j];
//            array[j * 4] = (byte) ((c & 0xFF000000) >> 24);
//            array[j * 4 + 1] = (byte) ((c & 0xFF0000) >> 16);
//            array[j * 4 + 2] = (byte) ((c & 0xFF00) >> 8);
//            array[j * 4 + 3] = (byte) (c & 0xFF);
//        }
        final byte[] array = ByteUtil.int2Bytes(keyCode);
        byte[] type = ByteUtil.int2Bytes(surfaceViewIdx);
        byte[] length = ByteUtil.int2Bytes(array.length);
        LiveEntity liveEntity = new LiveEntity();
        liveEntity.setType(type);
        liveEntity.setContentLength(length);
        liveEntity.setContent(array);
        try {
            waitSendQueue.put(liveEntity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(LiveEntity liveEntity) {
        if (liveEntity != null && waitSendQueue != null) {
            try {
                waitSendQueue.put(liveEntity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isConnected() && connectListener != null) {
            connectListener.onStatus(DISCONNECTED,"连接断开");
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
                sendLiveDate(liveEntity.getType(), liveEntity.getContentLength(), liveEntity.getContent());
            }
        }
    }

    public interface ConnectListener {
        void onStatus(int code,String msg);
    }
}
