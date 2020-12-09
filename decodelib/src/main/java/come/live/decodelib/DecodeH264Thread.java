package come.live.decodelib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

/**
 * @author          hengyang.lxb
 * @date            2020/12/09
 * @Version:        1.0
 * @Description:    解码线程
 */
public class DecodeH264Thread extends Thread {

    private LinkedBlockingQueue<byte[]> oriH264Queue;
    private MediaCodec mediaCodec;
    private boolean isStart = false;

    public DecodeH264Thread(LinkedBlockingQueue<byte[]> queue,int width,int height, Surface surface){
        oriH264Queue = queue;
        initMediaCodec(width,height,surface);
    }

    /**
     * 初始化解码器
     * @param width   宽度
     * @param height  高度
     * @param surface 渲染显示对象
     */
    private void initMediaCodec(int width,int height, Surface surface){
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, height);
        format.setInteger(MediaFormat.KEY_MAX_WIDTH, width);
        //format.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
        //format.setByteBuffer("csd-1", ByteBuffer.wrap(mPps));
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();
            isStart = true;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = false;
        }
    }

    @Override
    public void run() {
        super.run();
        decodeH264();
    }

    /**
     * 停止线程任务并释放解码器相关对象
     */
    public void stopDecodeH264(){
        isStart = false;
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if(oriH264Queue != null){
            oriH264Queue.clear();
            oriH264Queue = null;
        }

    }

    private void decodeH264(){
        while (isStart){
            while (!oriH264Queue.isEmpty()){
                byte[] frame = oriH264Queue.poll();
                if(frame != null && frame.length > 0){
                    onFrame(frame,0,frame.length);
                }else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * 解码
     * @param buf    待解码的一帧数据
     * @param offset 起始位置，本接口起始位置为0
     * @param length 解码长度
     * @return       解码结果
     */
    public boolean onFrame(byte[] buf, int offset, int length) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, System.currentTimeMillis(), 0);
        } else {
            try {
                //无可用缓存时，休眠100ms
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);

        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        }
        return true;
    }
}
