package com.example.core_model

data class ImageUploadResponseData(
    val files: List<ImageFileData> = listOf(),
    val retMsg: String = ""
)

data class ImageFileData(
    val mImgUrl: String = "",
    val imginfo: String = ""
)