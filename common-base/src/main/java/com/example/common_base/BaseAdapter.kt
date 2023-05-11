package com.example.common_base

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.common_util.ImageViewExtensionWrapper

abstract class BaseDiffUtilAdapter<Binding: ViewDataBinding, T>(
    override val requestManager: RequestManager
) : RecyclerView.Adapter<BaseViewHolder<Binding>>(), ImageViewExtensionWrapper {

    protected abstract fun getBinding(parent: ViewGroup, viewType: Int): BaseViewHolder<Binding>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = getBinding(parent, viewType)

    override fun onBindViewHolder(holder: BaseViewHolder<Binding>, position: Int) = holder.bind(position)

    override fun getItemCount() = asyncAdapterList.currentList.size

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    private val itemCallback by lazy {
        object : DiffUtil.ItemCallback<T>(){
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
                this@BaseDiffUtilAdapter.areItemsTheSame(oldItem, newItem)

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
                this@BaseDiffUtilAdapter.areContentsTheSame(oldItem, newItem)

        }
    }

    private val asyncAdapterList = AsyncListDiffer(this, itemCallback)

    protected val adapterList: List<T>
        get() = asyncAdapterList.currentList

    open fun submit(list: List<T>, complete: () -> Unit = {}) {
        asyncAdapterList.submitList(list, complete)
    }

}

abstract class BaseViewHolder<out Binding: ViewDataBinding>(open val binding: Binding) : RecyclerView.ViewHolder(binding.root) {
    open fun bind(position: Int) = Unit
}