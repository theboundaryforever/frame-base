package com.yuehai.data.collection.path.mine

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

//金币流水
data class CoinFlowResult(
    @SerializedName("list") val coinFlowList: List<CoinDetailInfo> = ArrayList<CoinDetailInfo>()
)

data class CoinDetailInfo(
    @SerializedName("log_msg") val logMsg: String = "",
    @SerializedName("price") val price: BigDecimal = BigDecimal(0),
    @SerializedName("time") val time: Long = 0,
    @SerializedName("type") val type: Int = 0,

    @SerializedName("log_type_name") val logTypeName: String = "",

    @SerializedName("log_type_icon") val logTypeIcon: String = "",

    ) : CoinFlowListData {
    override fun areItemsTheSame(newItem: CoinFlowListData): Boolean {
        if (newItem !is CoinDetailInfo) {
            return false
        }
        return this == newItem
    }

    override fun areContentsTheSame(newItem: CoinFlowListData): Boolean {
        if (newItem !is CoinDetailInfo) return false
        return this.logMsg == newItem.logMsg &&
                this.price == newItem.price &&
                this.time == newItem.time &&
                this.type == newItem.type &&
                this.logTypeName == newItem.logTypeName &&
                this.logTypeIcon == newItem.logTypeIcon
    }
}

data class CoinFlowEmpty(val type: Int = 0) : CoinFlowListData {
    override fun areItemsTheSame(newItem: CoinFlowListData): Boolean {
        return newItem is CoinFlowEmpty
    }

    override fun areContentsTheSame(newItem: CoinFlowListData): Boolean {
        if (newItem !is CoinFlowEmpty) {
            return false
        }
        return this.type == newItem.type
    }

}


//流水筛选条件
data class BillConditionInfo(
    @SerializedName("income_list")
    val incomeList: List<ConditionInfo> = ArrayList(),
    @SerializedName("expenditure_list")
    val expenditureList: List<ConditionInfo> = ArrayList()
)

data class ConditionInfo(
    @SerializedName("id")
    val conditionId: Int = 0,
    @SerializedName("name")
    val conditionName: String = "",

    ) {
    var conditionSelect: Boolean = false
}