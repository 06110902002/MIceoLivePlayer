package come.live.decodelib;

import java.util.concurrent.LinkedBlockingQueue;

import come.live.decodelib.audio.AudioPlay;
import come.live.decodelib.audio.AudioPlayer;

/**
 * author         hengyang.lxb
 * date           2020/12/26
 * Version:       1.0
 * Description:
 */
public class TestAudioThread extends Thread {

    private LinkedBlockingQueue<byte[]> audioQueue;
    private AudioPlay audioPlay;
    private AudioPlayer audioPlayer;

    public TestAudioThread(LinkedBlockingQueue<byte[]> audioQueue){
        this.audioQueue = audioQueue;
        audioPlay = new AudioPlay();
        audioPlayer = new AudioPlayer();
        audioPlayer.startPlayer();
    }

    @Override
    public void run() {
        super.run();
        while(true){
            while(audioQueue != null && !audioQueue.isEmpty()){
                byte[] audioAACBytes = audioQueue.poll();
                audioPlay.playAudio(audioAACBytes,0,audioAACBytes.length);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
