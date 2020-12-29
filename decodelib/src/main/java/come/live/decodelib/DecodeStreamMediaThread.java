package come.live.decodelib;

import java.util.concurrent.LinkedBlockingQueue;

import android.view.Surface;
import come.live.decodelib.audio.AudioPlay;
import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.video.VideoPlay;

/**
 * author         hengyang.lxb
 * date           2020/12/28
 * Version:       1.0
 * Description:   音频，视频解码线程
 */
public class DecodeStreamMediaThread extends Thread{

    private LinkedBlockingQueue<LiveEntity> liveEntitiesQueue;
    private static volatile DecodeStreamMediaThread decodeStreamMediaThread;
    private boolean isStart = false;
    private VideoPlay videoPlay;
    private AudioPlay audioPlay;


    public DecodeStreamMediaThread(){
        videoPlay = new VideoPlay();
        audioPlay = new AudioPlay();
    }

    /**
     * 设置待解码的队列
     * @param queue 待解码的流媒体队列
     */
    public void setStreamMediaQueue(LinkedBlockingQueue<LiveEntity> queue){
        liveEntitiesQueue = queue;
    }

    /**
     * 初始化视频解码器
     * @param width   宽度
     * @param height  高度
     * @param surface 渲染显示对象
     */
    public void initVideoMeidaCodec(int width,int height, Surface surface){
        isStart = videoPlay.initMediaCodec(width,height,surface);
    }

    @Override
    public void run() {
        super.run();
        decodec();
    }

    private void decodec(){
        while(isStart){
            if(interrupted()){
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isStart = false;
                }
            }
            while(!liveEntitiesQueue.isEmpty()){
                LiveEntity liveEntity = liveEntitiesQueue.poll();
                if (liveEntity != null) {

                    if (liveEntity.getType() == LiveEntity.AUDIO) {
                        audioPlay.playAudio(liveEntity.getContent(),0,liveEntity.getContent().length);
                    }else {
                        videoPlay.onFrame(liveEntity.getContent(),0,liveEntity.getContent().length);
                    }

                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                isStart = false;
            }
        }
    }

    public void stopDecodec(){
        isStart = false;
        if(liveEntitiesQueue != null){
            liveEntitiesQueue.clear();
            liveEntitiesQueue = null;
        }

        if(videoPlay != null){
            videoPlay.release();
            videoPlay = null;
        }

        if(audioPlay != null){
            audioPlay.release();
            audioPlay = null;
        }
    }
}
