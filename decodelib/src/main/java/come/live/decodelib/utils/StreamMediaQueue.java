package come.live.decodelib.utils;

import java.util.concurrent.LinkedBlockingQueue;

import come.live.decodelib.model.VideoFrame;

/**
 * @author        hengyang.lxb
 * @date          2020/12/18
 * @Version:      1.0
 * @Description:  流媒体队列管理器
 */
public class StreamMediaQueue extends LinkedBlockingQueue<VideoFrame> {

    /**
     * 丢帧的阈值，当队列里面的关健帧个数大于 5时，
     */
    private final int abondFrameThreshold = 5;
    private int curKeyFrameCount = 0;
    @Override
    public void put(VideoFrame o) throws InterruptedException {
        super.put(o);
        curKeyFrameCount ++;
        if(curKeyFrameCount > abondFrameThreshold){
            LogUtils.v("队列中关键帧个数大于5，开始丢帧，丢帧策略为只保留队列中的后5个关键帧.....");
            while(this.size() > abondFrameThreshold){
                VideoFrame frame = this.peek();
                if(frame != null && frame.getType() != 1){
                    this.poll();
                }
            }
        }
    }


}
