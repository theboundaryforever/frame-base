package com.adealik.frame.base.ext


fun <T> fastLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)