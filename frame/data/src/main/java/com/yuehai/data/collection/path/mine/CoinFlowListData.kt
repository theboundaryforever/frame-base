package com.yuehai.data.collection.path.mine

interface CoinFlowListData {
    fun areItemsTheSame(newItem: CoinFlowListData): Boolean
    fun areContentsTheSame(newItem: CoinFlowListData): Boolean
}