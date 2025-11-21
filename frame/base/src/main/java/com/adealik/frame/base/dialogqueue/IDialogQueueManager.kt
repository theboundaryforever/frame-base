package com.adealik.frame.base.dialogqueue

import androidx.fragment.app.FragmentManager
import com.adealik.frame.base.dialogqueue.data.Priority


interface IDialogQueueManager {
    fun offer(tag: String, priority: Priority, fragmentManager : FragmentManager, dialogBuilder:Any?)
}