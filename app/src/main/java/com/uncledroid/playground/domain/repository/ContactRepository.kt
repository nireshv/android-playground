package com.uncledroid.playground.domain.repository

import com.uncledroid.playground.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    val allContact: Flow<List<Contact>>
    fun getContact(id: Int): Flow<Contact?>
    suspend fun update(contact: Contact)
}