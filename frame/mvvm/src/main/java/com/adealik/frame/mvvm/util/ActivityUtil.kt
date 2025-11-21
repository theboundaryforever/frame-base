package com.adealik.frame.mvvm.util

import android.app.Activity
import android.content.Context


fun isActivityUnAttach(context: Context?): Boolean {
    return context is Activity && context.application == null
}

fun isActivityDestroy(context: Context?): Boolean {
    return context is Activity && context.isDestroyed
}

fun isActivityFinishing(context: Context?): Boolean {
    return context is Activity && context.isFinishing
}

fun isActivityInValid(context: Context?): Boolean {
    return isActivityUnAttach(context) || isActivityFinishing(context) || isActivityDestroy(context)
}