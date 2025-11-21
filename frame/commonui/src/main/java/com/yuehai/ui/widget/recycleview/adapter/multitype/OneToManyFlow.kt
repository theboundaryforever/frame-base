package com.yuehai.ui.widget.recycleview.adapter.multitype

import androidx.annotation.CheckResult
import com.adealink.frame.commonui.recycleview.adapter.multitype.OneToManyEndpoint

/**
 * Process and flow operators for one-to-many.
 *
 */
interface OneToManyFlow<T> {

  /**
   * Sets some item view delegates to the item type.
   *
   * @param delegates the item view delegates
   * @return end flow operator
   */
  @CheckResult
  fun to(vararg delegates: ItemViewDelegate<T, *>): OneToManyEndpoint<T>

  @CheckResult
  fun to(vararg delegates: ItemViewBinder<T, *>): OneToManyEndpoint<T>
}
