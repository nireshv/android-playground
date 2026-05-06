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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostCreateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var state by mutableStateOf(CreateState())
    private val actions = mutableListOf<CreateAction>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PostCreateScreen(
                state = state,
                onAction = { action ->
                    actions.add(action)
                    state = when (action) {
                        is CreateAction.OnUserIdChanged -> state.copy(userId = action.userId)
                        is CreateAction.OnTitleChanged -> state.copy(title = action.title)
                        is CreateAction.OnBodyChanged -> state.copy(body = action.body)
                        CreateAction.OnSubmit -> state
                    }
                },
            )
        }
    }

    // region static content

    @Test
    fun displaysHeaderText() {
        composeTestRule.onNodeWithText("Post Create screen").assertIsDisplayed()
    }

    @Test
    fun displaysFieldLabels() {
        composeTestRule.onNodeWithText("User Id").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body").assertIsDisplayed()
    }

    @Test
    fun displaysSaveButton() {
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    // endregion

    // region state reflection

    @Test
    fun userIdFieldReflectsState() {
        state = state.copy(userId = "42")
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun titleFieldReflectsState() {
        state = state.copy(title = "My Title")
        composeTestRule.onNodeWithText("My Title").assertIsDisplayed()
    }

    @Test
    fun bodyFieldReflectsState() {
        state = state.copy(body = "My Body")
        composeTestRule.onNodeWithText("My Body").assertIsDisplayed()
    }

    // endregion

    // region action dispatching

    @Test
    fun typingInUserIdFieldDispatchesOnUserIdChanged() {
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("5")
        assertEquals(CreateAction.OnUserIdChanged("5"), actions.last())
    }

    @Test
    fun typingInTitleFieldDispatchesOnTitleChanged() {
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("New Title")
        assertEquals(CreateAction.OnTitleChanged("New Title"), actions.last())
    }

    @Test
    fun typingInBodyFieldDispatchesOnBodyChanged() {
        composeTestRule.onAllNodes(hasSetTextAction())[2].performTextInput("Some body text")
        assertEquals(CreateAction.OnBodyChanged("Some body text"), actions.last())
    }

    @Test
    fun clickingSaveDispatchesOnSubmit() {
        composeTestRule.onNodeWithText("Save").performClick()
        assertEquals(CreateAction.OnSubmit, actions.last())
    }

    // endregion

    // region userId digit filter

    @Test
    fun nonDigitInputInUserIdFieldIsIgnored() {
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("abc")
        assertTrue(
            "Non-digit input should not dispatch OnUserIdChanged",
            actions.filterIsInstance<CreateAction.OnUserIdChanged>().isEmpty(),
        )
    }

    @Test
    fun mixedInputInUserIdFieldIsIgnored() {
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("1a2")
        // "1a2".all { it.isDigit() } == false, so the whole string is rejected
        assertTrue(
            "Mixed digit/non-digit input should not dispatch OnUserIdChanged",
            actions.filterIsInstance<CreateAction.OnUserIdChanged>().isEmpty(),
        )
    }

    // endregion
}
