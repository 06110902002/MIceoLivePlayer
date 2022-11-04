package come.live.microliveplayer.list.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import come.live.decodelib.MsgCenterMgr;
import come.live.decodelib.model.LiveEntity;
import come.live.decodelib.utils.ByteUtil;
import come.live.decodelib.utils.LogUtils;
import come.live.decodelib.utils.UIUtils;
import come.live.microliveplayer.R;
import come.live.microliveplayer.config.Constants;
import come.live.microliveplayer.list.ViewHolder;
import come.live.microliveplayer.list.model.AppItemInfo;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/17 19:20
 * Author      : rambo.liu
 * Description :
 */
public class AppItemView extends ViewHolder<AppItemInfo> {

    private ImageView icon;
    private TextView text;
    private AppItemInfo appItemData;

    public AppItemView(Context context, ViewGroup parent) {
        super(context, parent, R.layout.layout_app_item);
        mContext = context;

    }

    @Override
    public void setData(AppItemInfo data) {
        if (appItemData != data) {
            appItemData = data;
            Bitmap bitmap = UIUtils.base642Bitmap(appItemData.getIcon());
            if (bitmap != null) {
                icon.setImageBitmap(bitmap);
            }
            text.setText(appItemData.getName());
            icon.setOnClickListener(new EventClick(appItemData.getPkgName(),appItemData.getActivityInfo(),data.getPageIdx()));

        }
    }

    @Override
    public void findView(View itemView) {
        icon = itemView.findViewById(R.id.app_icon);
        text = itemView.findViewById(R.id.app_title);
    }

    private class EventClick implements View.OnClickListener {

        private String activity;
        private String pkgName;
        private int pageIdx;

        public EventClick(String pkgName,String activity,int pageIdx) {
            this.activity = activity;
            this.pkgName = pkgName;
            this.pageIdx = pageIdx;
        }

        @Override
        public void onClick(View v) {
            openApp(pkgName,activity);
        }

        private void openApp(String packageName,String activity) {
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activity)) {
                LiveEntity liveEntity = new LiveEntity();
                liveEntity.setType(ByteUtil.int2Bytes(LiveEntity.STRAT_CODEC));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("pkgName",packageName);
                    jsonObject.put("activity",activity);
                    jsonObject.put("pageIdx",pageIdx);
                    LogUtils.v("启动包名 = " + packageName + " activity :" + activity + " pageIdx:" + pageIdx);
                    byte[] content = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                    liveEntity.setContentLength(ByteUtil.int2Bytes(content.length));
                    liveEntity.setContent(content);
                    MsgCenterMgr.getInstance().stopLastEncoder(pageIdx == 0 ? 1 : 0);
                    MsgCenterMgr.getInstance().sendMsg(liveEntity);
                    sendBroadcast(pageIdx);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendBroadcast(int pageIdx) {
        Intent intent = new Intent();
        intent.putExtra(Constants.PAGE_IDX_KEY,pageIdx);
        intent.setAction(Constants.PAGE_IDX_ACTION);
        mContext.sendBroadcast(intent);
    }
}