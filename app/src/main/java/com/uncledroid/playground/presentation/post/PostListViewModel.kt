package com.uncledroid.playground.presentation.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(list = repo.getPosts(2)) }
        }
    }

    fun action(action: ListAction) {
        when (action) {
            is ListAction.OnPostSelected -> {

            }
        }
    }
}

data class ListState(
    val list: List<Post> = emptyList()
)

sealed interface ListAction {
    data class OnPostSelected(val contactId: Int) : ListAction
}