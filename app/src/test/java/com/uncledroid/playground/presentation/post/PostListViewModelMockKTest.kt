package com.uncledroid.playground.presentation.post

import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.model.Response
import com.uncledroid.playground.domain.repository.PostFlowRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostListViewModelMockKTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var repo: PostFlowRepository
    private lateinit var viewModel: PostListViewModel

    private val samplePost = Post(id = 1, userId = 10, title = "Title", body = "Body")

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        // default stub: return empty flow for any userId
        every { repo.getPostsForUser(any()) } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region initial state

    @Test
    fun `initial state is empty and not loading`() {
        viewModel = PostListViewModel(repo)
        assertEquals(ListState(), viewModel.state.value)
    }

    // endregion

    // region OnSearch / OnPostSelected actions

    @Test
    fun `OnSearch updates search in state`() {
        viewModel = PostListViewModel(repo)
        viewModel.onAction(ListAction.OnSearch("hello"))
        assertEquals("hello", viewModel.state.value.search)
    }

    @Test
    fun `OnPostSelected does not change state`() {
        viewModel = PostListViewModel(repo)
        val before = viewModel.state.value
        viewModel.onAction(ListAction.OnPostSelected(99))
        assertEquals(before, viewModel.state.value)
    }

    // endregion

    // region getPostsForUser routing

    @Test
    fun `empty search calls getPostsForUser with null`() = runTest(testDispatcher) {
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        verify { repo.getPostsForUser(null) }
    }

    @Test
    fun `numeric search calls getPostsForUser with parsed userId`() = runTest(testDispatcher) {
        every { repo.getPostsForUser(42) } returns emptyFlow()
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        viewModel.onAction(ListAction.OnSearch("42"))
        advanceUntilIdle()
        verify { repo.getPostsForUser(42) }
    }

    @Test
    fun `non-numeric search calls getPostsForUser with null`() = runTest(testDispatcher) {
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        viewModel.onAction(ListAction.OnSearch("android"))
        advanceUntilIdle()
        verify(atLeast = 1) { repo.getPostsForUser(null) }
    }

    // endregion

    // region response handling

    @Test
    fun `Loading response sets isLoading true`() = runTest(testDispatcher) {
        every { repo.getPostsForUser(null) } returns flowOf(Response.Loading)
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `Success response updates list and clears loading`() = runTest(testDispatcher) {
        every { repo.getPostsForUser(null) } returns flowOf(Response.Success(listOf(samplePost)))
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(samplePost), state.list)
    }

    @Test
    fun `Error response clears loading`() = runTest(testDispatcher) {
        every { repo.getPostsForUser(null) } returns flowOf(Response.Error("network error"))
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    // endregion

    // region debounce

    @Test
    fun `rapid search changes trigger only one call after debounce`() = runTest(testDispatcher) {
        viewModel = PostListViewModel(repo)
        advanceUntilIdle() // consume initial "" debounce
        clearMocks(repo, answers = false) // reset call records; keep stubs

        viewModel.onAction(ListAction.OnSearch("a"))
        viewModel.onAction(ListAction.OnSearch("ab"))
        viewModel.onAction(ListAction.OnSearch("abc"))
        advanceUntilIdle() // only "abc" fires

        verify(exactly = 1) { repo.getPostsForUser(null) }
    }

    @Test
    fun `search change within 300ms does not trigger a new call`() = runTest(testDispatcher) {
        viewModel = PostListViewModel(repo)
        advanceUntilIdle() // consume initial "" debounce

        viewModel.onAction(ListAction.OnSearch("k"))
        advanceTimeBy(299) // just under threshold — debounce has not fired

        // only the initial "" call, no new call for "k"
        verify(exactly = 1) { repo.getPostsForUser(null) }
    }

    @Test
    fun `search change after 300ms triggers a new call`() = runTest(testDispatcher) {
        viewModel = PostListViewModel(repo)
        advanceUntilIdle()

        viewModel.onAction(ListAction.OnSearch("k"))
        advanceTimeBy(300)
        advanceUntilIdle()

        // initial "" call + debounced "k" call (both map to null userId)
        verify(exactly = 2) { repo.getPostsForUser(null) }
    }

    // endregion
}