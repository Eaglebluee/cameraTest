package com.example.common_base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.common_util.ImageViewExtensionWrapper
import com.example.common_util.LifecycleOwnerWrapper

abstract class BaseFragment<Binding: ViewDataBinding> : Fragment(), ImageViewExtensionWrapper,
    LifecycleOwnerWrapper {

    private var _binding : Binding? = null
    protected val binding get() = _binding!!

    protected abstract fun createFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : Binding

    protected open fun initFragment() = Unit

    override fun initLifeCycleOwner(): LifecycleOwner = viewLifecycleOwner

    override val requestManager: RequestManager
        get() = Glide.with(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createFragmentBinding(inflater, container).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragment()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}