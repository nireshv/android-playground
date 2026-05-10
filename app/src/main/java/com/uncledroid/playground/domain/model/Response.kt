package com.uncledroid.playground.domain.model

sealed interface Response<out T> {
    data object Loading : Response<Nothing>
    data class Success<T>(val data: T) : Response<T>
    data class Error(val message: String) : Response<Nothing>
}