package com.example.core_data.repository

import com.example.core_model.FaceReportResponseData
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody

interface ReportRepository {
    fun reqAnalyzeReport(raw: RequestBody): Flow<FaceReportResponseData>
}