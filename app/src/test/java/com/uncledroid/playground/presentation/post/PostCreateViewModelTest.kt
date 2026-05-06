package com.uncledroid.playground.presentation.post

import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.repository.PostRepository
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostCreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakePostRepository
    private lateinit var viewModel: PostCreateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakePostRepository()
        viewModel = PostCreateViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields`() {
        assertEquals(CreateState(), viewModel.state.value)
    }

    @Test
    fun `OnUserIdChanged updates userId`() {
        viewModel.onAction(CreateAction.OnUserIdChanged("42"))
        assertEquals("42", viewModel.state.value.userId)
    }

    @Test
    fun `OnTitleChanged updates title`() {
        viewModel.onAction(CreateAction.OnTitleChanged("My Title"))
        assertEquals("My Title", viewModel.state.value.title)
    }

    @Test
    fun `OnBodyChanged updates body`() {
        viewModel.onAction(CreateAction.OnBodyChanged("My Body"))
        assertEquals("My Body", viewModel.state.value.body)
    }

    @Test
    fun `multiple field updates accumulate in state`() {
        viewModel.onAction(CreateAction.OnUserIdChanged("5"))
        viewModel.onAction(CreateAction.OnTitleChanged("Title"))
        viewModel.onAction(CreateAction.OnBodyChanged("Body"))
        assertEquals(
            CreateState(userId = "5", title = "Title", body = "Body"),
            viewModel.state.value
        )
    }

    @Test
    fun `OnSubmit calls createPost with post built from state`() = runTest {
        viewModel.onAction(CreateAction.OnUserIdChanged("1"))
        viewModel.onAction(CreateAction.OnTitleChanged("Test Title"))
        viewModel.onAction(CreateAction.OnBodyChanged("Test Body"))

        viewModel.onAction(CreateAction.OnSubmit)

        assertEquals(
            Post(id = 1, userId = 1, title = "Test Title", body = "Test Body"),
            fakeRepo.lastCreatedPost,
        )
        assertEquals(1, fakeRepo.lastCreatedPost?.id)
    }

    @Test
    fun `OnSubmit does not change state`() = runTest {
        viewModel.onAction(CreateAction.OnUserIdChanged("1"))
        viewModel.onAction(CreateAction.OnTitleChanged("Title"))
        viewModel.onAction(CreateAction.OnBodyChanged("Body"))
        val stateBefore = viewModel.state.value

        viewModel.onAction(CreateAction.OnSubmit)

        assertEquals(stateBefore, viewModel.state.value)
    }

}

private class FakePostRepository : PostRepository {
    var lastCreatedPost: Post? = null

    override suspend fun createPost(post: Post): Post? {
        lastCreatedPost = post.copy(id = 1)
        return post
    }

    override suspend fun getPosts(userId: Int): List<Post> = emptyList()
    override suspend fun getPost(id: Int): Post? = null
    override suspend fun putPost(post: Post): Post? = null
    override suspend fun patchPost(id: Int, map: Map<String, String>): Post? = null
    override suspend fun deletePost(id: Int): HttpResponse = TODO()
}
