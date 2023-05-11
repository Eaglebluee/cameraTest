package com.example.core_data.usecase

import com.example.core_data.repository.ImageRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class ImageUploadUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(bodyData: MultipartBody.Part) = imageRepository.reqImageUpload(bodyData)
}