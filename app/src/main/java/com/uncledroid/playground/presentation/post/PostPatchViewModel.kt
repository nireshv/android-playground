package com.uncledroid.playground.presentation.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostPatchViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PatchState())
    val state = _state.asStateFlow()

    private val _event = Channel<PatchEvent>()
    val event = _event.receiveAsFlow()


    fun onAction(action: PatchAction) {
        when (action) {
            is PatchAction.OnUserIdChanged -> {
                _state.update { it.copy(userId = action.userId) }
            }

            is PatchAction.OnBodyChanged -> {
                _state.update { it.copy(body = action.body) }
            }

            PatchAction.OnSubmit -> {
                val state = _state.value
                viewModelScope.launch {
                    val response = repo.patchPost(state.userId.toInt(), mapOf("body" to state.body))
                    Log.w("TAG", "Patch response: $response")
                    _event.send(PatchEvent.Back)
                }
            }
        }
    }
}

data class PatchState(
    val userId: String = "",
    val body: String = ""
)

sealed interface PatchAction {
    data class OnUserIdChanged(val userId: String) : PatchAction
    data class OnBodyChanged(val body: String) : PatchAction
    object OnSubmit : PatchAction
}

sealed interface PatchEvent {
    object Back : PatchEvent
}