package com.yuehai.coroutine.thread

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


class NamedThreadFactory(private val baseName: String, private val priority: Int) : ThreadFactory {

    private val defaultThreadFactory: ThreadFactory = Executors.defaultThreadFactory()
    private val count = AtomicInteger(0)

    override fun newThread(r: Runnable?): Thread {
        val thread: Thread = defaultThreadFactory.newThread(r)
        thread.name = "${baseName}-${count.getAndIncrement()}"
        thread.priority = priority
        return thread
    }

}