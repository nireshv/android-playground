package com.uncledroid.playground.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatchPostRequest(
    val body: String? = null,
    val title: String? = null,
    val userId: Int? = null
)