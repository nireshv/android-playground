package com.uncledroid.playground.presentation.contactdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncledroid.playground.domain.model.Contact
import com.uncledroid.playground.domain.repository.ContactRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ContactDetailViewModel.Factory::class)
class ContactDetailViewModel @AssistedInject constructor(
    @Assisted private val id: Int,
    private val repo: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getContact(id).collect { contact ->
                _state.update { it.copy(contact = contact) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: Int): ContactDetailViewModel
    }

    fun onAction(event: DetailEvent) {
        when (event) {
            DetailEvent.Update -> {
                viewModelScope.launch {
                    val updatedContact = state.value.contact?.let {
                        it.copy(name = "${it.name}1")
                    }
                    updatedContact?.let {
                        repo.update(updatedContact)
                    }
                }
            }
        }
    }

}

data class DetailState(
    val contact: Contact? = null
)

sealed interface DetailEvent {
    data object Update : DetailEvent
}