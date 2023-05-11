package com.example.core_model.state

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> mutableResultState(
    state: ResultUiState<T> = ResultUiState.Loading
): MutableStateFlow<ResultUiState<T>> = MutableStateFlow(state)

sealed class ResultUiState<out T> {

    object UnInitialize : ResultUiState<Nothing>()

    object Loading : ResultUiState<Nothing>()

    data class Success<T>(val data: T) : ResultUiState<T>()

    data class Error(val error: Throwable) : ResultUiState<Nothing>()

    object Finish : ResultUiState<Nothing>()

}