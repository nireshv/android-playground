package com.uncledroid.playground.data.remote

import com.uncledroid.playground.common.CoroutineDispatchers
import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.model.Response
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostFlowRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val appScope = CoroutineScope(testDispatcher)
    private val json = Json { ignoreUnknownKeys = true }

    private val testDispatchers = object : CoroutineDispatchers {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }

    private val samplePost = Post(id = 1, userId = 1, title = "test title", body = "test body")

    private fun postJson(
        id: Int = 1,
        userId: Int = 1,
        title: String = "test title",
        body: String = "test body",
    ) = """{"id":$id,"userId":$userId,"title":"$title","body":"$body"}"""

    private fun mockClient(
        status: HttpStatusCode = HttpStatusCode.OK,
        content: String = "",
        onRequest: (HttpRequestData) -> Unit = {},
        responseDelay: Long = 0L,
    ) = HttpClient(MockEngine { request ->
        onRequest(request)
        if (responseDelay > 0L) delay(responseDelay)
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }) {
        install(ContentNegotiation) { json(json) }
    }

    private fun mockFailingClient() = HttpClient(MockEngine {
        throw IOException("Network failure")
    }) {
        install(ContentNegotiation) { json(json) }
    }

    private fun createRepo(client: HttpClient, scope: CoroutineScope = appScope) = PostFlowRepositoryImpl(
        client = client,
        dispatchers = testDispatchers,
        scope = scope,
    )

    // StandardTestDispatcher needed for stateIn tests: advanceUntilIdle() drains the
    // flowOn channel and WhileSubscribed startup in the right order.
    private fun TestScope.stdDispatchers(): CoroutineDispatchers {
        val d = StandardTestDispatcher(testScheduler)
        return object : CoroutineDispatchers {
            override val main: CoroutineDispatcher = d
            override val io: CoroutineDispatcher = d
            override val default: CoroutineDispatcher = d
        }
    }

    private fun TestScope.createRepoStd(client: HttpClient) = PostFlowRepositoryImpl(
        client = client,
        dispatchers = stdDispatchers(),
        scope = backgroundScope,
    )

    // region allPosts
    // allPosts is a StateFlow (stateIn). responseDelay=1 creates a 1ms virtual-time gap between
    // onStart(Loading) and the response so the flowOn channel drains Loading before Success arrives,
    // preventing StateFlow conflation from swallowing the Loading emission.
    // take(3) = [initial Success(empty), Loading, actual result]

    @Test
    fun `allPosts emits Loading then Success on 200`() = runTest {
        val repo = createRepoStd(
            mockClient(content = """[${postJson(1)},${postJson(2, userId = 2)}]""", responseDelay = 1L)
        )
        val emissions = repo.allPosts.take(3).toList()
        assertEquals(Response.Loading, emissions[1])
        val data = (emissions[2] as Response.Success<List<Post>>).data
        assertEquals(2, data.size)
        assertEquals(samplePost, data[0])
    }

    @Test
    fun `allPosts emits Error on non-200 status`() = runTest {
        val repo = createRepoStd(mockClient(status = HttpStatusCode.InternalServerError, responseDelay = 1L))
        val emissions = repo.allPosts.take(3).toList()
        assertEquals(Response.Loading, emissions[1])
        assertTrue(emissions[2] is Response.Error)
    }

    @Test
    fun `allPosts emits Error after retries on network failure`() = runTest {
        // retry delays (1s, 2s, 4s) are sufficient gaps — no extra responseDelay needed
        val repo = createRepoStd(mockFailingClient())
        val emissions = repo.allPosts.take(3).toList()
        assertEquals(Response.Loading, emissions[1])
        assertTrue(emissions[2] is Response.Error)
    }

    // endregion

    // region getPost

    @Test
    fun `getPost emits Loading then Success`() = runTest(testDispatcher) {
        val repo = createRepo(mockClient(content = postJson(id = 3)))
        val emissions = repo.getPost(3).take(2).toList()
        assertEquals(Response.Loading, emissions[0])
        assertEquals(3, (emissions[1] as Response.Success).data.id)
    }

    @Test
    fun `getPost sends GET to correct path`() = runTest(testDispatcher) {
        var path = ""
        val repo = createRepo(
            mockClient(
                content = postJson(id = 7),
                onRequest = { path = it.url.encodedPath },
            )
        )
        repo.getPost(7).take(2).toList()
        assertEquals("/post/7", path)
    }

    @Test
    fun `getPost emits Error on network failure`() = runTest(testDispatcher) {
        val emissions = createRepo(mockFailingClient()).getPost(1).take(2).toList()
        assertEquals(Response.Loading, emissions[0])
        assertTrue(emissions[1] is Response.Error)
    }

    // endregion

    // region getPostsForUser

    @Test
    fun `getPostsForUser emits Loading then Success`() = runTest(testDispatcher) {
        val repo = createRepo(mockClient(content = """[${postJson()}]"""))
        val emissions = repo.getPostsForUser(null).take(2).toList()
        assertEquals(Response.Loading, emissions[0])
        val data = (emissions[1] as Response.Success).data
        assertEquals(1, data.size)
    }

    @Test
    fun `getPostsForUser with userId adds query parameter`() = runTest(testDispatcher) {
        var params = emptyMap<String, List<String>>()
        val repo = createRepo(
            mockClient(
                content = """[${postJson()}]""",
                onRequest = { params = it.url.parameters.entries().associate { e -> e.key to e.value } },
            )
        )
        repo.getPostsForUser(2).take(2).toList()
        assertEquals("2", params["userId"]?.first())
    }

    @Test
    fun `getPostsForUser without userId omits query parameter`() = runTest(testDispatcher) {
        var params = emptyMap<String, List<String>>()
        val repo = createRepo(
            mockClient(
                content = "[]",
                onRequest = { params = it.url.parameters.entries().associate { e -> e.key to e.value } },
            )
        )
        repo.getPostsForUser(null).take(2).toList()
        assertFalse(params.containsKey("userId"))
    }

    @Test
    fun `getPostsForUser polls repeatedly`() = runTest(testDispatcher) {
        val repo = createRepo(mockClient(content = """[${postJson()}]"""))
        // Loading + 3 poll results (virtual time advances through each 10s delay)
        val emissions = repo.getPostsForUser(null).take(4).toList()
        assertEquals(Response.Loading, emissions[0])
        assertTrue(emissions[1] is Response.Success)
        assertTrue(emissions[2] is Response.Success)
        assertTrue(emissions[3] is Response.Success)
    }

    @Test
    fun `getPostsForUser emits Error on non-200 status`() = runTest(testDispatcher) {
        val repo = createRepo(mockClient(status = HttpStatusCode.NotFound))
        val emissions = repo.getPostsForUser(null).take(2).toList()
        assertEquals(Response.Loading, emissions[0])
        assertTrue(emissions[1] is Response.Error)
    }

    // endregion

    // region createPost

    @Test
    fun `createPost sends POST to correct path`() = runTest(testDispatcher) {
        var method: HttpMethod? = null
        var path = ""
        val repo = createRepo(
            mockClient(
                content = postJson(id = 101),
                onRequest = { method = it.method; path = it.url.encodedPath },
            )
        )
        repo.createPost(samplePost)
        assertEquals(HttpMethod.Post, method)
        assertEquals("/posts", path)
    }

    @Test
    fun `createPost returns Success with mapped post`() = runTest(testDispatcher) {
        val result = createRepo(mockClient(content = postJson(id = 101))).createPost(samplePost)
        assertEquals(101, (result as Response.Success).data.id)
    }

    @Test
    fun `createPost returns Error on network failure`() = runTest(testDispatcher) {
        val result = createRepo(mockFailingClient()).createPost(samplePost)
        assertTrue(result is Response.Error)
    }

    // endregion

    // region updatePost

    @Test
    fun `updatePost sends PUT to correct path`() = runTest(testDispatcher) {
        var method: HttpMethod? = null
        var path = ""
        val repo = createRepo(
            mockClient(
                content = postJson(id = 5),
                onRequest = { method = it.method; path = it.url.encodedPath },
            )
        )
        repo.updatePost(samplePost.copy(id = 5))
        assertEquals(HttpMethod.Put, method)
        assertEquals("/posts/5", path)
    }

    @Test
    fun `updatePost returns Success with mapped post`() = runTest(testDispatcher) {
        val result = createRepo(mockClient(content = postJson(id = 5))).updatePost(samplePost.copy(id = 5))
        assertEquals(5, (result as Response.Success).data.id)
    }

    @Test
    fun `updatePost returns Error on network failure`() = runTest(testDispatcher) {
        val result = createRepo(mockFailingClient()).updatePost(samplePost)
        assertTrue(result is Response.Error)
    }

    // endregion

    // region patchPost

    @Test
    fun `patchPost sends PATCH to correct path`() = runTest(testDispatcher) {
        var method: HttpMethod? = null
        var path = ""
        val repo = createRepo(
            mockClient(
                content = postJson(id = 2, title = "updated"),
                onRequest = { method = it.method; path = it.url.encodedPath },
            )
        )
        repo.patchPost(2, PatchPost(title = "updated"))
        assertEquals(HttpMethod.Patch, method)
        assertEquals("/posts/2", path)
    }

    @Test
    fun `patchPost returns Success with updated post`() = runTest(testDispatcher) {
        val result = createRepo(mockClient(content = postJson(id = 2, title = "updated")))
            .patchPost(2, PatchPost(title = "updated"))
        assertEquals("updated", (result as Response.Success).data.title)
    }

    @Test
    fun `patchPost returns Error on network failure`() = runTest(testDispatcher) {
        val result = createRepo(mockFailingClient()).patchPost(1, PatchPost(title = "t"))
        assertTrue(result is Response.Error)
    }

    // endregion

    // region deletePost

    @Test
    fun `deletePost sends DELETE to correct path`() = runTest(testDispatcher) {
        var method: HttpMethod? = null
        var path = ""
        val repo = createRepo(
            mockClient(
                status = HttpStatusCode.OK,
                onRequest = { method = it.method; path = it.url.encodedPath },
            )
        )
        repo.deletePost(4)
        assertEquals(HttpMethod.Delete, method)
        assertEquals("/posts/4", path)
    }

    @Test
    fun `deletePost returns Success true on 200`() = runTest(testDispatcher) {
        val result = createRepo(mockClient(status = HttpStatusCode.OK)).deletePost(1)
        assertEquals(Response.Success(true), result)
    }

    @Test
    fun `deletePost returns Error on network failure`() = runTest(testDispatcher) {
        val result = createRepo(mockFailingClient()).deletePost(1)
        assertTrue(result is Response.Error)
    }

    // endregion
}