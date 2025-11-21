package com.yuehai.ui.widget.recycleview.adapter.multitype

import com.adealink.frame.commonui.recycleview.adapter.multitype.Linker

data class Type<T>(
    val clazz: Class<out T>,
    val delegate: ItemViewDelegate<T, *>,
    val linker: Linker<T>
)
