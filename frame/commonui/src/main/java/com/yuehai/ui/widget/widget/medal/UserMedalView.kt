package com.yuehai.ui.widget.widget.medal

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.yuehai.ui.databinding.CommonUserMedalViewBinding
import com.yuehai.ui.widget.GridSpacingItemDecoration
import com.yuehai.ui.widget.recycleview.layoutmanager.CenterGridLayoutManger
import com.yuehai.util.util.ext.dp

class UserMedalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding =
        CommonUserMedalViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun setMedalInfo(
        medalList: List<String>? = ArrayList(),
        spanCount: Int = 6,
        horizonalSpace: Int = 6.dp(),
        verticalSpace: Int = 6.dp(),
        viewWidth: Int = 38.dp(),
        includeEdge: Boolean = false,
        gravityCenter: Boolean = false
    ) {
        val medalAdapter = UserMedalAdapter(viewWidth)
        binding.recyclerviewMedal.apply {
            if (gravityCenter) {
                layoutManager = CenterGridLayoutManger(spanCount)
                addItemDecoration(
                    GridSpacingItemDecoration(
                        spanCount,
                        horizonalSpace,
                        verticalSpace,
                        includeEdge
                    )
                )
            } else {
                layoutManager = GridLayoutManager(context, spanCount)
                addItemDecoration(
                    GridSpacingItemDecoration(
                        spanCount,
                        horizonalSpace,
                        verticalSpace,
                        includeEdge
                    )
                )
            }

            adapter = medalAdapter
        }
        medalAdapter.setNewInstance(medalList?.toMutableList())
    }
}