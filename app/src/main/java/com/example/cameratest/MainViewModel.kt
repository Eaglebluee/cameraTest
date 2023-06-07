package com.example.cameratest

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
import com.example.common_base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : BaseViewModel() {

    lateinit var photoUri : Uri
    lateinit var imgName : String
    var screenState: ScreenState = ScreenState.Detect
    val handResultDesc = mutableListOf<Pair<String, String>>()
    val handCoordList = mutableListOf(listOf(listOf<Int>()))
    var isLeft = true


    private val _toMainScreen = MutableSharedFlow<Unit>()
    val toMainScreen = _toMainScreen.asSharedFlow()

    fun changeToMain() {
        viewModelScope.launch {
            _toMainScreen.emit(Unit)
        }
    }


    sealed interface ScreenState {
        object Detect : ScreenState
        object Analyze : ScreenState
        object Result : ScreenState
    }
}