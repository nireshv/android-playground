package com.uncledroid.playground.di

import com.uncledroid.playground.common.CoroutineDispatchers
import com.uncledroid.playground.common.DefaultCoroutineDispatchers
import com.uncledroid.playground.data.remote.PostRepositoryImpl
import com.uncledroid.playground.data.repository.ContactRepositoryImpl
import com.uncledroid.playground.domain.repository.ContactRepository
import com.uncledroid.playground.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun provideContactRepository(repo: ContactRepositoryImpl): ContactRepository

    @Singleton
    @Binds
    abstract fun bindCoroutineDispatchers(dispatchers: DefaultCoroutineDispatchers): CoroutineDispatchers

    companion object {
        @Singleton
        @Provides
        @ApplicationScope
        fun provideApplicationScope(dispatchers: CoroutineDispatchers): CoroutineScope =
            CoroutineScope(dispatchers.default + SupervisorJob())

        @Singleton
        @Provides
        fun providePostRepository(): PostRepository {
            return PostRepositoryImpl(
                HttpClient {

                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }

                    install(HttpTimeout) {
                        socketTimeoutMillis = 10000
                        requestTimeoutMillis = 10000
                        connectTimeoutMillis = 10000
                    }

                    install(DefaultRequest) {
                        url {
                            host = "jsonplaceholder.typicode.com"
                            protocol = URLProtocol.HTTPS
                        }
                        header(HttpHeaders.Authorization, "fkasjflkasjfasklfd")
                        contentType(ContentType.Application.Json)
                    }
                }
            )
        }
    }

}