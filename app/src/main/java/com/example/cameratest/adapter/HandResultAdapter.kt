package com.example.cameratest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.example.cameratest.databinding.ItemHandResultBinding
import com.example.common_base.BaseDiffUtilAdapter
import com.example.common_base.BaseViewHolder

class HandResultAdapter(
    private val context: Context,
    override val requestManager: RequestManager
): BaseDiffUtilAdapter<ItemHandResultBinding, Pair<String, String>>(requestManager) {

    override fun getBinding(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHandResultBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    inner class ViewHolder(
        itemHandResultBinding: ItemHandResultBinding
    ): BaseViewHolder<ItemHandResultBinding>(itemHandResultBinding) {
        override fun bind(position: Int) = with(binding) {
            val data = adapterList[position]

            txtTitle.text = data.first
            txtDesc.text = data.second

        }
    }

    override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>) =
        oldItem == newItem

    override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>) =
        oldItem == newItem

}