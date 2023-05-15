package com.example.core_network.service

import com.example.core_model.FaceReportResponseData
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportService {

    /** Receive analyze report */
    @POST("/predict")
    fun reqAnalyzeReport(@Body raw: RequestBody): Flow<FaceReportResponseData>

}