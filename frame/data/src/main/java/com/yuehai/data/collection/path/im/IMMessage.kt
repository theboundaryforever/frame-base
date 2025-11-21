package com.yuehai.data.collection.path.im

data class FocusIMMessage(
    val businessID: String,
    val focusUser: Boolean,
    val excludeFromHistory: Boolean,
    val hasReaction: Boolean,
    val hasRiskContent: Boolean,
    val id: String,
    val isEnableForward: Boolean,
    val isGroup: Boolean,
    val isProcessing: Boolean,
    val isSending: Boolean,
    val isUseMsgReceiverAvatar: Boolean,
    val messageSource: Int,
    val msgTime: Long,
    val status: Int,
    val userBeanMap: Map<String, Any> // 如果结构已知也可以定义成具体的实体类
)
