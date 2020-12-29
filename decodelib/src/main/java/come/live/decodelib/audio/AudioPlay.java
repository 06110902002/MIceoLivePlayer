package come.live.decodelib.audio;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioTrack;
import android.media.MediaCodec;
import android.os.Build;
import come.live.decodelib.utils.LogUtils;

/**
 * author          hengyang.lxb
 * date            2020/12/09
 * Version:        1.0
 * Description:    音频解码并播放
 */
public class AudioPlay {

    private MediaCodec mAudioMediaCodec;
    private AudioTrack mAudioTrack;
    private int count = 0;

    private LinkedBlockingQueue<byte[]> audioQueue;
    private boolean isStartPlay = false;
    private PlayThread playThread;

    public AudioPlay() {
        this.mAudioMediaCodec = AudioMediaCodec.getAudioMediaCodec();
        this.mAudioTrack = AudioMediaCodec.getAudioTrack();
        this.mAudioMediaCodec.start();
        this.mAudioTrack.play();


        audioQueue = new LinkedBlockingQueue<>();
        isStartPlay = true;
        playThread = new PlayThread();
        playThread.start();
    }

    public void playAudio(byte[] buf, int offset, int length) {
        //输入ByteBuffer
        ByteBuffer[] codecInputBuffers = mAudioMediaCodec.getInputBuffers();
        //输出ByteBuffer
        ByteBuffer[] codecOutputBuffers = mAudioMediaCodec.getOutputBuffers();
        //等待时间，0->不等待，-1->一直等待
        long kTimeOutUs = 0;
        try {
            //返回一个包含有效数据的input buffer的index,-1->不存在
            int inputBufIndex = mAudioMediaCodec.dequeueInputBuffer(kTimeOutUs);
            if (inputBufIndex >= 0) {
                //获取当前的ByteBuffer
                ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                //清空ByteBuffer
                dstBuf.clear();
                //填充数据
                dstBuf.put(buf, offset, length);
                //将指定index的input buffer提交给解码器
                mAudioMediaCodec.queueInputBuffer(inputBufIndex, 0, length, 0, 0);
            }
            //编解码器缓冲区
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            //返回一个output buffer的index，-1->不存在
            int outputBufferIndex = mAudioMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

            if (outputBufferIndex < 0) {
                //记录解码失败的次数
                count++;
            }

            ByteBuffer outputBuffer;
            while (outputBufferIndex >= 0) {
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        LogUtils.v( "INFO_OUTPUT_BUFFERS_CHANGED");
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        LogUtils.v("INFO_OUTPUT_FORMAT_CHANGE");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        LogUtils.v( "INFO_TRY_AGAIN_LATER");
                        break;
                }
                //---------------------------------------------------------------
                //获取解码后的ByteBuffer
                outputBuffer = codecOutputBuffers[outputBufferIndex];
                //用来保存解码后的数据
                byte[] outData = new byte[info.size];
                outputBuffer.get(outData);
                //清空缓存
                outputBuffer.clear();
                //播放解码后的数据
                //mAudioTrack.write(outData, 0, info.size);
                audioQueue.put(outData);


//                Log.e("DecodeThread", "buff length = " + info.size);
                //释放已经解码的buffer
                mAudioMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                //解码未解完的数据
                outputBufferIndex = mAudioMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);
                //--------------------------------------------------------
            }
        } catch (Exception e) {
            LogUtils.v( e.toString());
            e.printStackTrace();
        }
    }

    public int getCount() {
        return count;
    }


    public void release() {
        if (mAudioMediaCodec != null) {
            mAudioMediaCodec.stop();
            mAudioMediaCodec.release();
            mAudioMediaCodec = null;
        }
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    private class PlayThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(isStartPlay){
                while (!audioQueue.isEmpty() && mAudioTrack != null){
                    byte[] audioFrame = audioQueue.poll();
                    mAudioTrack.write(audioFrame, 0, audioFrame.length);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(interrupted()){
                    try {
                        throw new InterruptedException();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isStartPlay = false;
                    }
                }
            }
        }
    }
}
