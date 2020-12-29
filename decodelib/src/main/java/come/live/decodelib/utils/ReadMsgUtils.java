package come.live.decodelib.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import come.live.decodelib.model.LiveHead;

/**
 * Author:      hengyang
 * CreateDate:  2020/12/6 8:07 AM
 * Version:     1.0
 * Description: 消息读取管理器
 */
public class ReadMsgUtils {

    /**
     * 分析头部数据,主要分析前8字节
     */
    public static LiveHead analysisHeader(byte[] header) {
        byte[] typeBuff = new byte[4];
        //协议类型
        System.arraycopy(header, 0, typeBuff, 0, 3);
        int nType = ByteUtil.bytesToInt(typeBuff);

        byte[] lengthByte = new byte[4];
        //报文长度
        System.arraycopy(header,4,lengthByte,0,3);
        int len = ByteUtil.bytesToInt(lengthByte);

        return new LiveHead(nType, len);
    }

    /**
     * 读取头部的指定的buffSize数据
     *
     * @param is socket 输入流
     * @param liveHead  头部数据
     * @return 实体报文
     */
    public static byte[] analysisDataWithHead(InputStream is,LiveHead liveHead){
        byte[] buff = null;
        if (liveHead.getBuffSize() != 0) {
            try {
                buff = readBytesByLength(is, liveHead.getBuffSize());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buff;
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param is        输入流
     * @param readSize  读入的尺寸
     * @return          读取指定长度的byte[]
     * @throws IOException 异常
     */
    public static byte[] readBytesByLength(InputStream is, int readSize) throws IOException {
        byte[] buff = new byte[readSize];
        int len = 0;
        int eachLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (len < readSize) {
            eachLen = is.read(buff);
            if (eachLen != -1) {
                len += eachLen;
                baos.write(buff, 0, eachLen);
            } else {
                baos.close();
                throw new IOException();
            }
            if (len < readSize) {
                buff = new byte[readSize - len];
            }
        }
        byte[] b = baos.toByteArray();
        baos.close();
        return b;
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param socket        输入流
     * @param size          读入的尺寸
     * @return              读取指定长度的byte[]
     * @throws IOException  当socket通道关闭，或者读取的过程中发生断开则丢出异常
     */
    public static byte[] readBytesByLength(Socket socket,int size) throws IOException {
        if (socket != null && socket.isConnected()) {
            InputStream is = socket.getInputStream();
            byte[] buff = new byte[size];
            int len = 0;
            int eachLen = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (len < size) {
                eachLen = is.read(buff);
                if (eachLen != -1) {
                    len += eachLen;
                    baos.write(buff, 0, eachLen);
                } else {
                    baos.close();
                    throw new IOException();
                }
                if (len < size) {
                    buff = new byte[size - len];
                }
            }
            byte[] b = baos.toByteArray();
            baos.close();
            return b;
        }
        return null;
    }


}
