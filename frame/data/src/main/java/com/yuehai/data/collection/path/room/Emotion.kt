package com.yuehai.data.collection.path.room

import com.google.gson.annotations.SerializedName

class EmotionInfoList(
    @SerializedName("list")
    val emotionList: List<EmotionInfo> = ArrayList()
)

class EmotionInfo(
    @SerializedName("id")
    val emotionId: Int,
    @SerializedName("img")
    val emotionImg: String,
    @SerializedName("name")
    val emotionName: String,
)

class IMEmotionInfo(
    @SerializedName("voice_uid")
    val voiceUid: Int,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("id")
    val emotionId: String,
    @SerializedName("img")
    val emotionImg: String,
)