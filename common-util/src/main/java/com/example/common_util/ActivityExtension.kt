package com.example.common_util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.replaceFragment(
    @IdRes containerId: Int,
    fragment: Fragment?
) {
    if (fragment == null) return
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(containerId, fragment)
    transaction.commitAllowingStateLoss()
}

fun FragmentActivity.removeFragment(fragment: Fragment?) {
    if (fragment == null) return
    val transaction = supportFragmentManager.beginTransaction()
    transaction.remove(fragment)
    transaction.commitAllowingStateLoss()
}