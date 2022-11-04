package come.live.microliveplayer.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/17 18:55
 * Author      : rambo.liu
 * Description :
 */
public abstract class ViewHolder<T> extends RecyclerView.ViewHolder {

    protected Context mContext;

    public ViewHolder(Context context, ViewGroup parent, int layoutId) {
        super(LayoutInflater.from(context).inflate(layoutId, parent, false));
        mContext = context;
        findView(itemView);
    }

    public ViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 设置数据的接口，即将数据与View联系起来
     *
     * @param data
     */
    public abstract void setData(T data);

    /**
     * 与适配器对应的控件初始化操作，需要子类扩展
     *
     * @param itemView
     */
    public abstract void findView(View itemView);


}
