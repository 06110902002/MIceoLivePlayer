package come.live.decodelib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;
import come.live.decodelib.model.LiveHead;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.ReadMsgUtils;

/**
 * @author  hengyang.lxb
 * @date    2020/12/09
 * @Version: 1.0
 * @Description: serverSocket读取消息线程
 */
public class ReadMsgThread extends Thread {

    private final int PORT = 12580;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LinkedBlockingQueue<byte[]> oriH264Queue;

    @Override
    public void run() {
        super.run();
        init();
        readMessage();
    }

    public void init(){
        try {
            oriH264Queue = new LinkedBlockingQueue<>();

            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            InetSocketAddress socketAddress = new InetSocketAddress(PORT);
            serverSocket.bind(socketAddress);
            serverSocket.setSoTimeout(20000);
            while (true){
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                if (socket.isConnected()) {
                    LogUtils.v("socket连接成功");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
