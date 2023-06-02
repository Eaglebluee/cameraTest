package com.example.core_data.usecase

import com.example.core_data.repository.ReportRepository
import okhttp3.RequestBody
import javax.inject.Inject

class ReportFaceUseCase @Inject constructor(
    private val reportRepository: ReportRepository
){
    operator fun invoke(raw: RequestBody) = reportRepository.reqAnalyzeReport(raw)
}