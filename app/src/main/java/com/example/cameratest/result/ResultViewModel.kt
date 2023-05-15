package com.example.cameratest.result

import androidx.lifecycle.viewModelScope
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ReportUseCase
import com.example.core_model.FaceReportResponseData
import com.example.core_model.state.ResultUiState
import com.example.core_model.state.mutableResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val reportUseCase: ReportUseCase
) : BaseViewModel() {

    private val _reportState = mutableResultState<FaceReportResponseData>(ResultUiState.UnInitialize)
    val reportState = _reportState.asStateFlow()

    fun getAnalyzeReport(name: String) {
        viewModelScope.launch {
            val json = JSONObject().apply { put("img_path", name) }
            val raw = json.toString().toRequestBody("application/json".toMediaType())
            reportUseCase.invoke(raw).resultState(this) { _reportState.value = it }
        }
    }

}