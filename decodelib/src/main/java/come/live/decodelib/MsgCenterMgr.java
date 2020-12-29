package come.live.decodelib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import androidx.annotation.NonNull;
import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.model.LiveHead;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.ReadMsgUtils;

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

    public MsgCenterMgr(){
        mHandler = new ReconnectHandler();
    }

    public void start(){
        if(isRunning) {
            LogUtils.v("已经启动了，无需再次启动......");
            return;
        }
        if(surface == null){
            LogUtils.v("渲染视频对象参数错误，请提供一个渲染解码后的surface,当前surface:"+surface);
            return;
        }
        readMsgThread = new ReadMsgThread();
        readMsgThread.start();

        streamQueue = new LinkedBlockingQueue<>();

        decodeStreamMediaThread = new DecodeStreamMediaThread();
        decodeStreamMediaThread.setStreamMediaQueue(streamQueue);
        decodeStreamMediaThread.initVideoMeidaCodec(width,height,surface);
        decodeStreamMediaThread.start();
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
            serverSocket.setSoTimeout(60 * 1000);
            while (serverSocket != null){
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                if (socket.isConnected()) {
                    LogUtils.v("socket连接成功:"+socket.getInetAddress());
                    break;
                }else{
                    Thread.sleep(100);
                }
            }
            isRunning = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    /**
     * 初始化解码器
     * @param width    解码器输入画面宽度
     * @param height   解码器输入画面高度
     * @param surface  解码h264之后渲染画面
     */
    public void setConfig(int width,int height, Surface surface){
        //测试单独解码视频
        //oriH264Queue = new LinkedBlockingQueue<>();
        //decodeH264Thread = new DecodeH264Thread(oriH264Queue,width,height,surface);
        //decodeH264Thread.start();

        //测试单独解码音频
        //new TestAudioThread(oriH264Queue).start();

        this.width = width;
        this.height = height;
        this.surface = surface;
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

                byte[] lengthByte = new byte[4];
                //报文长度
                System.arraycopy(header,4,lengthByte,0,3);
                int len = ByteUtil.bytesToInt(lengthByte);

                byte[] content = ReadMsgUtils.readBytesByLength(inputStream, len);

                LiveEntity liveEntity = new LiveEntity(type,content);
                streamQueue.put(liveEntity);

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

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECONNECT:
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
}
