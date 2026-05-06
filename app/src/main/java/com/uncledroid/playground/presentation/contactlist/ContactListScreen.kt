package com.uncledroid.playground.presentation.contactlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ContactListScreen(state: ListState, onAction: (ListAction) -> Unit) {
    Column {
        Text("Contact List screen", modifier = Modifier)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.list) {
                ListItem(
                    modifier = Modifier.clickable(onClick = {
                        onAction(
                            ListAction.OnContactSelected(
                                it.id
                            )
                        )
                    }),
                    headlineContent = {
                        Text("Name: ${it.name}")
                    }
                )
            }
        }
    }
}
