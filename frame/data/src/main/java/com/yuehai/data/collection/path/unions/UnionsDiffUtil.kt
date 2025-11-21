package com.yuehai.data.collection.path.unions

import androidx.recyclerview.widget.DiffUtil


class UnionsListItemDiffUtil : DiffUtil.ItemCallback<UnionsListData>() {

    override fun areItemsTheSame(oldItem: UnionsListData, newItem: UnionsListData): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: UnionsListData, newItem: UnionsListData): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}

interface UnionsListData {
    fun areItemsTheSame(newItem: UnionsListData): Boolean
    fun areContentsTheSame(newItem: UnionsListData): Boolean
}
