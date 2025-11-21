package com.yuehai.ui.widget.recycleview.itemdecoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GalleryItemDecoration extends RecyclerView.ItemDecoration {

    private final int mPageMargin;
    private final int mLeftPageVisibleWidth;

    private final int mExtraTop;
    private final int mExtraBottom;

    public GalleryItemDecoration(int leftPageVisibleWidth, int pageMargin, int extraTop, int extraBottom) {
        mLeftPageVisibleWidth = leftPageVisibleWidth;
        mPageMargin = pageMargin;
        mExtraTop = extraTop;
        mExtraBottom = extraBottom;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();

        int leftMargin;
        if (position == 0) {
            leftMargin = mLeftPageVisibleWidth;
        } else {
            leftMargin = mPageMargin;
        }

        int rightMargin;
        if (position == itemCount - 1) {
            rightMargin = mLeftPageVisibleWidth;
        } else {
            rightMargin = mPageMargin;
        }

        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
        lp.setMargins(leftMargin, 0, rightMargin, 0);
        view.setLayoutParams(lp);

        super.getItemOffsets(outRect, view, parent, state);

        if (mExtraTop != -1) {
            outRect.top += mExtraTop;
        }

        if (mExtraBottom != -1) {
            outRect.bottom += mExtraBottom;
        }
    }
}
