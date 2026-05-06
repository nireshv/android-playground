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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDeleteViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DeleteState())
    val state = _state.asStateFlow()

    private val _event = Channel<DeleteEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: DeleteAction) {
        when (action) {
            is DeleteAction.OnUserIdChanged -> {
                _state.value = _state.value.copy(userId = action.userId)
            }

            DeleteAction.OnSubmit -> {
                viewModelScope.launch {
                    val state = _state.value
                    val response = repo.deletePost(state.userId.toInt())
                    Log.w("Tag", "Delete response: ${response.status}")
                    _event.send(DeleteEvent.Back)
                }
            }
        }
    }
}

data class DeleteState(
    val userId: String = "",
)

sealed interface DeleteAction {
    data class OnUserIdChanged(val userId: String) : DeleteAction
    object OnSubmit : DeleteAction
}

sealed interface DeleteEvent {
    object Back : DeleteEvent
}