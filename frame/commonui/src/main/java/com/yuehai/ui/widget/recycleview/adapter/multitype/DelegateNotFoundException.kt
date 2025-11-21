package com.yuehai.ui.widget.recycleview.adapter.multitype

internal class DelegateNotFoundException(clazz: Class<*>) : RuntimeException(
  "Have you registered the ${clazz.name} type and its delegate or binder?"
)
