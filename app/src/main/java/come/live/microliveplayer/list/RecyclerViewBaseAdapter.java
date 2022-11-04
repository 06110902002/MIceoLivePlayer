package come.live.microliveplayer.list;

import android.content.Context;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import come.live.microliveplayer.list.model.ViewDataType;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/17 18:53
 * Author      : rambo.liu
 * Description :
 */
public class RecyclerViewBaseAdapter extends RecyclerView.Adapter<ViewHolder> {


    protected Context context;
    private List<ViewDataType> dataList;

    public RecyclerViewBaseAdapter(Context context) {
        this.context = context;
    }

    public void addData(List<? extends ViewDataType> append) {
        getDataList().addAll(append);
    }


    public void appendData(ViewDataType BaseData){
        getDataList().add(BaseData);
    }


    public void addData(ViewDataType vhModel) {
        List<ViewDataType> list = new ArrayList<>();
        list.add(vhModel);
        addData(list);
    }

    public void clean() {
        getDataList().clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        if (viewType == Constant.EMPTY_TIPS) {
//
//            return new EmptyView(context, parent);
//        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(getDataList().get(position));
    }

    @Override
    public int getItemCount() {
        return getDataList().size();
    }

    /**
     * 获取视图类型，从数据中获取
     * 即从数据中设定一个类型用来区分不同的视图
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return getDataList().get(position).getItemType();
    }

    public List<ViewDataType> getDataList() {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        return dataList;
    }

}
