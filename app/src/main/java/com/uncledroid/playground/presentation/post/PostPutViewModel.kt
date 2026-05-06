package com.uncledroid.playground.presentation.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Post
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
class PostPutViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PutState())
    val state = _state.asStateFlow()

    private val _event = Channel<PutEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: PutAction) {
        when (action) {
            is PutAction.OnIdChanged -> {
                _state.update { it.copy(id = action.id) }
            }

            is PutAction.OnUserIdChanged -> {
                _state.update { it.copy(userId = action.userId) }
            }

            is PutAction.OnBodyChanged -> {
                _state.update { it.copy(body = action.body) }
            }

            is PutAction.OnTitleChanged -> {
                _state.update { it.copy(title = action.title) }
            }

            PutAction.OnSubmit -> {
                val state = _state.value
                val newPost = Post(
                    id = state.id.toInt(),
                    userId = state.userId.toInt(),
                    title = state.title,
                    body = state.body
                )
                viewModelScope.launch {
                    Log.w("TAG", "Put post state: $state")
                    val response = repo.putPost(newPost)
                    Log.w("TAG", "Put response: $response")
                    _event.send(PutEvent.Back)
                }
            }
        }
    }
}

data class PutState(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = ""
)

sealed interface PutAction {
    data class OnIdChanged(val id: String) : PutAction
    data class OnUserIdChanged(val userId: String) : PutAction
    data class OnTitleChanged(val title: String) : PutAction
    data class OnBodyChanged(val body: String) : PutAction
    object OnSubmit : PutAction
}

sealed interface PutEvent {
    object Back : PutEvent
}