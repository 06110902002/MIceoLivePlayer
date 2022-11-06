package come.live.decodelib.video;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

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
    private BlockingQueue<byte[]> packets = new LinkedBlockingQueue<>(10);
    private final HandlerThread mDecodeThread = new HandlerThread("VideoDecoder");

    /**
     * 初始化解码器
     * @param width   宽度
     * @param height  高度
     * @param surface 渲染显示对象
     * @return 返回初始化结果
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initMediaCodec(int width, int height, Surface surface){
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
//        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
//        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, height);
//        format.setInteger(MediaFormat.KEY_MAX_WIDTH, width);
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
            //使用同步的方式解码渲染
//            mDecodeThread.start();
//            mediaCodec.setCallback(mDecoderCallback, new Handler(mDecodeThread.getLooper()));
            mediaCodec.start();
            //使用  putH264InputBuffer  接口需要将这里打开
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

    //添加同步的方式 渲染  使用下列代码
    private final MediaCodec.Callback mDecoderCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            try {
                byte[] h264 = packets.take();
                codec.getInputBuffer(index).put(h264);
                mediaCodec.queueInputBuffer(index, 0, h264.length, System.currentTimeMillis(), 0);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted when is waiting");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            try {
                codec.releaseOutputBuffer(index, true);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

        }
    };
    public void addH264Packer(byte[] nalPacket) {
        try {
            packets.put(nalPacket);
        } catch (InterruptedException e) {
            LogUtils.e("队列满了:", e);
        }
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
        int index = mediaCodec.dequeueInputBuffer(-1);
        if (index >= 0) {
            ByteBuffer buffer;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                buffer = mediaCodec.getInputBuffers()[index];
                buffer.clear();
            } else {
                buffer = mediaCodec.getInputBuffer(index);
            }
            if (buffer != null) {
                buffer.put(h264Buffer, 0, h264Buffer.length);
                mediaCodec.queueInputBuffer(index, 0, h264Buffer.length, 0, 0);
            }
        }

    }

    /**
     * 从MediaCodec中输入队列获取待解码的h264数据进行解码
     *
     * @param bufferInfo MediaCodec中输入队列数据
     */
    public void onFrame(MediaCodec.BufferInfo bufferInfo){
        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (isStart) {
                int index = mediaCodec.dequeueOutputBuffer(info, 0);
                if (index >= 0) {
                    // setting true is telling system to render frame onto Surface
                    mediaCodec.releaseOutputBuffer(index, true);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        break;
                    }
                }
            }
        } catch (IllegalStateException e) {
        }
    }


    private class PlayerThread extends Thread{

        @Override
        public void run() {
            super.run();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (isStart && mediaCodec != null){
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
