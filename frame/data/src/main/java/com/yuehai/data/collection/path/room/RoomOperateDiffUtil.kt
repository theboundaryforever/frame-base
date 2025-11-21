package com.yuehai.data.collection.path.room

import androidx.recyclerview.widget.DiffUtil
import com.yuehai.data.collection.path.user.MicSeatListData

interface RoomOperateDiffUtil {
    fun areItemsTheSame(newItem: RoomOperateDiffUtil): Boolean
    fun areContentsTheSame(newItem: RoomOperateDiffUtil): Boolean
}

class RoomOperateItemDiffUtil : DiffUtil.ItemCallback<RoomOperateDiffUtil>() {

    override fun areItemsTheSame(oldItem: RoomOperateDiffUtil, newItem: RoomOperateDiffUtil): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: RoomOperateDiffUtil, newItem: RoomOperateDiffUtil): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}



