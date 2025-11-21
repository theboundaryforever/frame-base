package com.adealik.frame.base.dialogqueue

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.adealik.frame.base.dialogFragment.BaseDialogFragment
import com.adealik.frame.base.dialogqueue.data.Priority
import com.adealik.frame.base.frame.BaseFrame
import com.adealik.frame.base.frame.IListener
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.PriorityQueue


class DialogQueue(val l: LifecycleOwner) : BaseFrame<IListener>(), DialogCallback,
    IDialogQueueManager {
    private var activeDialog: Any? = null
    private var curActiveDialogTag: String? = null
    private val pq = PriorityQueue<Task>(20, Comparator { o1, o2 ->
        return@Comparator o2.priority - o1.priority
    })

    init {
        l.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                launch {
                    tryPoll()
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                activeDialog = null
                curActiveDialogTag = null
            }
        })
    }

    private suspend fun tryPoll() {
        launch {
            if (activeDialog == null && l.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            ) {
                val task = pq.poll() ?: return@launch
                activeDialog = task.dialogBuilder
                curActiveDialogTag = task.tag

                when (val nextActiveDialog = activeDialog) {
                    is BaseDialogFragment -> {
                        withContext(Dispatcher.UI) {
                            nextActiveDialog.apply {
                                setDialogCallback(this@DialogQueue)
                                show(task.fragmentManager, task.tag)
                            }
                        }
                    }
                    else -> {    //未知类型展示下一个弹窗
                        pollDialog()
                    }
                }

            }
        }
    }

    override fun offer(
        tag: String,
        priority: Priority,
        fragmentManager: FragmentManager,
        dialogBuilder: Any?,
    ) {
        launch {
            val task = Task(tag, priority.priority, fragmentManager, dialogBuilder)
            if (pq.contains(task) || curActiveDialogTag == tag) {
                return@launch
            }
            pq.offer(task)
            tryPoll()
        }
    }

    private class Task(
        val tag: String,
        val priority: Int,
        val fragmentManager: FragmentManager,
        val dialogBuilder: Any?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Task
            return tag == other.tag
        }

        override fun hashCode(): Int {
            return tag.hashCode()
        }
    }

    override fun pollDialog() {
        launch {
            activeDialog = null
            curActiveDialogTag = null
            tryPoll()
        }
    }
}