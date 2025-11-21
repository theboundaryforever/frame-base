package com.yuehai.media.constant

import com.yuehai.data.collection.path.IError


class MediaChannelNameNull :IError ()
class MediaTokenNull : IError()
class JoinChannelError(override val msg: String) : IError()