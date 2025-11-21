package com.yuehai.ui.widget.recycleview.adapter.multitype

import com.adealink.frame.commonui.recycleview.adapter.multitype.Linker

internal class DefaultLinker<T> : Linker<T> {
  override fun index(position: Int, item: T): Int = 0
}
