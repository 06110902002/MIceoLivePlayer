package come.live.microliveplayer.peer.discovering;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.text.TextUtils;
import come.live.decodelib.MsgCenterMgr;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.NetworkUtils;
import come.live.microliveplayer.AppContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author       hengyang.lxb
 * @date         2020/12/19
 * @Version:     1.0
 * @Description:
 * 广播设备信息，主要是将自身的ip与端口号广播给客户端，方便客户端拿到
 * ip与port 进行连接
 */
public class DevicesBroadcast {

    private static final String BROADCAST_IP = "224.0.0.10";
    private static final int BROADCAST_PORT  = 8681;
    private static final int BROADCAST_INTAL = 5 * 1000;
    private static final int BROADCAST_LIVE  = 100;
    private static byte[] broadcastPacket;
    private boolean isStartBroadcat = false;
    private static DevicesBroadcast devicesBroadcast;
    private MulticastSocket multicastSocket = null;
    private InetAddress address = null;
    private DatagramPacket dataPacket;
    private BroadcastUDPThread broadcastUDPThread;
    private String mIp;

    private DevicesBroadcast() {
    }

    public static DevicesBroadcast getInstance() {
        if (devicesBroadcast == null) {
            synchronized (DevicesBroadcast.class){
                devicesBroadcast = new DevicesBroadcast();
            }
        }
        return devicesBroadcast;
    }

    /**
     * 当与客户端tcp连接断开之后应再次调用本函数
     */
    public void start(){
        broadcastUDPThread = new BroadcastUDPThread();
        broadcastUDPThread.start();
    }

    /**
     * 构建广播信息
     *
     * @param ip   将本身的ip 暴露给接收方，接收方收到这个ip就可以进行连接
     * @param port 建立socket连接的端口号
     */
    private byte[] buildBroadcastInfo(String ip, int port) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ip",ip);
            jsonObject.put("port",port);
            byte[] broadcastBytes = jsonObject.toString().getBytes();
            return broadcastBytes;
        } catch (JSONException e) {
            LogUtils.v(e.toString());
            isStartBroadcat = false;
        }
        return null;

    }

    /**
     * 当与客户端建立好tcp 连接之后，就调用本接口释放资源
     */
    public void stop() {
        isStartBroadcat = true;
        if(broadcastUDPThread != null){
            broadcastUDPThread.cancel();
            broadcastUDPThread = null;
        }
        devicesBroadcast = null;
        if(multicastSocket != null && address != null){
            try {
                multicastSocket.leaveGroup(address);
                multicastSocket = null;
                address = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataPacket = null;
        broadcastPacket = null;
    }

    /**
     * 创建udp数据
     */
    private void initUDP(String ip) {
        try {
            multicastSocket = new MulticastSocket(BROADCAST_PORT);
            // 广播生存时间0-255
            multicastSocket.setTimeToLive(BROADCAST_LIVE);
            address = InetAddress.getByName(BROADCAST_IP);
            //加入广播接收组
            multicastSocket.joinGroup(address);
            multicastSocket.setLoopbackMode(false);// 必须是false才能开启广播功能
            broadcastPacket = buildBroadcastInfo(ip,MsgCenterMgr.PORT);
            dataPacket = new DatagramPacket(broadcastPacket, broadcastPacket.length, address, BROADCAST_PORT);
            isStartBroadcat = true;
        } catch (IOException e) {
            e.printStackTrace();
            isStartBroadcat = false;
        }
    }

    /**
     * 广播udp 的报文
     */
    private class BroadcastUDPThread extends Thread{
        @Override
        public void run() {
            super.run();
            if(TextUtils.isEmpty(mIp)){
                mIp = NetworkUtils.getWifiIP(AppContext.getContext());
            }
            initUDP(mIp);
            while (isStartBroadcat){
                if (multicastSocket != null && dataPacket != null) {
                    try {
                        multicastSocket.send(dataPacket);
                        Thread.sleep(BROADCAST_INTAL);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        isStartBroadcat = false;
                    }
                    LogUtils.v("正在发送udp广播报文:"+new String(broadcastPacket));
                } else {
                    initUDP(mIp);
                }
            }
        }

        public void cancel(){
            isStartBroadcat = false;
            interrupt();
        }
    }
}
