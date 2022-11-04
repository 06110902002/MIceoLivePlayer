package come.live.microliveplayer.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import come.live.decodelib.utils.LogUtils;
import come.live.microliveplayer.config.Constants;
import come.live.microliveplayer.list.model.AppItemInfo;
import come.live.microliveplayer.list.model.ViewDataType;
import come.live.microliveplayer.list.view.AppItemView;


/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/17 19:19
 * Author      : rambo.liu
 * Description :
 */
public class AppAdapter extends RecyclerViewBaseAdapter {
    private Context mContext;

    public AppAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Constants.RIGHT_MENU_ITEM || viewType == Constants.APP_LIST_ITEM) {
            return new AppItemView(this.context, parent);
        }

        return super.onCreateViewHolder(parent, viewType);
    }
}
