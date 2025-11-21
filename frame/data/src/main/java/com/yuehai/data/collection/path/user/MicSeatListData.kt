package com.yuehai.data.collection.path.user

interface MicSeatListData {
    fun areItemsTheSame(newItem: MicSeatListData): Boolean
    fun areContentsTheSame(newItem: MicSeatListData): Boolean
}

