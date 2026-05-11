package com.uncledroid.playground.presentation.post

import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.model.Response
import com.uncledroid.playground.domain.repository.PostFlowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostListViewModelTest {

    // Fresh dispatcher per test so virtual time starts at 0 each time.
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var fakeRepo: FakePostFlowRepository
    private lateinit var viewModel: PostListViewModel

    private val samplePost = Post(id = 1, userId = 10, title = "Title", body = "Body")

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakePostFlowRepository()
        viewModel = PostListViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region initial state

    @Test
    fun `initial state has empty list, empty search, and is not loading`() {
        assertEquals(ListState(), viewModel.state.value)
    }

    // endregion

    // region OnSearch / OnPostSelected actions

    @Test
    fun `OnSearch updates search in state`() {
        viewModel.onAction(ListAction.OnSearch("hello"))
        assertEquals("hello", viewModel.state.value.search)
    }

    @Test
    fun `OnPostSelected does not change state`() {
        val before = viewModel.state.value
        viewModel.onAction(ListAction.OnPostSelected(99))
        assertEquals(before, viewModel.state.value)
    }

    // endregion

    // region getPostsForUser routing

    @Test
    fun `empty search calls getPostsForUser with null userId`() = runTest(testDispatcher) {
        advanceUntilIdle() // 300ms debounce for initial ""
        assertNull(fakeRepo.capturedUserIds.first())
    }

    @Test
    fun `numeric search calls getPostsForUser with parsed userId`() = runTest(testDispatcher) {
        advanceUntilIdle() // consume initial debounce
        viewModel.onAction(ListAction.OnSearch("42"))
        advanceUntilIdle()
        assertEquals(42, fakeRepo.capturedUserIds.last())
    }

    @Test
    fun `non-numeric search calls getPostsForUser with null`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onAction(ListAction.OnSearch("android"))
        advanceUntilIdle()
        assertNull(fakeRepo.capturedUserIds.last())
    }

    // endregion

    // region response handling

    @Test
    fun `Loading response sets isLoading true`() = runTest(testDispatcher) {
        fakeRepo.postsResponse = flowOf(Response.Loading)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `Success response updates list and clears loading`() = runTest(testDispatcher) {
        fakeRepo.postsResponse = flowOf(Response.Success(listOf(samplePost)))
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(samplePost), state.list)
    }

    @Test
    fun `Error response clears loading`() = runTest(testDispatcher) {
        fakeRepo.postsResponse = flowOf(Response.Error("network error"))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    // endregion

    // region debounce

    @Test
    fun `rapid search changes trigger only one call after debounce`() = runTest(testDispatcher) {
        advanceUntilIdle() // consume initial "" debounce → 1 call
        val callsAfterInit = fakeRepo.capturedUserIds.size

        viewModel.onAction(ListAction.OnSearch("a"))
        viewModel.onAction(ListAction.OnSearch("ab"))
        viewModel.onAction(ListAction.OnSearch("abc"))
        advanceUntilIdle() // only "abc" fires → 1 more call

        assertEquals(callsAfterInit + 1, fakeRepo.capturedUserIds.size)
    }

    @Test
    fun `search change within 300ms does not trigger a new call`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val callsAfterInit = fakeRepo.capturedUserIds.size

        viewModel.onAction(ListAction.OnSearch("k"))
        advanceTimeBy(299) // just under threshold

        assertEquals(callsAfterInit, fakeRepo.capturedUserIds.size)
    }

    @Test
    fun `search change after 300ms triggers a new call`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val callsAfterInit = fakeRepo.capturedUserIds.size

        viewModel.onAction(ListAction.OnSearch("k"))
        advanceTimeBy(300) // at threshold
        advanceUntilIdle() // run the triggered coroutine

        assertEquals(callsAfterInit + 1, fakeRepo.capturedUserIds.size)
    }

    // endregion
}

private class FakePostFlowRepository : PostFlowRepository {
    val capturedUserIds = mutableListOf<Int?>()
    var postsResponse: Flow<Response<List<Post>>> = emptyFlow()

    override val allPosts: Flow<Response<List<Post>>> = emptyFlow()
    override fun getPost(id: Int): Flow<Response<Post>> = emptyFlow()

    override fun getPostsForUser(userId: Int?): Flow<Response<List<Post>>> {
        capturedUserIds.add(userId)
        return postsResponse
    }

    override suspend fun createPost(post: Post): Response<Post> = Response.Error("not used")
    override suspend fun updatePost(post: Post): Response<Post> = Response.Error("not used")
    override suspend fun patchPost(id: Int, patch: PatchPost): Response<Post> = Response.Error("not used")
    override suspend fun deletePost(id: Int): Response<Boolean> = Response.Error("not used")
}