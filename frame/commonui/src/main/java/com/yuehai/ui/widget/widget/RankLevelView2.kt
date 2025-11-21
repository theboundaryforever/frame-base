package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.adealik.frame.base.ext.fastLazy
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.yuehai.data.collection.path.json.toJsonErrorNull
import com.yuehai.data.collection.path.user.ChatMessageListData
import com.yuehai.data.collection.path.user.ChatMessageListItemDiffUtil
import com.yuehai.data.collection.path.user.UserInfo
import com.yuehai.ui.R
import com.yuehai.ui.databinding.RankViewLevel2Binding
import com.yuehai.ui.widget.recycleview.adapter.MultiTypeListAdapter
import com.yuehai.ui.widget.widget.level.UserLevelAdapter2
import com.yuehai.ui.widget.widget.level.UserLevelMedalBinder
import com.yuehai.ui.widget.widget.level.UserLevelNicknameBinder
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.show
import com.yuehai.util.util.getCompatColor

class RankLevelView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = RankViewLevel2Binding.inflate(LayoutInflater.from(context), this, true)

    private var rankLevelAdapter: MultiTypeListAdapter<ChatMessageListData> =
        MultiTypeListAdapter(ChatMessageListItemDiffUtil())

    init {
        rankLevelAdapter.register(UserLevelNicknameBinder())
        rankLevelAdapter.register(UserLevelAdapter2())
        rankLevelAdapter.register(UserLevelMedalBinder())
    }

    fun setLevel2(
        vipLevel: String? = "",
        wealthLevel: String? = "",
        charmLevel: String? = "",
        identityTagList: List<String> = ArrayList(),
        medalList: List<String> = ArrayList(),
        center: Boolean = true,
        userInfo: UserInfo? = null,
        labelHeight: Int = 18.dp(),
        medalHeight: Int = 18.dp()
    ) {
        binding.tvPlace.gone()
        val levelList = mutableListOf<ChatMessageListData>()
        if (userInfo != null && userInfo.id > 0) {
            levelList.add(UserInfoLevel(userInfo))
        }

        if (!vipLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(vipLevel, labelHeight = labelHeight))
        }

        if (!wealthLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(wealthLevel, labelHeight = labelHeight))
        }
        if (!charmLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(charmLevel, labelHeight = labelHeight))
        }

        identityTagList.forEach {
            if (it.isNotEmpty()) {
                levelList.add(LevelViewInfo(it, labelHeight = labelHeight))
            }

        }
        medalList.forEach {
            if (it.isNotEmpty()) {
                levelList.add(LevelMedalViewInfo(it, medalHeight))
            }
        }
        val flexboxTimeLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

            justifyContent = if (center) {
                JustifyContent.CENTER
            } else {
                JustifyContent.FLEX_START
            }
            alignItems = AlignItems.FLEX_START
        }

        binding.recyclerviewTag.apply {
            layoutManager = flexboxTimeLayoutManager
            adapter = rankLevelAdapter
        }
        if (levelList.isEmpty()) {
            this.gone()
        } else {
            rankLevelAdapter.submitList(levelList)
            this.show()
        }

    }

    fun setLevel3(
        vipLevel: String? = "",
        wealthLevel: String? = "",
        charmLevel: String? = "",
        identityTagList: List<String> = ArrayList(),
        center: Boolean = true,
        userInfo: UserInfo? = null,
        labelHeight: Int = 18.dp()
    ) {
        binding.tvPlace.gone()
        val levelList = mutableListOf<ChatMessageListData>()
        if (userInfo != null) {
            levelList.add(UserInfoLevel(userInfo))
        }

        if (!vipLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(vipLevel, labelHeight = labelHeight))
        }

        if (!wealthLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(wealthLevel, labelHeight = labelHeight))
        }
        if (!charmLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(charmLevel, labelHeight = labelHeight))
        }

        identityTagList.forEach {
            if (it.isNotEmpty()) {
                levelList.add(LevelViewInfo(it, labelHeight = labelHeight))
            }

        }
        val flexboxTimeLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

            justifyContent = if (center) {
                JustifyContent.CENTER
            } else {
                JustifyContent.FLEX_START
            }
            alignItems = AlignItems.FLEX_START
        }

        binding.recyclerviewTag.apply {
            layoutManager = flexboxTimeLayoutManager
            adapter = rankLevelAdapter
        }
        rankLevelAdapter.submitList(levelList)
        if (levelList.isEmpty()) {
            this.gone()
        } else {

            this.show()
        }

    }

    fun setEmptyLevel() {
        binding.tvPlace.show()
        binding.tvPlace.setTextColor(getCompatColor(R.color.color_FFFFFF))
        binding.tvPlace.text = viewPlace
    }
}
