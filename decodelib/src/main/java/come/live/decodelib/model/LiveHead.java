package come.live.decodelib.model;

import java.io.Serializable;

/**
 * Author:      hengyang
 * CreateDate:  2020/12/5 6:46 PM
 * Version:     1.0
 * Description:
 */
public class LiveHead implements Serializable {

    /**
     * 协议类型
     */
    private int type;

    /**
     * 报文长度
     */
    private int buffSize;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void setBuffSize(int buffSize) {
        this.buffSize = buffSize;
    }

    public LiveHead(int type,int buffSize){
        this.type = type;
        this.buffSize = buffSize;
    }

}
