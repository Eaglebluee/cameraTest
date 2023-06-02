package com.example.cameratest.detection

import androidx.lifecycle.viewModelScope
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
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

    fun setMinFaceDetectionConfidence(confidence: Float) {
        _minFaceDetectionConfidence = confidence
    }
    fun setMinFaceTrackingConfidence(confidence: Float) {
        _minFaceTrackingConfidence = confidence
    }
    fun setMinFacePresenceConfidence(confidence: Float) {
        _minFacePresenceConfidence = confidence
    }

    fun setMaxFaces(maxResults: Int) {
        _maxFaces = maxResults
    }

}