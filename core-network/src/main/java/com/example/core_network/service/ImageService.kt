package com.example.core_network.service

import com.example.core_model.ImageUploadResponseData
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImageService {

    /** 이미지 업로드 */
    @Multipart
    @POST("/photo/image/upload")
    fun reqImageUpload(
        @Query("memNo") memNo: Int = 1,
        @Part imagefiles: MultipartBody.Part,
    ): Flow<ImageUploadResponseData>

}