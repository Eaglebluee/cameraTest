package com.example.cameratest.result

import androidx.lifecycle.viewModelScope
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ReportFaceUseCase
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
class FaceResultViewModel @Inject constructor(
    private val reportFaceUseCase: ReportFaceUseCase
) : BaseViewModel() {

    private val _reportState = mutableResultState<FaceReportResponseData>(ResultUiState.UnInitialize)
    val reportState = _reportState.asStateFlow()

    fun getAnalyzeReport(name: String) {
        viewModelScope.launch {
            val json = JSONObject().apply { put("img_path", name) }
            val raw = json.toString().toRequestBody("application/json".toMediaType())
            reportFaceUseCase.invoke(raw).resultState(this) { _reportState.value = it }
        }
    }

    private var _delegate: Int = FaceLandmarkerHelper.DELEGATE_CPU
    private var _minFaceDetectionConfidence: Float =
        FaceLandmarkerHelper.DEFAULT_FACE_DETECTION_CONFIDENCE
    private var _minFaceTrackingConfidence: Float = FaceLandmarkerHelper
        .DEFAULT_FACE_TRACKING_CONFIDENCE
    private var _minFacePresenceConfidence: Float = FaceLandmarkerHelper
        .DEFAULT_FACE_PRESENCE_CONFIDENCE
    private var _maxFaces: Int = FaceLandmarkerHelper.DEFAULT_NUM_FACES

    val currentDelegate: Int get() = _delegate
    val currentMinFaceDetectionConfidence: Float
        get() =
            _minFaceDetectionConfidence
    val currentMinFaceTrackingConfidence: Float
        get() =
            _minFaceTrackingConfidence
    val currentMinFacePresenceConfidence: Float
        get() =
            _minFacePresenceConfidence
    val currentMaxFaces: Int get() = _maxFaces

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

}