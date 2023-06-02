package com.example.core_model

import com.google.gson.annotations.SerializedName

data class HandReportResponseData(
    val type : String = "",
    @SerializedName("feel_title") val feelTitle: String = "",
    @SerializedName("brain_title") val brainTitle: String = "",
    @SerializedName("life_title") val lifeTitle: String = "",
    @SerializedName("feel_content") val feelContent: String = "",
    @SerializedName("brain_content") val brainContent: String = "",
    @SerializedName("life_content") val lifeContent: String = "",
    @SerializedName("feel_coordinate") val feelCoordinate: List<List<Int>> = listOf(listOf()),
    @SerializedName("brain_coordinate") val brainCoordinate: List<List<Int>> = listOf(listOf()),
    @SerializedName("life_coordinate") val lifeCoordinate: List<List<Int>> = listOf(listOf())
)