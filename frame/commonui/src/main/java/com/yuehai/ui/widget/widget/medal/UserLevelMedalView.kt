package com.yuehai.ui.widget.widget.medal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.adealik.frame.base.ext.fastLazy
import com.yuehai.ui.databinding.CommonUserMedalViewBinding
import com.yuehai.ui.widget.recyclerview.HorizontalSpaceItemDecoration
import com.yuehai.ui.widget.widget.level.UserLevelMedalAdapter
import com.yuehai.util.util.ext.dp

//用户等级和勋章
class UserLevelMedalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding =
        CommonUserMedalViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val medalAdapter by fastLazy {
        UserLevelMedalAdapter()
    }

    init {
        binding.recyclerviewMedal.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalSpaceItemDecoration(4.dp()))
                adapter = medalAdapter
            }
        }
    }

    fun setLevelMedalInfo(
        vipLevel: String? = "",
        wealthLevel: String? = "",
        charmLevel: String? = "",
        medalList: List<String>? = ArrayList(),
    ) {

        val levelList = mutableListOf<String>()
        if (vipLevel?.isEmpty()?.not() == true) {
            levelList.add(vipLevel)
        }
        if (wealthLevel?.isEmpty()?.not() == true) {
            levelList.add(wealthLevel)
        }
        if (charmLevel?.isEmpty()?.not() == true) {
            levelList.add(charmLevel)
        }
        medalList?.let {
            levelList.addAll(it)
        }

        medalAdapter.setNewInstance(levelList.toMutableList())
    }
}