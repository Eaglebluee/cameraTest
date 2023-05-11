package com.example.core_model

data class ReportResponseData(
    val results: List<ReportData> = listOf()
)

data class ReportData(
    val title: String = "",
    val label: String = "",
    val content: String = ""
)