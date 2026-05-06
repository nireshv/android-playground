package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PostPutScreen(state: PutState, onAction: (PutAction) -> Unit) {
    Column {
        Text("Post Put screen")
        TextField(
            value = state.id,
            label = { Text("Id") },
            placeholder = { Text("Enter Id") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onAction(PutAction.OnIdChanged(newValue))
                }
            }
        )
        TextField(
            value = state.userId,
            label = { Text("User Id") },
            placeholder = { Text("Enter User Id") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onAction(PutAction.OnUserIdChanged(newValue))
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
                onAction(PutAction.OnTitleChanged(newValue))
            }
        )
        TextField(
            value = state.body,
            label = { Text("Body") },
            placeholder = { Text("Enter Body") },
            onValueChange = { newValue ->
                onAction(PutAction.OnBodyChanged(newValue))
            }
        )
        Button(onClick = {
            onAction(PutAction.OnSubmit)
        }) {
            Text("Save")
        }
    }
}