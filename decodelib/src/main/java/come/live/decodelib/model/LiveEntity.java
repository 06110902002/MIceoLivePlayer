package come.live.decodelib.model;

import java.io.Serializable;

/**
 * Author:      hengyang
 * CreateDate:  2020/11/28 8:43 PM
 * Version:     1.0
 * Description: 推流直播报文
 */
public class LiveEntity implements Serializable {

    /**
     * pps帧
     */
    public final static int PPS = 22;

    /**
     * sps
     */
    public final static int SPS = 23;


    /**
     * 关键帧
     */
    public final static int IDR = 24;

    /**
     * 普通帧
     */
    public final static int NORMAL_FRAME   = 25;

    /**
     * 音频帧
     */
    public final static int AUDIO = 26;
    /**
     * 分辨率
     */
    public final static int RESOLUTION = 27;

    public final static int DISPLAY_ID = 28;

    public final static int STRAT_CODEC = 29;


    /**
     * 报文类型，用4个字节表示
     * 如音频：1000
     * 视频：1001
     * 普通报文：1002
     */
    private byte[] type;

    /**
     * surfaceView 显示Id
     */
    private byte[] surfaceViewIdx;

    /**
     * 报文长度，4个字节表示
     */
    private byte[] contentLength;

    /**
     * 报文内容
     */
    private byte[] content;


    public byte[] getType() {
        return type;
    }

    public void setType(byte[] type) {
        this.type = type;
    }

    public byte[] getSurfaceViewIdx() {
        return surfaceViewIdx;
    }

    public void setSurfaceViewIdx(byte[] surfaceViewIdx) {
        this.surfaceViewIdx = surfaceViewIdx;
    }

    public byte[] getContentLength() {
        return contentLength;
    }

    public void setContentLength(byte[] contentLength) {
        this.contentLength = contentLength;
    }


    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }



}
