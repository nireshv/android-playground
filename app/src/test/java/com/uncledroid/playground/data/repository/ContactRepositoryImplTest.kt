package com.uncledroid.playground.data.repository

import com.uncledroid.playground.common.CoroutineDispatchers
import com.uncledroid.playground.domain.model.Contact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ContactRepositoryImpl

    @Before
    fun setUp() {
        repository = ContactRepositoryImpl(
            dispatchers = FakeCoroutineDispatchers(testDispatcher),
            scope = CoroutineScope(testDispatcher)
        )
    }

    @Test
    fun `allContact emits all 3 initial contacts`() = runTest(testDispatcher) {
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertEquals(3, contacts.size)
    }

    @Test
    fun `allContact contains pre-seeded contacts`() = runTest(testDispatcher) {
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertEquals(
            listOf(
                Contact(1, "Niresh", "niresh@gmail.com"),
                Contact(2, "Vasu", "vasu@gmail.com"),
                Contact(3, "Devan", "devan@gmail.com"),
            ),
            contacts
        )
    }

    @Test
    fun `getContact returns correct contact for valid id`() = runTest(testDispatcher) {
        val contact = repository.getContact(1).first { it != null }
        assertEquals(Contact(1, "Niresh", "niresh@gmail.com"), contact)
    }

    @Test
    fun `getContact returns null for non-existent id`() = runTest(testDispatcher) {
        val contact = repository.getContact(99).first()
        assertNull(contact)
    }

    @Test
    fun `update modifies existing contact`() = runTest(testDispatcher) {
        val updated = Contact(1, "Niresh V", "nireshv@gmail.com")
        repository.update(updated)
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertTrue(contacts.contains(updated))
    }

    @Test
    fun `update reflects change in getContact flow`() = runTest(testDispatcher) {
        val updated = Contact(2, "Vasu Updated", "vasu_new@gmail.com")
        repository.update(updated)
        val contact = repository.getContact(2).first { it != null }
        assertEquals(updated, contact)
    }

    @Test
    fun `update preserves other contacts`() = runTest(testDispatcher) {
        repository.update(Contact(1, "Updated", "new@gmail.com"))
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertTrue(contacts.any { it.id == 2 && it.name == "Vasu" })
        assertTrue(contacts.any { it.id == 3 && it.name == "Devan" })
    }

    @Test
    fun `update on non-existent id does not modify list`() = runTest(testDispatcher) {
        repository.update(Contact(99, "Ghost", "ghost@gmail.com"))
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertEquals(3, contacts.size)
        assertFalse(contacts.any { it.id == 99 })
    }

    @Test
    fun `sequential updates are all reflected`() = runTest(testDispatcher) {
        repository.update(Contact(1, "Niresh V", "nireshv@gmail.com"))
        repository.update(Contact(2, "Vasu V", "vasuv@gmail.com"))
        val contacts = repository.allContact.first { it.isNotEmpty() }
        assertEquals("Niresh V", contacts.first { it.id == 1 }.name)
        assertEquals("Vasu V", contacts.first { it.id == 2 }.name)
    }

    @Test
    fun `allContact re-emits automatically when update is called`() = runTest(testDispatcher) {
        val emissions = mutableListOf<Contact>()
        var emissionCount = 0
        val job = launch {
            repository.allContact.collect {
                emissionCount++
                emissions.apply {
                    clear()
                    addAll(it)
                }
            }
        }

        val beforeListSize = emissions.size
        val countBeforeUpdate = emissionCount

        repository.update(Contact(1, "Niresh V", "nireshv@gmail.com"))

        assertEquals(countBeforeUpdate + 1, emissionCount)
        assertEquals(beforeListSize, emissions.size)
        assertEquals("Niresh V", emissions.first { it.id == 1 }.name)

        job.cancel()
    }
}

private class FakeCoroutineDispatchers(
    dispatcher: CoroutineDispatcher
) : CoroutineDispatchers {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
}
