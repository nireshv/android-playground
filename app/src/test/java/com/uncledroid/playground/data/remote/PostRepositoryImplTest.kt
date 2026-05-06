package com.uncledroid.playground.data.remote

import com.uncledroid.playground.domain.model.Post
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }

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
    ) = HttpClient(MockEngine { request ->
        onRequest(request)
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }) {
        install(ContentNegotiation) { json(json) }
//        install(DefaultRequest) { url("http://localhost/") }
    }

    // region getPosts

    @Test
    fun `getPosts returns mapped list`() = runTest {
        val client = mockClient(content = """[${postJson(1)},${postJson(2, userId = 2)}]""")
        val result = PostRepositoryImpl(client).getPosts()
        assertEquals(2, result.size)
        assertEquals(samplePost, result[0])
        assertEquals(2, result[1].id)
        assertEquals(2, result[1].userId)
    }

    @Test
    fun `getPosts with userId adds query parameter`() = runTest {
        var params = emptyMap<String, List<String>>()
        val client = mockClient(
            content = """[${postJson()}]""",
            onRequest = {
                params = it.url.parameters.entries().associate { e -> e.key to e.value }
            },
        )
        PostRepositoryImpl(client).getPosts(userId = 1)
        assertEquals("1", params["userId"]?.first())
    }

    @Test
    fun `getPosts without userId filter omits query parameter`() = runTest {
        var params = emptyMap<String, List<String>>()
        val client = mockClient(
            content = "[]",
            onRequest = {
                params = it.url.parameters.entries().associate { e -> e.key to e.value }
            },
        )
        PostRepositoryImpl(client).getPosts(userId = -1)
        assertTrue("userId should not be present", !params.containsKey("userId"))
    }

    @Test
    fun `getPosts returns empty list on error`() = runTest {
        val client = mockClient(status = HttpStatusCode.InternalServerError, content = "invalid")
        assertTrue(PostRepositoryImpl(client).getPosts().isEmpty())
    }

    // endregion

    // region getPost

    @Test
    fun `getPost returns mapped post`() = runTest {
        val client = mockClient(content = postJson(id = 3))
        val result = PostRepositoryImpl(client).getPost(3)
        assertEquals(3, result?.id)
        assertEquals("test title", result?.title)
    }

    @Test
    fun `getPost sends GET to correct path`() = runTest {
        var path = ""
        val client = mockClient(
            content = postJson(id = 7),
            onRequest = { path = it.url.encodedPath },
        )
        PostRepositoryImpl(client).getPost(7)
        assertEquals("/posts/7", path)
    }

    @Test
    fun `getPost returns null on error`() = runTest {
        val client = mockClient(status = HttpStatusCode.NotFound, content = "invalid")
        assertNull(PostRepositoryImpl(client).getPost(999))
    }

    // endregion

    // region createPost

    @Test
    fun `createPost sends POST to correct path`() = runTest {
        var method: HttpMethod? = null
        var path = ""
        val client = mockClient(
            content = postJson(id = 101),
            onRequest = { method = it.method; path = it.url.encodedPath },
        )
        val result = PostRepositoryImpl(client).createPost(samplePost)
        assertEquals(HttpMethod.Post, method)
        assertEquals("/posts", path)
        assertEquals(101, result?.id)
    }

    @Test
    fun `createPost returns null on error`() = runTest {
        val client = mockClient(status = HttpStatusCode.BadRequest, content = "invalid")
        assertNull(PostRepositoryImpl(client).createPost(samplePost))
    }

    // endregion

    // region putPost

    @Test
    fun `putPost sends PUT to correct path`() = runTest {
        var method: HttpMethod? = null
        var path = ""
        val client = mockClient(
            content = postJson(id = 5),
            onRequest = { method = it.method; path = it.url.encodedPath },
        )
        val result = PostRepositoryImpl(client).putPost(samplePost.copy(id = 5))
        assertEquals(HttpMethod.Put, method)
        assertEquals("/posts/5", path)
        assertEquals(5, result?.id)
    }

    @Test
    fun `putPost returns null on error`() = runTest {
        val client = mockClient(status = HttpStatusCode.NotFound, content = "invalid")
        assertNull(PostRepositoryImpl(client).putPost(samplePost))
    }

    // endregion

    // region patchPost

    @Test
    fun `patchPost sends PATCH to correct path`() = runTest {
        var method: HttpMethod? = null
        var path = ""
        val client = mockClient(
            content = postJson(id = 2, title = "updated"),
            onRequest = { method = it.method; path = it.url.encodedPath },
        )
        val result = PostRepositoryImpl(client).patchPost(2, mapOf("title" to "updated"))
        assertEquals(HttpMethod.Patch, method)
        assertEquals("/posts/2", path)
        assertEquals("updated", result?.title)
    }

    @Test
    fun `patchPost returns null on error`() = runTest {
        val client = mockClient(status = HttpStatusCode.InternalServerError, content = "invalid")
        assertNull(PostRepositoryImpl(client).patchPost(1, mapOf("title" to "t")))
    }

    // endregion

    // region deletePost

    @Test
    fun `deletePost sends DELETE to correct path`() = runTest {
        var method: HttpMethod? = null
        var path = ""
        val client = mockClient(
            status = HttpStatusCode.OK,
            content = "",
            onRequest = { method = it.method; path = it.url.encodedPath },
        )
        val response = PostRepositoryImpl(client).deletePost(4)
        assertEquals(HttpMethod.Delete, method)
        assertEquals("/posts/4", path)
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // endregion
}
