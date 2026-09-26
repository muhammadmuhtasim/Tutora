package com.example.tutora.util

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A safe version of [viewModelScope.launch] that automatically attaches a [CoroutineExceptionHandler].
 * Use this for background tasks that might throw unexpected exceptions to prevent app crashes.
 */
fun ViewModel.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("ViewModel", "Uncaught exception in safeLaunch", throwable)
        onError?.invoke(throwable)
    }

    return viewModelScope.launch(context + exceptionHandler, block = block)
}
