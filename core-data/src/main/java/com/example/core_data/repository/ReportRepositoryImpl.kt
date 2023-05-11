package com.example.core_data.repository

import com.example.core_model.ReportResponseData
import com.example.core_network.datasource.ReportRemoteDataSource
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportRemoteDataSource: ReportRemoteDataSource
): ReportRepository {

    override fun reqAnalyzeReport(raw: RequestBody): Flow<ReportResponseData>  = reportRemoteDataSource.reqAnalyzeReport(raw)

}