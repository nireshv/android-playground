package com.uncledroid.playground.presentation.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Contact
import com.uncledroid.playground.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repo: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.allContact.collect { list ->
                _state.update { it.copy(list = list) }
            }
        }
    }

    fun action(action: ListAction) {
        when (action) {
            is ListAction.OnContactSelected -> {

            }
        }
    }
}

data class ListState(
    val list: List<Contact> = emptyList()
)

sealed interface ListAction {
    data class OnContactSelected(val contactId: Int) : ListAction
}