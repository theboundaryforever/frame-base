package com.yuehai.data.collection.path.user

interface ChatMessageListData {
    fun areItemsTheSame(newItem: ChatMessageListData): Boolean
    fun areContentsTheSame(newItem: ChatMessageListData): Boolean
}

