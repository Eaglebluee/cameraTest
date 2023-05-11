package com.example.core_data.repository

import com.example.core_model.ImageUploadResponseData
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import org.json.JSONObject

interface ImageRepository {
    fun reqImageUpload(bodyData: MultipartBody.Part): Flow<ImageUploadResponseData>
}