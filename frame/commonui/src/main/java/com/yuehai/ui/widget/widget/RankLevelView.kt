package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuehai.data.collection.path.user.ChatMessageListData
import com.yuehai.data.collection.path.user.ChatMessageListItemDiffUtil
import com.yuehai.data.collection.path.user.UserInfo
import com.yuehai.ui.R
import com.yuehai.ui.databinding.RankViewLevel2Binding
import com.yuehai.ui.widget.recycleview.adapter.MultiTypeListAdapter
import com.yuehai.ui.widget.recycleview.layoutmanager.FlowLayoutManager
import com.yuehai.ui.widget.widget.level.UserLevelAdapter
import com.yuehai.ui.widget.widget.level.UserLevelNicknameBinder
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.show
import com.yuehai.util.util.getCompatColor

class RankLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = RankViewLevel2Binding.inflate(LayoutInflater.from(context), this, true)

    private var rankLevelAdapter: MultiTypeListAdapter<ChatMessageListData> =
        MultiTypeListAdapter(ChatMessageListItemDiffUtil())

    init {
        rankLevelAdapter.register(UserLevelNicknameBinder())
        rankLevelAdapter.register(UserLevelAdapter())
    }

    fun setLevel(
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
            levelList.add(LevelViewInfo(vipLevel, labelHeight=labelHeight))
        }

        if (!wealthLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(wealthLevel, labelHeight=labelHeight))
        }
        if (!charmLevel.isNullOrEmpty()) {
            levelList.add(LevelViewInfo(charmLevel, labelHeight=labelHeight))
        }
        identityTagList.forEach {
            if (it.isNotEmpty()) {
                levelList.add(LevelViewInfo(it))
            }

        }


        val centerLayoutManager = FlowLayoutManager(
            LinearLayoutManager.VERTICAL,
            if (center) {
                Gravity.CENTER_HORIZONTAL
            } else {
                0
            },
            4.dp(),
            6.dp()
        )
        binding.recyclerviewTag.apply {
            layoutManager = centerLayoutManager
            adapter = rankLevelAdapter
        }

        if (wealthLevel.isNullOrEmpty() && charmLevel.isNullOrEmpty() && vipLevel.isNullOrEmpty()) {
            this.gone()
        } else {
            this.show()
        }
        rankLevelAdapter.submitList(levelList)
    }


    fun setEmptyLevel() {
        binding.tvPlace.show()
        binding.tvPlace.setTextColor(getCompatColor(R.color.color_FFFFFF))
        binding.tvPlace.text = viewPlace
    }
}

data class LevelViewInfo(
    val levelUrl: String, val vipLevel: Boolean = false,
    val adminLevel: Boolean = false,
    var labelHeight: Int = 18.dp()
) : ChatMessageListData {
    override fun areItemsTheSame(newItem: ChatMessageListData): Boolean {
        return newItem is LevelViewInfo
    }

    override fun areContentsTheSame(newItem: ChatMessageListData): Boolean {
        if (newItem !is LevelViewInfo) {
            return false
        }
        return this.levelUrl ==
                newItem.levelUrl && this.vipLevel == newItem.vipLevel && this.adminLevel == newItem.adminLevel
    }

}

data class LevelMedalViewInfo(
    val levelUrl: String,
    var labelHeight: Int = 18.dp()
) : ChatMessageListData {
    override fun areItemsTheSame(newItem: ChatMessageListData): Boolean {
        return newItem is LevelViewInfo
    }

    override fun areContentsTheSame(newItem: ChatMessageListData): Boolean {
        if (newItem !is LevelViewInfo) {
            return false
        }
        return this.levelUrl ==
                newItem.levelUrl
    }

}

data class UserInfoLevel(val info: UserInfo) : ChatMessageListData {
    override fun areItemsTheSame(newItem: ChatMessageListData): Boolean {
        return newItem is UserInfoLevel
    }

    override fun areContentsTheSame(newItem: ChatMessageListData): Boolean {
        if (newItem !is UserInfoLevel) {
            return false
        }
        return this.info.id == newItem.info.id && this.info.vipLevel == newItem.info.vipLevel &&
                this.info.wealthLevel == newItem.info.wealthLevel && this.info.charmingLevel == newItem.info.charmingLevel &&
                this.info.voiceUid == newItem.info.voiceUid && this.info.nickname == newItem.info.nickname

    }

}