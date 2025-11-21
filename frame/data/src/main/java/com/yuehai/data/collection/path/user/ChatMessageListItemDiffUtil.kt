package com.yuehai.data.collection.path.user

import androidx.recyclerview.widget.DiffUtil

class ChatMessageListItemDiffUtil : DiffUtil.ItemCallback<ChatMessageListData>() {

    override fun areItemsTheSame(oldItem: ChatMessageListData, newItem: ChatMessageListData): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: ChatMessageListData, newItem: ChatMessageListData): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}

