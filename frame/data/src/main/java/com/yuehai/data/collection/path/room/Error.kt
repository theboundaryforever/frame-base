package com.yuehai.data.collection.path.room

import com.yuehai.data.collection.path.IError

/**
 * 正在进入相同房间
 */
class RoomSameJoiningError : IError()

/**
 * 加入房间已改变错误
 */
class RoomJoinedChangedError : IError()

/**
 * 正在进房中
 */
class RoomJoiningError : IError()

/**
 * 重进房太频繁
 */
class RoomRejoiningTooOftenError : IError()

/**
 * 不在mic
 */
class RoomNotOnMicError : IError()

/**
 * 尚未进房错误
 */
class RoomNoJoinedError : IError()