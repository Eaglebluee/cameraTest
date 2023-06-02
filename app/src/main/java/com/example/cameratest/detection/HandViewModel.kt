package com.example.cameratest.detection

import androidx.lifecycle.viewModelScope
import com.example.cameratest.landmarkerhelper.HandLandmarkerHelper
import com.example.common_base.BaseViewModel
import com.example.common_util.resultState
import com.example.core_data.usecase.ImageUploadUseCase
import com.example.core_model.ImageUploadResponseData
import com.example.core_model.state.ResultUiState
import com.example.core_model.state.mutableResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class HandViewModel @Inject constructor(
    private val imageUploadUseCase: ImageUploadUseCase
) : BaseViewModel() {

    private val _uploadImageState = mutableResultState<ImageUploadResponseData>(ResultUiState.UnInitialize)
    val uploadImageState = _uploadImageState.asStateFlow()

    fun uploadImage(body: MultipartBody.Part) {
        imageUploadUseCase.invoke(body).resultState(viewModelScope) {
            _uploadImageState.value = it
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

    fun setMinHandDetectionConfidence(confidence: Float) {
        _minHandDetectionConfidence = confidence
    }
    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
    }
    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
    }

    fun setMaxHands(maxResults: Int) {
        _maxHands = maxResults
    }

}