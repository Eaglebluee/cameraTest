package com.example.common_util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.removeFragment(fragment: Fragment?) {
    if (fragment == null) return

    val transaction = supportFragmentManager.beginTransaction()
    transaction.remove(fragment)
    transaction.commitAllowingStateLoss()
}