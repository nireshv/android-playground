package com.uncledroid.playground.data.remote

import android.util.Log
import com.uncledroid.playground.common.CoroutineDispatchers
import com.uncledroid.playground.data.remote.dto.PostResponse
import com.uncledroid.playground.data.remote.mapper.toPatchPostRequest
import com.uncledroid.playground.data.remote.mapper.toPost
import com.uncledroid.playground.data.remote.mapper.toPostRequest
import com.uncledroid.playground.di.ApplicationScope
import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.model.Response
import com.uncledroid.playground.domain.repository.PostFlowRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.io.IOException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

class PostFlowRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val dispatchers: CoroutineDispatchers,
    @ApplicationScope private val scope: CoroutineScope
) : PostFlowRepository {

    private val _refresh = MutableStateFlow(0)
    private fun refresh() = _refresh.update { Random.nextInt() }

    private suspend fun retry(cause: Throwable, attempt: Long): Boolean {
        return if (cause is IOException && attempt < 3) {
            val newDelay = 1000L * 2.0.pow(attempt.toDouble()).toLong()
            Log.w("Tag", "Retry waiting for $newDelay, attempt $attempt, ${cause.message}")
            delay(newDelay)
            true
        } else {
            false
        }
    }

    override val allPosts: Flow<Response<List<Post>>> =
        combine(_refresh) {
            client.get("/posts")
        }
            .retryWhen { cause, attempt -> retry(cause, attempt) }
            .map { response ->
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val result = response.body<List<PostResponse>>().map { it.toPost() }
                        Response.Success(result)
                    }

                    else -> {
                        Response.Error("Error : ${response.status}")
                    }
                }
            }
            .onStart { emit(Response.Loading) }
            .catch { emit(Response.Error(it.message ?: "Something went wrong")) }
            .flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.WhileSubscribed(500), Response.Success(emptyList()))

    override fun getPost(id: Int): Flow<Response<Post>> {
        return combine(_refresh) {
            client
                .get("/post/$id")
                .body<PostResponse>()
                .toPost()
        }
            .retryWhen { cause, attempt -> retry(cause, attempt) }
            .map<Post, Response<Post>> { Response.Success(it) }
            .onStart { emit(Response.Loading) }
            .catch { emit(Response.Error(it.message ?: "Something went wrong")) }
            .flowOn(dispatchers.io)
    }

    override fun getPostsForUser(userId: Int?): Flow<Response<List<Post>>> {
        Log.w("Tag", "getPostsForUser userId:$userId")
        val timerFlow = flow {
            while (true) {
                emit(Unit); delay(10_000)
            }
        }
        return combine(_refresh, timerFlow) { (ref) ->
            Log.w("Tag", "getPostsForUser $ref, userId:$userId")
            client
                .get("/posts") {
                    userId?.let { parameter("userId", userId) }
                }
        }
            .retryWhen { cause, attempt -> retry(cause, attempt) }
            .map { response ->
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val result = response.body<List<PostResponse>>().map { it.toPost() }
                        Response.Success(result)
                    }

                    else -> Response.Error("Error: ${response.status}")
                }
            }
            .onStart { emit(Response.Loading) }
            .catch { emit(Response.Error(it.message ?: "Something went wrong")) }
            .flowOn(dispatchers.io)
    }

    override suspend fun createPost(post: Post): Response<Post> {
        return try {
            val result = client
                .post("/posts") {
                    contentType(ContentType.Application.Json)
                    setBody(post.toPostRequest())
                }
                .body<PostResponse>()
                .toPost()

            refresh()
            Response.Success(result)
        } catch (e: IOException) {
            Response.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun updatePost(post: Post): Response<Post> {
        return try {
            val result = client
                .put("/posts/${post.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(post.toPostRequest())
                }
                .body<PostResponse>()
                .toPost()
            refresh()
            Response.Success(result)
        } catch (e: IOException) {
            Response.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun patchPost(id: Int, patch: PatchPost): Response<Post> {
        return try {
            val result = client
                .patch("/posts/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(patch.toPatchPostRequest())
                }
                .body<PostResponse>()
                .toPost()

            refresh()
            Response.Success(result)
        } catch (e: IOException) {
            Response.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun deletePost(id: Int): Response<Boolean> {
        return try {
            val result = client
                .delete("/posts/$id")
                .status.value == 200

            refresh()
            Response.Success(result)
        } catch (e: IOException) {
            Response.Error(e.message ?: "Something went wrong")
        }
    }
}