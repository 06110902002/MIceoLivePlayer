package come.live.microliveplayer.list.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import come.live.microliveplayer.R;
import come.live.microliveplayer.list.AppAdapter;
import come.live.microliveplayer.list.ViewHolder;
import come.live.microliveplayer.list.model.AppPageItem;


/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 14:25
 * Author      : rambo.liu
 * Description :
 */
public class AppPageItemView extends ViewHolder<AppPageItem> {

    private RecyclerView pageItemList;
    private AppPageItem appItemData;
    private AppAdapter appAdapter;


    public AppPageItemView(Context context, ViewGroup parent) {
        super(context, parent, R.layout.layout_middle_app_page);
        mContext = context;

    }

    @Override
    public void setData(AppPageItem data) {
        if (appItemData != data) {
            appItemData = data;
            appAdapter.getDataList().clear();
            appAdapter.addData(appItemData.getAppItemInfo());
            appAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void findView(View itemView) {
        pageItemList = itemView.findViewById(R.id.page_item_list);
        appAdapter = new AppAdapter(mContext);
        pageItemList.setLayoutManager(new GridLayoutManager(mContext,4));
        pageItemList.setAdapter(appAdapter);

    }
}