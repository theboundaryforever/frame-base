package com.yuehai.util.util.ext

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun DialogFragment.safeShow(manager: FragmentManager, tag: String) {
    manager.findFragmentByTag(tag)?.let {
        (it as? DialogFragment)?.dismissAllowingStateLoss()
        manager.beginTransaction().remove(it).commitAllowingStateLoss()
    }
    show(manager, tag)
}
