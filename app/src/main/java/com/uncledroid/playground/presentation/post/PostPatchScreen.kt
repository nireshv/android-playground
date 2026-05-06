package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PostPatchScreen(state: PatchState, onAction: (PatchAction) -> Unit) {
    Column {
        Text("Post Put screen")
        TextField(
            value = state.userId,
            label = { Text("User Id") },
            placeholder = { Text("Enter User Id") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onAction(PatchAction.OnUserIdChanged(newValue))
                }
            }
        )
        TextField(
            value = state.body,
            label = { Text("Body") },
            placeholder = { Text("Enter Body") },
            onValueChange = { newValue ->
                onAction(PatchAction.OnBodyChanged(newValue))
            }
        )
        Button(onClick = {
            onAction(PatchAction.OnSubmit)
        }) {
            Text("Save")
        }
    }
}