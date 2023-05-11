package com.example.common_util

import android.widget.ImageView
import com.bumptech.glide.RequestManager

interface ImageViewExtensionWrapper {

    val requestManager: RequestManager

    fun ImageView.setImage(url: String?) {
        requestManager
            .load(url)
            .placeholder(R.drawable.icon_loading)
            .error(R.color.transparent)
            .into(this)
    }
}