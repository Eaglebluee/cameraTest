package com.example.cameratest.fragment

import androidx.lifecycle.viewModelScope
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ImageUploadUseCase
import com.example.core_data.usecase.ReportUseCase
import com.example.core_model.ImageUploadResponseData
import com.example.core_model.ReportResponseData
import com.example.core_model.state.ResultUiState
import com.example.core_model.state.mutableResultState
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class FaceViewModel @Inject constructor(
    private val imageUploadUseCase: ImageUploadUseCase,
    private val reportUseCase: ReportUseCase
) : BaseViewModel() {


    private val _uploadImageState = mutableResultState<ImageUploadResponseData>(ResultUiState.UnInitialize)
    val uploadImageState = _uploadImageState.asStateFlow()

    private val _reportState = mutableResultState<ReportResponseData>(ResultUiState.UnInitialize)
    val reportState = _reportState.asStateFlow()

    fun uploadImage(body: MultipartBody.Part) {
        imageUploadUseCase.invoke(body).resultState(viewModelScope) {
            _uploadImageState.value = it
        }
    }

    fun getAnalyzeReport(name: String) {
        viewModelScope.launch {
            val json = JSONObject().apply { put("img_path", name) }
            val raw = json.toString().toRequestBody("application/json".toMediaType())
            reportUseCase.invoke(raw).resultState(this) { _reportState.value = it }
        }
    }

}