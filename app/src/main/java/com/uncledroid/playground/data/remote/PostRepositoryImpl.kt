package com.uncledroid.playground.data.remote

import android.util.Log
import com.uncledroid.playground.data.remote.dto.PostResponse
import com.uncledroid.playground.data.remote.mapper.toPost
import com.uncledroid.playground.data.remote.mapper.toPostRequest
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.repository.PostRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PostRepositoryImpl(
    private val client: HttpClient
) : PostRepository {

    override suspend fun getPosts(userId: Int): List<Post> {
        return try {
            client
                .get("/posts") {
                    if (userId != -1) {
                        parameter("userId", userId)
                    }
                }
                .body<List<PostResponse>>()
                .map { it.toPost() }
        } catch (e: Exception) {
            Log.e("Error", "getPosts filter:$userId", e)
            emptyList()
        }
    }

    override suspend fun getPost(id: Int): Post? {
        return try {
            client
                .get("/posts/$id")
                .body<PostResponse>()
                .toPost()
        } catch (e: Exception) {
            Log.e("Error", "getPost $id", e)
            null
        }
    }

    override suspend fun createPost(post: Post): Post? {
        return try {
            client
                .post("/posts") {
                    contentType(ContentType.Application.Json)
                    setBody(post.toPostRequest())
                }
                .body<PostResponse>()
                .toPost()
        } catch (e: Exception) {
            Log.e("Error", "createPost", e)
            null
        }
    }

    override suspend fun putPost(post: Post): Post? {
        return try {
            client
                .put("/posts/${post.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(post.toPostRequest())
                }
                .body<PostResponse>()
                .toPost()
        } catch (e: Exception) {
            Log.e("Error", "putPost", e)
            null
        }
    }

    override suspend fun patchPost(id: Int, map: Map<String, String>): Post? {
        return try {
            client
                .patch("/posts/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(map)
                }
                .body<PostResponse>()
                .toPost()
        } catch (e: Exception) {
            Log.e("Error", "patchPost", e)
            null
        }
    }

    override suspend fun deletePost(id: Int): HttpResponse {
        return client.delete("/posts/$id")
    }


}