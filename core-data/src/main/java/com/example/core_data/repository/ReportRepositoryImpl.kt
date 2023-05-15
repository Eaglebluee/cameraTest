package com.example.core_data.repository

import com.example.core_model.FaceReportResponseData
import com.example.core_network.datasource.ReportRemoteDataSource
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDataSource: ReportRemoteDataSource
): ReportRepository {

    override fun reqAnalyzeReport(raw: RequestBody): Flow<FaceReportResponseData>  = reportRemoteDataSource.reqAnalyzeReport(raw)

}