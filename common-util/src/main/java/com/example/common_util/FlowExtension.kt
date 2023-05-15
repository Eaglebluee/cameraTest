package com.example.common_util

import com.example.core_model.state.ResultUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

inline fun <T> Flow<T>.collect(
    externalScope: CoroutineScope,
    crossinline collect: (T) -> Unit
) = onEach { collect.invoke(it) }.launchIn(externalScope)

fun <T> Flow<T>.resultState(
    scope: CoroutineScope,
    delay: Long? = null,
    collect: (ResultUiState<T>) -> Unit
) {
    onLoading(delay) { collect(ResultUiState.Loading) }
        .mapLatest { collect(ResultUiState.Success(it)) }
        .catch { collect(ResultUiState.Error(it)) }
        .onEach { collect(ResultUiState.Finish) }
        .launchIn(scope)
}

fun <T> Flow<T>.onLoading(
    delay: Long? = null,
    action: suspend () -> Unit
): Flow<T> = onStart {
    action.invoke()
    if(delay != null) delay(delay)
}
