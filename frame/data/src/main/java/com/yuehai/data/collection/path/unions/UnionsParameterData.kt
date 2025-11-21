package com.yuehai.data.collection.path.unions

import com.google.gson.annotations.SerializedName

// ---- 创建工会 ----
data class CreateUnionsRequest(
    @SerializedName("type")
    val type: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("agency_name")
    val agencyName: String,
    @SerializedName("real_name")
    val realName: String,
    @SerializedName("agency_country")
    val agencyCountry: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("other_platforms")
    val otherPlatforms: String,
)

// ---- 保存工会 ----
data class SaveUnionsRequest(
    @SerializedName("guild_id")
    val guildId: Int,
    @SerializedName("agency_name")
    val agencyName: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("announcement")
    val announcement: String,
)

// ---- 获取工会信息 ----
data class UnionsInfoRequest(
    @SerializedName("guild_id")
    val guildId: Int,
)

// ---- 申请加入工会 ----
data class ApplyToJoinUnionsRequest(
    @SerializedName("guild_id")
    val guildId: Int,
)

// ---- 工会成员列表 ----
data class UnionsMemberListRequest(
    @SerializedName("guild_id")
    val guildId: Int,
    @SerializedName("page")
    val page: Int,
)

// ---- 邀请用户加入工会 ----
data class InviteToUnionsRequest(
    @SerializedName("invite_uid")
    val inviteUid: Int,
)

// ---- 处理邀请 ----
data class HandleUnionsInviteRequest(
    @SerializedName("invite_uid")
    val inviteUid: Int,
    @SerializedName("type")
    val type: Int,
)

// ---- 移除工会用户 ----
data class RemoveUnionsUserRequest(
    @SerializedName("guild_id")
    val guildId: Int,
    @SerializedName("to_uid")
    val toUid: Int,
)

// ---- 工会详情分页请求 ----
data class UnionsDetailRequest(
    @SerializedName("guild_id")
    val guildId: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("page")
    val page: Int,
)

// ---- 工会主播详情请求 ----
data class UnionsAnchorDetailRequest(
    @SerializedName("guild_id")
    val guildId: Int,
    @SerializedName("type")
    val type: Int,
)



data class HandleInviteRequest(
    @SerializedName("record_id")
    val recordId: Int,
    @SerializedName("type")
    val type: Int,
)

