package com.example.cameratest.facedetect

import androidx.lifecycle.viewModelScope
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ImageUploadUseCase
import com.example.core_data.usecase.ReportUseCase
import com.example.core_model.ImageUploadResponseData
import com.example.core_model.FaceReportResponseData
import com.example.core_model.state.ResultUiState
import com.example.core_model.state.mutableResultState
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
    private val imageUploadUseCase: ImageUploadUseCase
) : BaseViewModel() {


    private val _uploadImageState = mutableResultState<ImageUploadResponseData>(ResultUiState.UnInitialize)
    val uploadImageState = _uploadImageState.asStateFlow()

    fun uploadImage(body: MultipartBody.Part) {
        imageUploadUseCase.invoke(body).resultState(viewModelScope) {
            _uploadImageState.value = it
        }
    }

}