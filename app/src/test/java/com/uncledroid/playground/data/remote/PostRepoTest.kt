package com.uncledroid.playground.data.remote

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
import org.junit.Test

class PostRepoTest {

    fun mockClient(
        response: String = "",
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (HttpRequestData) -> Unit = {}
    ) = HttpClient(engine = MockEngine { request ->
        onRequest(request)
        respond(
            content = response,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    fun postJson(
        id: String = "1",
        userId: String = "1",
        title: String = "test title",
        body: String = "test body",
    ) = """{"id":$id,"userId":$userId,"title":"$title","body":"$body"}"""

    @Test
    fun testGetPost() = runTest {
        val response = "[${postJson()}, ${postJson(id = "2")}, ${postJson(id = "3")}]"
        var requestType: HttpMethod? = null
        val client = mockClient(response) {
            requestType = it.method
        }

        val result = PostRepositoryImpl(client).getPosts(1)
        assertEquals(3, result.size)
        assertEquals(3, result[2].id)
        assertEquals(requestType, HttpMethod.Get)
    }
}