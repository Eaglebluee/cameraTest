package com.example.core_network.datasource

import com.example.core_model.FaceReportResponseData
import com.example.core_network.service.ReportService
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import javax.inject.Inject

class ReportRemoteDataSourceImpl @Inject constructor(
    private val reportService: ReportService
): ReportRemoteDataSource {

    override fun reqAnalyzeReport(raw: RequestBody): Flow<FaceReportResponseData> = reportService.reqAnalyzeReport(raw)

}