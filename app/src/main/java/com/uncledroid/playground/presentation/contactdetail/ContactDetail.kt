package com.uncledroid.playground.presentation.contactdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ContactDetailScreen(state: DetailState, onAction: (DetailEvent) -> Unit) {
    Column {
        Text("Detail Screen \n${state.contact}")
        Button(onClick = { onAction(DetailEvent.Update) }) {
            Text("Update Name")
        }
    }
}