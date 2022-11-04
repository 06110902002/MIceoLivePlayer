package come.live.microliveplayer.list.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import come.live.decodelib.utils.UIUtils;
import come.live.microliveplayer.R;
import come.live.microliveplayer.list.AppAdapter;
import come.live.microliveplayer.list.HorizontalSpaceItemDecoration;
import come.live.microliveplayer.list.ViewHolder;
import come.live.microliveplayer.list.model.AppPageItem;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 16:42
 * Author      : rambo.liu
 * Description :
 */
public class AppWholePageItemView extends ViewHolder<AppPageItem> {

    private RecyclerView wholePageItemList;
    private AppPageItem appItemData;
    private AppAdapter appAdapter;


    public AppWholePageItemView(Context context, ViewGroup parent) {
        super(context, parent, R.layout.layout_middle_whole_page);
        mContext = context;

    }

    @Override
    public void setData(AppPageItem data) {
        if (appItemData != data) {
            appItemData = data;
            appAdapter.addData(appItemData.getAppItemInfo());
            appAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void findView(View itemView) {
        wholePageItemList = itemView.findViewById(R.id.whole_page_item_list);
        appAdapter = new AppAdapter(mContext);
        wholePageItemList.setLayoutManager(new GridLayoutManager(mContext,4));
        wholePageItemList.setAdapter(appAdapter);

        HorizontalSpaceItemDecoration space = new HorizontalSpaceItemDecoration();
        int screenWidth = UIUtils.getScreenWidth((Activity) mContext);
        int itemWith = UIUtils.dp2px( mContext,90);
        space.setScreenWidthHeight(screenWidth,itemWith);
        wholePageItemList.addItemDecoration(space);
    }
}