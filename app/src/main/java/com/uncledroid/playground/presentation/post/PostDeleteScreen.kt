package com.uncledroid.playground.presentation.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PostDeleteScreen(state: DeleteState, onAction: (DeleteAction) -> Unit) {
    Column {
        Text("Post Delete screen")
        TextField(
            value = state.userId,
            label = { Text("User Id") },
            placeholder = { Text("Enter User Id") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onAction(DeleteAction.OnUserIdChanged(newValue))
                }
            }
        )
        Button(onClick = {
            onAction(DeleteAction.OnSubmit)
        }) {
            Text("Save")
        }
    }
}