package com.uncledroid.playground.domain.repository

import com.uncledroid.playground.common.Response
import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post
import kotlinx.coroutines.flow.Flow


interface PostFlowRepository {
    val allPosts: Flow<Response<List<Post>>>
    fun getPost(id: Int): Flow<Response<Post>>
    fun getPostsForUser(userId: Int): Flow<Response<List<Post>>>
    suspend fun createPost(post: Post): Response<Post>
    suspend fun updatePost(post: Post): Response<Post>
    suspend fun patchPost(id: Int, patch: PatchPost): Response<Post>
    suspend fun deletePost(id: Int): Response<Boolean>
}