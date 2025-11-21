package com.yuehai.ui.widget.recycleview.adapter.withheader

/**
 * Created by suruijia on 2019/8/7
 *
 * modified by yucan on 2020/6/2
 * Comment: All item data must implement this interface
 */
interface BaseItemData {
    fun getItemType(position: Int): Int
}