package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PostListScreen(state: ListState, onAction: (ListAction) -> Unit) {
    Column {
        Text("Post List screen", modifier = Modifier)
        TextField(
            value = state.search,
            label = { Text("Search") },
            onValueChange = { onAction(ListAction.OnSearch(it)) }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.list) {
                ListItem(
                    modifier = Modifier.clickable(onClick = { onAction(ListAction.OnPostSelected(it.id)) }),
                    headlineContent = {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("id: ${it.id}")
                            Text("userId: ${it.userId}")
                        }
                    },
                    supportingContent = {
                        Column {
                            Text(fontWeight = FontWeight.Bold, text = it.title)
                            Text(it.body)
                        }
                    }
                )
            }
        }
    }
}
