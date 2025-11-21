package com.yuehai.data.collection.path.unions

import android.R
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.login.CountryInfo
import com.yuehai.data.collection.path.user.UserInfo
import kotlinx.parcelize.Parcelize

data class GuildResult(
    @SerializedName("user_info") val userInfo: UserInfo? = null,
    @SerializedName("guild_info") val guideInfo: GuildInfo? = null
)

data class UnionDetailResult(
    @SerializedName("list") val userList: List<UnionUserInfo> = emptyList()
)

data class UnionMemberResult(
    @SerializedName("list") val userList: List<UserInfo> = emptyList(),
    @SerializedName("count") val count: Int = 0
)


data class UnionIndividualResult(
    @SerializedName("list") val userList: List<IndividualInfo> = emptyList()
)


data class GuildDetailResultInfo(
    @SerializedName("guild_info") val guildInfo: GuildInfo = GuildInfo(),
    @SerializedName("agency_data") val agencyData: AgencyInfo? = null,
    @SerializedName("individual_data") val individualData: AgencyInfo? = null,
    @SerializedName("sub_agent_data") val subAgentData: AgencyInfo? = null
)

@Parcelize
data class GuildInfo(
    @SerializedName("guild_id") val guildId: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("luck") val luck: Int = 0,
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("introduce") val introduce: String = "",
    @SerializedName("member_count") val memberCount: Int = 0,
    @SerializedName("sub_agent_count") val subAgentCount: Int = 0,
    @SerializedName("country_info") val countryInfo: CountryInfo = CountryInfo(),
    @SerializedName("last_week_rank") val lastWeekRank: String = "",
    @SerializedName("is_agent") val isAgent: Int = 0,
    @SerializedName("is_admin") val isAdmin: Int = 0,
    @SerializedName("share_url") val shareUrl: String = "",
    @SerializedName("is_manager") val isManager: Int = 0,
    @SerializedName("is_bd") val isBD: Int = 0,

    ) : Parcelable {

    fun isAgencyAgent(): Boolean {
        return isAgent == 1
    }

    fun isAgentAdmin(): Boolean {
        return isAdmin == 1
    }

    fun isAgencyManager(): Boolean {
        return isManager == 1
    }

    fun isBdManager(): Boolean {
        return isBD == 1
    }

}

@Parcelize
data class AgencyInfo(
    @SerializedName("first_half_month") val firstHalfMonth: PeriodDataWithRate = PeriodDataWithRate(),
    @SerializedName("second_half_month") val secondHalfMonth: PeriodDataWithRate = PeriodDataWithRate(),
    @SerializedName("today") val today: TotalHostInfo? = null,
    @SerializedName("yesterday") val yesterday: TotalHostInfo? = null,
    @SerializedName("this_week") val thisWeek: TotalHostInfo? = null,
    @SerializedName("last_week") val lastWeek: TotalHostInfo? = null,
    @SerializedName("this_month") val thisMonth: TotalHostInfo? = null,
    @SerializedName("last_month") val lastMonth: TotalHostInfo? = null,
    @SerializedName("type") val type: Int = 0
) : Parcelable

@Parcelize
data class PeriodDataWithRate(
    @SerializedName("start_time") val startTime: Long = 0L,
    @SerializedName("end_time") val endTime: Long = 0L,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("rate") val rate: String = "",
    @SerializedName("type") val type: Int = 0,
    @SerializedName("active_days") val activeDays: Int = 0
) : Parcelable

@Parcelize
data class TotalHostInfo(
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("active_host") val activeHost: Int = 0,
    @SerializedName("mic_duration") val micDuration: Long = 0,
    @SerializedName("active_days") val activeDays: Int = 0,
    @SerializedName("type") val type: Int = 0
) : Parcelable


data class UnionUserInfo(
    @SerializedName("total")//	钻石数
    val diamondTotal: Double = 0.0,
    @SerializedName("active_days")//活跃天数
    val activeDays: Long = 0,
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nickname") val nickname: String = "",
    @SerializedName("guild_id") val guildId: Int = 0,
    @SerializedName("create_time") val createTime: Long = 0,
    @SerializedName("name") val agentName: String = ""

)


enum class UnionsInfoType(val type: Int) {
    MY_AGENCY(1), ADMIN_CENTER(2), BD_CENTER(3), CREATE_GUIDE(4), MANAGER_CENTER(5), CUSTOM_SERVICE(
        6
    )

}

enum class AgencyType(val type: Int) {
    AGENCY(1), INDIVIDUAL(2), SUB_AGENT(3),

}


