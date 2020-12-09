package come.live.decodelib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import android.view.Surface;
import come.live.decodelib.model.LiveHead;
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
public class MsgCenterMgr extends Thread {

    private final int PORT = 12580;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LinkedBlockingQueue<byte[]> oriH264Queue;
    private DecodeH264Thread decodeH264Thread;

    @Override
    public void run() {
        super.run();
        init();
        readMessage();
    }

    /**
     * 初始化 serverSocket
     */
    public void init(){
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            InetSocketAddress socketAddress = new InetSocketAddress(PORT);
            serverSocket.bind(socketAddress);
            serverSocket.setSoTimeout(20000);
            while (true){
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                if (socket.isConnected()) {
                    LogUtils.v("socket连接成功:"+socket.getInetAddress());
                    break;
                }else{
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化解码器
     * @param width    解码器输入画面宽度
     * @param height   解码器输入画面高度
     * @param surface  解码h264之后渲染画面
     */
    public void initMediaCodec(int width,int height, Surface surface){
        oriH264Queue = new LinkedBlockingQueue<>();
        decodeH264Thread = new DecodeH264Thread(oriH264Queue,width,height,surface);
        decodeH264Thread.start();
    }

    /**
     * 将客户端发来的消息入队，等待解码
     */
    public void readMessage(){
        while(isConnected()){
            try {
                byte[] header = ReadMsgUtils.readBytesByLength(inputStream, 8);
                if (header == null || header.length == 0) {
                    Thread.sleep(10);
                    continue;
                }
                LiveHead liveHead = ReadMsgUtils.analysisHeader(header);
                LogUtils.v("正在读取消息,协议类型："+liveHead.getType());
                if(liveHead.getBuffSize() <= 0){
                    Thread.sleep(10);
                    continue;
                }
                byte[] receiveData = ReadMsgUtils.analysisDataWithHead(inputStream, liveHead);
                if(receiveData == null || receiveData.length <= 0){
                    Thread.sleep(10);
                    continue;
                }
                oriH264Queue.put(receiveData);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected(){
        return socket != null && socket.isConnected();
    }

    /**
     * 关闭消息中心
     */
    public void shutDown(){
        if(isConnected()){
            try {
                socket.close();
                socket = null;
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
}
