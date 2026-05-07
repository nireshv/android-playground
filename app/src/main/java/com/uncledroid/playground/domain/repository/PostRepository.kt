package com.uncledroid.playground.domain.repository

import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post

interface PostRepository {
    suspend fun getPosts(userId: Int? = null): List<Post>
    suspend fun getPost(id: Int): Post?
    suspend fun createPost(post: Post): Post?
    suspend fun putPost(post: Post): Post?
    suspend fun patchPost(id: Int, patch: PatchPost): Post?
    suspend fun deletePost(id: Int): Boolean
}