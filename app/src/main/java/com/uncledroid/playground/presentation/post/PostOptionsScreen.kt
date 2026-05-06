package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


val options = listOf("Create Post", "List Post", "Update Post", "Patch Post", "Delete Post")

@Composable
fun PostOptions(onSelect: (String) -> Unit) {

    LazyColumn {
        items(options.size) {
            ListItem(
                modifier = Modifier.clickable(onClick = { onSelect(options[it]) }),
                headlineContent = {
                    Text(options[it])
                }
            )
        }
    }
}