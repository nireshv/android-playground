package com.uncledroid.playground.presentation.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uncledroid.playground.domain.model.Post
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val posts = listOf(
        Post(id = 1, userId = 10, title = "First Post", body = "First body"),
        Post(id = 2, userId = 20, title = "Second Post", body = "Second body"),
    ).toImmutableList()

    private var state by mutableStateOf(ListState())
    private val actions = mutableListOf<ListAction>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PostListScreen(
                state = state,
                onAction = { action ->
                    actions.add(action)
                    if (action is ListAction.OnSearch) {
                        state = state.copy(search = action.search)
                    }
                },
            )
        }
    }

    // region static content

    @Test
    fun displaysHeaderText() {
        composeTestRule.onNodeWithText("Post List screen").assertIsDisplayed()
    }

    @Test
    fun displaysSearchLabel() {
        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
    }

    // endregion

    // region loading state

    @Test
    fun showsLoadingText_whenLoading() {
        state = state.copy(isLoading = true)
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun hidesLoadingText_whenNotLoading() {
        composeTestRule.onNodeWithText("Loading...").assertDoesNotExist()
    }

    @Test
    fun hidesPostList_whenLoading() {
        state = state.copy(isLoading = true, list = posts)
        composeTestRule.onNodeWithText("id: 1").assertDoesNotExist()
    }

    // endregion

    // region post list

    @Test
    fun showsAllPostItems_whenNotLoading() {
        state = state.copy(list = posts)
        composeTestRule.onNodeWithText("id: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("id: 2").assertIsDisplayed()
    }

    @Test
    fun displaysPostTitleAndBody() {
        state = state.copy(list = posts)
        composeTestRule.onNodeWithText("First Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("First body").assertIsDisplayed()
    }

    @Test
    fun displaysPostIdAndUserId() {
        state = state.copy(list = posts)
        composeTestRule.onNodeWithText("id: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("userId: 10").assertIsDisplayed()
    }

    // endregion

    // region state reflection

    @Test
    fun searchFieldReflectsState() {
        state = state.copy(search = "android")
        composeTestRule.onNodeWithText("android").assertIsDisplayed()
    }

    // endregion

    // region action dispatching

    @Test
    fun typingInSearchField_dispatchesOnSearch() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("kotlin")
        assertEquals(ListAction.OnSearch("kotlin"), actions.last())
        assertEquals("kotlin", state.search)
    }

    @Test
    fun clickingPost_dispatchesOnPostSelected() {
        state = state.copy(list = posts)
        // onNodeWithText finds the merged ListItem node (which carries the click action)
        composeTestRule.onNodeWithText("id: 1").performClick()
        assertEquals(ListAction.OnPostSelected(1), actions.last())
    }

    // endregion
}