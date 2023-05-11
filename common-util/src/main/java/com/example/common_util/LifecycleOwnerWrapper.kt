package com.example.common_util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.core_model.state.ResultUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface LifecycleOwnerWrapper {

    fun initLifeCycleOwner(): LifecycleOwner

    fun <T> Flow<T>.onResult(action: (T) -> Unit) {
        initLifeCycleOwner().lifecycleScope.launch {
            collect(action)
        }
    }

    fun <T> Flow<ResultUiState<T>>.onUiState(
        loading: () -> Unit = {},
        success: (T) -> Unit = {},
        error: (Throwable) -> Unit = {},
        finish: () -> Unit = {}
    ) {
        collect(initLifeCycleOwner().lifecycleScope) { state ->
            when (state) {
                ResultUiState.Loading -> loading()
                is ResultUiState.Success -> success(state.data)
                is ResultUiState.Error -> error(state.error)
                ResultUiState.Finish -> finish()
                else -> Unit
            }
        }
    }

}