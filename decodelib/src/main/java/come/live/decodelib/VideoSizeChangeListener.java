package come.live.decodelib;

import org.json.JSONObject;

/**
 * author         hengyang.lxb
 * date           2021/01/02
 * Version:       1.0
 * Description:   视频分辨率变化监听器
 */
public interface VideoSizeChangeListener {

    void onVideoSizeChange(String jsonString);
}
