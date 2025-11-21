package com.yuehai.data.collection.path.user

import androidx.recyclerview.widget.DiffUtil

class MicSeatListItemDiffUtil : DiffUtil.ItemCallback<MicSeatListData>() {

    override fun areItemsTheSame(oldItem: MicSeatListData, newItem: MicSeatListData): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: MicSeatListData, newItem: MicSeatListData): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}

