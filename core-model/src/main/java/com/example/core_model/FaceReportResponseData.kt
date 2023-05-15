package com.example.core_model

data class FaceReportResponseData(
    val results: List<FaceReportData> = listOf()
)

data class FaceReportData(
    val title: String = "",
    val label: String = "",
    val content: String = ""
)