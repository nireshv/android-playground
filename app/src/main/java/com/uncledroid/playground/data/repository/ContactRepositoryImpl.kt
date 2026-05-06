package com.uncledroid.playground.data.repository

import com.uncledroid.playground.common.CoroutineDispatchers
import com.uncledroid.playground.di.ApplicationScope
import com.uncledroid.playground.domain.model.Contact
import com.uncledroid.playground.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class ContactRepositoryImpl @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    @ApplicationScope private val scope: CoroutineScope
) : ContactRepository {
    private val mutex = Mutex()

    private val _refresh = MutableStateFlow(0)

    private val contacts = mutableListOf(
        Contact(1, "Niresh", "niresh@gmail.com"),
        Contact(2, "Vasu", "vasu@gmail.com"),
        Contact(3, "Devan", "devan@gmail.com"),
    )

    private suspend fun getContacts(): List<Contact> = mutex.withLock { contacts.toList() }

    private fun refresh() {
        _refresh.update { Random.nextInt() }
    }

    override val allContact: Flow<List<Contact>> =
        combine(_refresh) {
            getContacts()
        }.flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.WhileSubscribed(500), emptyList())

    override fun getContact(id: Int): Flow<Contact?> {
        return combine(_refresh) {
            getContacts().firstOrNull { it.id == id }
        }.flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.WhileSubscribed(500), null)
    }

    override suspend fun update(contact: Contact) = withContext(dispatchers.io) {
        mutex.withLock {
            val index = contacts.indexOfFirst { it.id == contact.id }
            if (index != -1) {
                contacts[index] = contact
                refresh()
            }
        }
    }
}