package com.uncledroid.playground.presentation.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.common.Response
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.repository.PostFlowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repo: PostFlowRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState())
    val state = _state.asStateFlow()

    private val search = MutableStateFlow("")

    init {
        viewModelScope.launch {
//            _state.update { it.copy(list = repo.getPosts(2)) }
            search.flatMapLatest { search ->
                Log.w("Tag", "Search value: $search")
                _state.update { it.copy(search = search) }
                val userId = try {
                    search.toInt()
                } catch (e: Exception) {
                    null
                }
                repo.getPostsForUser(userId)
            }.collect { postRes ->
                when (postRes) {
                    is Response.Error -> {
                        Log.e("Tag", "Error: ${postRes.message}")
                    }

                    is Response.Success -> {
                        Log.e("Tag", "Response.Success Post List: ${postRes.data.size}")
                        _state.update { it.copy(list = postRes.data) }
                    }
                }
            }
        }
    }

    fun onAction(action: ListAction) {
        when (action) {
            is ListAction.OnPostSelected -> {

            }

            is ListAction.OnSearch -> {
                Log.w("Tag", "")
                search.update { action.search }
            }
        }
    }
}

data class ListState(
    val list: List<Post> = emptyList(),
    val search: String = ""
)

sealed interface ListAction {
    data class OnPostSelected(val contactId: Int) : ListAction
    data class OnSearch(val search: String) : ListAction
}