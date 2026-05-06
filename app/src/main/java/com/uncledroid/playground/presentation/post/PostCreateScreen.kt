package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PostCreateScreen(state: CreateState, onAction: (CreateAction) -> Unit) {
    Column {
        Text("Post Create screen")
        TextField(
            value = state.userId,
            label = { Text("User Id") },
            placeholder = { Text("Enter User Id") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onAction(CreateAction.OnUserIdChanged(newValue))
                }
            }
        )
        TextField(
            value = state.title,
            label = { Text("Title") },
            placeholder = { Text("Enter Title") },
            singleLine = true,
            maxLines = 1,
            onValueChange = { newValue ->
                onAction(CreateAction.OnTitleChanged(newValue))
            }
        )
        TextField(
            value = state.body,
            label = { Text("Body") },
            placeholder = { Text("Enter Body") },
            onValueChange = { newValue ->
                onAction(CreateAction.OnBodyChanged(newValue))
            }
        )
        Button(onClick = {
            onAction(CreateAction.OnSubmit)
        }) {
            Text("Save")
        }
    }
}