package com.example.cameratest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.example.cameratest.databinding.ItemFaceResultBinding
import com.example.common_base.BaseDiffUtilAdapter
import com.example.common_base.BaseViewHolder
import com.example.core_model.FaceReportData

class FaceResultAdapter(
    private val context: Context,
    override val requestManager: RequestManager
): BaseDiffUtilAdapter<ItemFaceResultBinding, FaceReportData>(requestManager) {

    override fun getBinding(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFaceResultBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    inner class ViewHolder(
        itemFaceResultBinding: ItemFaceResultBinding
    ) : BaseViewHolder<ItemFaceResultBinding>(itemFaceResultBinding) {
        override fun bind(position: Int) = with(binding) {
            val data = adapterList[position]

            txtTitle.text = data.title
            txtSubTitle.text = data.label
            txtDesc.text = data.content

        }
    }

    override fun areContentsTheSame(oldItem: FaceReportData, newItem: FaceReportData): Boolean =
        oldItem.content == newItem.content

    override fun areItemsTheSame(oldItem: FaceReportData, newItem: FaceReportData): Boolean =
        oldItem == newItem
}