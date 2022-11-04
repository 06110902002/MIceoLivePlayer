package come.live.microliveplayer.list;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 16:04
 * Author      : rambo.liu
 * Description :
 */
public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int mScreenWidth;
    private int mItemWidth;

    public void setScreenWidthHeight(int width ,int itemWidth) {
        mScreenWidth = width;
        mItemWidth = itemWidth;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int singleSpace = (mScreenWidth - 4 * mItemWidth) / 4;
        outRect.left = singleSpace / 2;
        outRect.right = singleSpace / 2;
    }


}