data class UnionsHeaderInfo(
    var headerInfo: ArrayList<UnionsInfoType> = ArrayList(),
    val guild: Int,
    val isAdmin: Boolean,
    val isAgent: Boolean,
    val isGoldAgent: Boolean,
    val isManager: Boolean,
    val isBd: Boolean,
    val adminCenterUrl: String? = null,
    val agencyCreateUrl: String? = null,
    val managerCenterUrl: String? = null,
    val bdCenterUrl: String? = null,
) : UnionsListData {
    override fun areItemsTheSame(newItem: UnionsListData): Boolean {
        return newItem is UnionsHeaderInfo
    }

    override fun areContentsTheSame(newItem: UnionsListData): Boolean {
        if (newItem !is UnionsHeaderInfo) {
            return false
        }
        return this.headerInfo == newItem.headerInfo && this.guild == newItem.guild && this.isAdmin == newItem.isAdmin && this.isAgent == newItem.isAgent && this.isGoldAgent == newItem.isGoldAgent && this.managerCenterUrl == newItem.managerCenterUrl && this.bdCenterUrl == newItem.bdCenterUrl
    }


}


data class UnionsRankTitleInfo(val headerInfo: Boolean = false) : UnionsListData {
    override fun areItemsTheSame(newItem: UnionsListData): Boolean {
        return newItem is UnionsHeaderInfo
    }

    override fun areContentsTheSame(newItem: UnionsListData): Boolean {
        if (newItem !is UnionsRankTitleInfo) {
            return false
        }
        return this.headerInfo == newItem.headerInfo
    }

}

data class UnionResultInfo(
    @SerializedName("info") val info: UnionInfo? = null,
    @SerializedName("list") val list: List<UnionRankItem> = ArrayList()
)

data class UnionInfo(
    @SerializedName("is_agent") val isAgent: Int = 0,
    @SerializedName("guild_id") val guildId: Int = 0,
    @SerializedName("is_admin") val isAdmin: Int = 0,
    @SerializedName("is_bd") val isBD: Int = 0,
    @SerializedName("is_manager") val isManager: Int = 0,
    @SerializedName("is_gold_agents") val isGoldAgents: Int = 0,
    @SerializedName("admin_center_url") val adminCenterUrl: String? = null,
    @SerializedName("create_agency_url") val createAgencyUrl: String? = null,
    @SerializedName("share_url") val shareUrl: String? = null,
    @SerializedName("manager_center_url") val managerCenterUrl: String? = null,
    @SerializedName("bd_center_url") val bdCenterUrl: String? = null,
) : UnionsListData {
    override fun areItemsTheSame(newItem: UnionsListData): Boolean {
        return newItem is UnionInfo
    }

    override fun areContentsTheSame(newItem: UnionsListData): Boolean {
        if (newItem !is UnionInfo) {
            return false
        }
        return this.isAgent == newItem.isAgent && this.guildId == newItem.guildId && this.adminCenterUrl == newItem.adminCenterUrl && this.bdCenterUrl == newItem.bdCenterUrl
    }

    fun isAdmin(): Boolean {
        return isAdmin == 1
    }

    fun isAgencyAgent(): Boolean {
        return isAgent == 1
    }

    fun isGoldAgentsState(): Boolean {
        return isGoldAgents == 1
    }

    fun isAgencyManager(): Boolean {
        return isManager == 1
    }

    fun isBD(): Boolean {
        return isBD == 1
    }
}

data class UnionRankItem(
    @SerializedName("guild_id") val guildId: Int = 0,
    @SerializedName("user_id") val userId: Long = 0L,
    @SerializedName("name") val name: String = "",
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("member_count") val memberCount: Int = 0,
    @SerializedName("rank") val rank: String = "0"
) : UnionsListData {
    override fun areItemsTheSame(newItem: UnionsListData): Boolean {
        return newItem is UnionRankItem
    }

    override fun areContentsTheSame(newItem: UnionsListData): Boolean {
        if (newItem !is UnionRankItem) {
            return false
        }
        return this.guildId == newItem.guildId && this.userId == newItem.userId && this.name == newItem.name && this.avatar == newItem.avatar && this.memberCount == newItem.memberCount && this.rank == newItem.rank
    }

}


@Parcelize
data class IndividualInfo(
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("date_time") val dateTime: Long = 0,
    @SerializedName("mic_duration") val micDuration: Long = 0,

    ) : Parcelable

enum class InviteState(val state: Int) {
    INVITE(1), INVITE_AGAIN(2), JOINED(3), INVITED(4)
}


