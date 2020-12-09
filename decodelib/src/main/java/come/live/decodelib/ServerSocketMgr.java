package come.live.decodelib;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

/**
 * @author liuxiaobing
 * @date 2020/12/09
 * @Version: 1.0
 * @Description:
 */
public class ServerSocketMgr {


    private volatile static ServerSocketMgr serverSocketMgr;

    public static ServerSocketMgr getInstance(){
        if(serverSocketMgr == null){
            synchronized (ServerSocketMgr.class){
                serverSocketMgr = new ServerSocketMgr();
            }
        }
        return serverSocketMgr;
    }

    public void startReadMsg(){

    }



}
