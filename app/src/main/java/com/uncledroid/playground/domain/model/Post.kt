package com.uncledroid.playground.domain.model

data class Post(
    val id: Int = 0,
    val body: String,
    val title: String,
    val userId: Int
)