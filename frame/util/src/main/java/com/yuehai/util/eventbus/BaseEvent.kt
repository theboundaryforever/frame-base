package com.yuehai.util.eventbus

class BaseEvent<T>(// 事件类型（用于区分不同事件）
    val eventType: String, // 泛型数据
    val data: T
)
