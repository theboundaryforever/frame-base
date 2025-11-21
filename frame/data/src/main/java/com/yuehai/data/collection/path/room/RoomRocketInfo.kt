package com.yuehai.data.collection.path.room

import androidx.recyclerview.widget.DiffUtil
import com.yuehai.data.collection.path.json.toJsonErrorNull
import java.util.ArrayList

interface RocketRewardDiffUtil {
    fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean
    fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean
}

class RocketRewardItemDiffUtil : DiffUtil.ItemCallback<RocketRewardDiffUtil>() {

    override fun areItemsTheSame(
        oldItem: RocketRewardDiffUtil,
        newItem: RocketRewardDiffUtil
    ): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(
        oldItem: RocketRewardDiffUtil,
        newItem: RocketRewardDiffUtil
    ): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}

class RocketRewardFirstType(val reward: RewardItem) : RocketRewardDiffUtil {
    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem == this
    }

    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketRewardFirstType) {
            return false
        }
        return this.reward.coin == newItem.reward.coin && this.reward.name == newItem.reward.name &&
                this.reward.img == newItem.reward.img && this.reward.day == newItem.reward.day &&
                this.reward.type == newItem.reward.type
    }

}

class RocketRewardSecondType(val reward: RewardItem) : RocketRewardDiffUtil {
    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem == this
    }

    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketRewardSecondType) {
            return false
        }
        return this.reward.coin == newItem.reward.coin && this.reward.name == newItem.reward.name &&
                this.reward.img == newItem.reward.img && this.reward.day == newItem.reward.day &&
                this.reward.type == newItem.reward.type
    }

}

class RocketRewardThirdType(val reward: RewardItem) : RocketRewardDiffUtil {
    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem == this
    }

    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketRewardSecondType) {
            return false
        }
        return this.reward.coin == newItem.reward.coin && this.reward.name == newItem.reward.name &&
                this.reward.img == newItem.reward.img && this.reward.day == newItem.reward.day &&
                this.reward.type == newItem.reward.type
    }

}


class RocketGiftRewardFirst(val rewardList: List<RewardItem> = ArrayList()) : RocketRewardDiffUtil {
    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem is RocketGiftRewardFirst
    }

    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketGiftRewardFirst) {
            return false
        }
        return toJsonErrorNull(this.rewardList) == toJsonErrorNull(rewardList)
    }

}

class RocketTitleReward(val title: String) :
    RocketRewardDiffUtil {
    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem is RocketTitleReward
    }

    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketTitleReward) {
            return false
        }
        return this.title == newItem.title
    }

}

class RocketUserRewardFirst(val userList: List<UserContribution> = ArrayList()) :
    RocketRewardDiffUtil {
    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem is RocketUserRewardFirst
    }

    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketUserRewardFirst) {
            return false
        }
        return toJsonErrorNull(this.userList) == toJsonErrorNull(userList)
    }

}

class RocketUserRewardSecond(val userList: List<UserContribution> = ArrayList()) :
    RocketRewardDiffUtil {
    override fun areItemsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        return newItem is RocketUserRewardSecond
    }

    override fun areContentsTheSame(newItem: RocketRewardDiffUtil): Boolean {
        if (newItem !is RocketUserRewardSecond) {
            return false
        }
        return toJsonErrorNull(this.userList) == toJsonErrorNull(userList)
    }

}
