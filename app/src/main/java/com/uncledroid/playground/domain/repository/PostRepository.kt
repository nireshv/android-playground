package com.uncledroid.playground.domain.repository

import com.uncledroid.playground.domain.model.Post
import io.ktor.client.statement.HttpResponse

interface PostRepository {
    suspend fun getPosts(userId: Int = -1): List<Post>
    suspend fun getPost(id: Int): Post?
    suspend fun createPost(post: Post): Post?
    suspend fun putPost(post: Post): Post?
    suspend fun patchPost(id: Int, map: Map<String, String>): Post?
    suspend fun deletePost(id: Int): HttpResponse
}