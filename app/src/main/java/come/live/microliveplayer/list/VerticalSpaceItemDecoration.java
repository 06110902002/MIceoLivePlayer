package come.live.microliveplayer.list;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 15:00
 * Author      : rambo.liu
 * Description :
 */
public class VerticalSpaceItemDecoration  extends RecyclerView.ItemDecoration {

    private int mScreenHeight;
    private int mItemHeight;

    public void setScreenWidthHeight(int width ,int itemHeight) {
        mScreenHeight = width;
        mItemHeight = itemHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int singleSpace = (mScreenHeight - 4 * mItemHeight) / 4;
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = singleSpace;
            outRect.bottom = singleSpace / 2;
        } else if (parent.getChildAdapterPosition(view) == 3) {
            outRect.bottom = singleSpace;
        } else {
            outRect.top = singleSpace / 2;
            outRect.bottom = singleSpace / 2;
        }

        //outRect.top = singleSpace;

    }
}
