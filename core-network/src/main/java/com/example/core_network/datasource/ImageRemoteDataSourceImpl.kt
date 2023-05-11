package com.example.core_network.datasource

import com.example.core_model.ImageUploadResponseData
import com.example.core_network.service.ImageService
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import org.json.JSONObject
import javax.inject.Inject

class ImageRemoteDataSourceImpl @Inject constructor(
    private val imageService: ImageService
): ImageRemoteDataSource {

    override fun reqImageUpload(
        bodyData: MultipartBody.Part
    ): Flow<ImageUploadResponseData> = imageService.reqImageUpload(1, bodyData)

}