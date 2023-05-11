package com.example.core_network.datasource

import com.example.core_model.ReportResponseData
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody

interface ReportRemoteDataSource {
    fun reqAnalyzeReport(raw: RequestBody): Flow<ReportResponseData>
}