package come.live.decodelib.video;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

/**
 * author         hengyang.lxb
 * date           2020/12/28
 * Version:       1.0
 * Description:   视频播放，主要实现h264的解码工作
 */
public class VideoPlay {

    private MediaCodec mediaCodec;
    private boolean isStart = false;

    /**
     * 初始化解码器
     * @param width   宽度
     * @param height  高度
     * @param surface 渲染显示对象
     * @return 返回初始化结果
     */
    public boolean initMediaCodec(int width,int height, Surface surface){
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, height);
        format.setInteger(MediaFormat.KEY_MAX_WIDTH, width);
        //byte[] header_sps = {0, 0, 0, 1, 103, 66, -128, 31, -38, 2, -48, 40, 104, 6, -48, -95, 53};
        //byte[] header_pps = {0, 0 ,0, 1, 104, -50, 6, -30};
        ////横屏
        //byte[] header_sps = {0, 0, 0, 1, 103, 66, -128, 31, -38, 1, 64, 22, -24, 6, -48, -95, 53};
        //byte[] header_pps = {0, 0 ,0, 1, 104, -50, 6, -30};
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
        return isStart;
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

    public void release(){
        isStart = false;
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

    }

}
