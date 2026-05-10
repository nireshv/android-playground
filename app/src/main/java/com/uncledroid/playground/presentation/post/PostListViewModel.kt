package com.uncledroid.playground.presentation.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Post
import com.uncledroid.playground.domain.model.Response
import com.uncledroid.playground.domain.repository.PostFlowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repo: PostFlowRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState())
    val state = _state.asStateFlow()

    private val search = state.map { it.search }.distinctUntilChanged()

    init {
        viewModelScope.launch {
            search
                .debounce(300)
                .flatMapLatest { search ->
                    Log.w("Tag", "Search value: $search")
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
                            _state.update { it.copy(isLoading = false) }
                        }

                        is Response.Success -> {
                            Log.e("Tag", "Response.Success Post List: ${postRes.data.size}")
                            _state.update { it.copy(list = postRes.data, isLoading = false) }
                        }

                        is Response.Loading -> _state.update { it.copy(isLoading = true) }
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
                _state.update { it.copy(search = action.search) }
            }
        }
    }
}

data class ListState(
    val isLoading: Boolean = false,
    val list: List<Post> = emptyList(),
    val search: String = "",
)

sealed interface ListAction {
    data class OnPostSelected(val contactId: Int) : ListAction
    data class OnSearch(val search: String) : ListAction
}