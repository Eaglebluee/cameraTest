package com.example.common_base

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.common_util.LifecycleOwnerWrapper

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity(),
    LifecycleOwnerWrapper {

    protected val binding: Binding by lazy { createBinding() }

    protected abstract fun createBinding(): Binding

    protected open fun initActivity() = Unit

    override fun initLifeCycleOwner(): LifecycleOwner = this

    protected open fun onActivityBackPressed() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        initActivity()
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    protected var enableBackPressed: Boolean = true

    private val callback = object : OnBackPressedCallback(enableBackPressed) {
        override fun handleOnBackPressed() {
            onActivityBackPressed()
        }
    }

    fun addFragment(
        @IdRes containerId: Int,
        fragment: Fragment?,
        addBackStack: Boolean = false
    ) {
        requireNotNull(fragment)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(containerId, fragment, fragment::class.java.simpleName).apply {
            if (addBackStack) addToBackStack(null)
            commitAllowingStateLoss()
        }
    }

    fun showFragment(fragment: Fragment?) {
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().hide(it).commitAllowingStateLoss()
        }
        val findFragment = supportFragmentManager.findFragmentByTag(fragment?.tag)
        findFragment?.let {
            supportFragmentManager.beginTransaction().show(it).commitAllowingStateLoss()
        }
    }
}