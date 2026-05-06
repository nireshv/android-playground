package com.uncledroid.playground.presentation.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostCreateViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CreateState())
    val state = _state.asStateFlow()

    fun onAction(action: CreateAction) {
        when (action) {
            is CreateAction.OnUserIdChanged -> {
                _state.value = _state.value.copy(userId = action.userId)
            }

            is CreateAction.OnBodyChanged -> {
                _state.value = _state.value.copy(body = action.body)
            }

            is CreateAction.OnTitleChanged -> {
                _state.value = _state.value.copy(title = action.title)
            }

            CreateAction.OnSubmit -> {
                val state = _state.value
                val newPost =
                    Post(userId = state.userId.toInt(), title = state.title, body = state.body)
                viewModelScope.launch {
                    val response = repo.createPost(newPost)
                    Log.w("TAG", "Create response: $response")
                }
            }
        }
    }
}

data class CreateState(
    val userId: String = "",
    val title: String = "",
    val body: String = ""
)

sealed interface CreateAction {
    data class OnUserIdChanged(val userId: String) : CreateAction
    data class OnTitleChanged(val title: String) : CreateAction
    data class OnBodyChanged(val body: String) : CreateAction
    object OnSubmit : CreateAction
}