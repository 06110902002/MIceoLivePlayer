package come.live.decodelib.video;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import come.live.decodelib.utils.LogUtils;

/**
 * author         hengyang.lxb
 * date           2020/12/28
 * Version:       1.0
 * Description:   视频播放，主要实现h264的解码工作
 */
public class VideoPlay {

    private MediaCodec mediaCodec;
    private boolean isStart = false;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private String mediaCodecStatus;
    private PlayerThread playerThread;

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
            mInputBuffers = mediaCodec.getInputBuffers();
            mOutputBuffers = mediaCodec.getOutputBuffers();
            playerThread = new PlayerThread();
            playerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            isStart = false;
        }
        return isStart;
    }

    /**
     * 解码,先将h264放入输入队列，再遍历解码,本接口已经过时
     * @param buf    待解码的一帧数据
     * @param offset 起始位置，本接口起始位置为0
     * @param length 解码长度
     * @return       解码结果
     */
    @Deprecated
    public boolean onFrame(byte[] buf, int offset, int length) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            if(length > inputBuffer.capacity()){
                String log = String.format("当前待存入缓存视频帧长度大于%d 缓存inputBuffer最大容量：%d",length,inputBuffer.capacity());
                LogUtils.v(log);
                return false;
            }
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

    /**
     * 将H264帧放入输入队列，等待解码
     *
     * @param h264Buffer 原始h264数据
     */
    public void putH264InputBuffer(byte[] h264Buffer) {
        if (h264Buffer == null || h264Buffer.length <= 0) {
            return;
        }
        if (mediaCodec == null) {
            return;
        }
        for (; ; ) {
            int idx = mediaCodec.dequeueInputBuffer(10000);//10ms
            if (idx >= 0) {
                ByteBuffer input = mInputBuffers[idx];
                int length = h264Buffer.length;
                input.clear();
                input.put(h264Buffer);
                mediaCodec.queueInputBuffer(idx, 0, length, 0, 0);
                break;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogUtils.v("queueInputBuffer failed: idx=" + idx);
            }
        }

    }

    /**
     * 从MediaCodec中输入队列获取待解码的h264数据进行解码
     *
     * @param bufferInfo MediaCodec中输入队列数据
     */
    public void onFrame(MediaCodec.BufferInfo bufferInfo){
        int idx = mediaCodec.dequeueOutputBuffer(bufferInfo, 50000);
        switch (idx) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                LogUtils.v( "INFO_OUTPUT_BUFFERS_CHANGED");
                mOutputBuffers = mediaCodec.getOutputBuffers();
                break;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                LogUtils.v( "New format " + mediaCodec.getOutputFormat());
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                LogUtils.v( "dequeueOutputBuffer timed out!");
                Thread.yield();
                break;
            default:
                ByteBuffer buffer = mOutputBuffers[idx];
                mediaCodec.releaseOutputBuffer(idx, true);
                break;
        }
    }


    private class PlayerThread extends Thread{

        @Override
        public void run() {
            super.run();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (isStart){
                onFrame(bufferInfo);
            }

        }

    }

    public void release(){
        isStart = false;
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if(playerThread != null){
            playerThread = null;
        }

    }

}
