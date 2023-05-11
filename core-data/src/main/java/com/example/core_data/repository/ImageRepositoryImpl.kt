package com.example.core_data.repository

import com.example.core_network.datasource.ImageRemoteDataSource
import okhttp3.MultipartBody
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageRemoteDataSource: ImageRemoteDataSource
): ImageRepository {

    override fun reqImageUpload(
        bodyData: MultipartBody.Part
    ) = imageRemoteDataSource.reqImageUpload(bodyData)

}