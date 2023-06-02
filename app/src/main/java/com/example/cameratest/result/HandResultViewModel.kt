package com.example.cameratest.result

import androidx.lifecycle.viewModelScope
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
import com.example.cameratest.landmarkerhelper.HandLandmarkerHelper
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ReportHandUseCase
import com.example.core_model.FaceReportResponseData
import com.example.core_model.HandReportResponseData
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
class HandResultViewModel @Inject constructor(
    private val reportHandUseCase: ReportHandUseCase
) : BaseViewModel() {

    private val _reportState = mutableResultState<HandReportResponseData>(ResultUiState.UnInitialize)
    val reportState = _reportState.asStateFlow()

    fun getAnalyzeReport(name: String) {
        viewModelScope.launch {
            val json = JSONObject().apply { put("img_path", name) }
            val raw = json.toString().toRequestBody("application/json".toMediaType())
            reportHandUseCase.invoke(raw).resultState(this) { _reportState.value = it }
        }
    }

    private var _delegate: Int = HandLandmarkerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float =
        HandLandmarkerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = HandLandmarkerHelper
        .DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = HandLandmarkerHelper
        .DEFAULT_HAND_PRESENCE_CONFIDENCE
    private var _maxHands: Int = HandLandmarkerHelper.DEFAULT_NUM_HANDS

    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float
        get() =
            _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float
        get() =
            _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float
        get() =
            _minHandPresenceConfidence
    val currentMaxHands: Int get() = _maxHands

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

}