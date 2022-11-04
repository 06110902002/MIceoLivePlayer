package come.live.microliveplayer.list;

import android.content.Context;
import android.view.ViewGroup;

import come.live.microliveplayer.config.Constants;
import come.live.microliveplayer.list.view.AppPageItemView;
import come.live.microliveplayer.list.view.AppWholePageItemView;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 14:24
 * Author      : rambo.liu
 * Description :
 */
public class AppPageAdapter extends RecyclerViewBaseAdapter {

    private Context mContext;

    public AppPageAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Constants.APP_PAGE_ITEM) {
            return new AppPageItemView(this.context, parent);
        } else if (viewType == Constants.APP_WHOLE_PAGE) {
            return new AppWholePageItemView(this.context, parent);
        }

        return super.onCreateViewHolder(parent, viewType);
    }
}
