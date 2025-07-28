package com.example.twit.ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.twit.data.ScheduledTweetEntity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableScheduledCard(tweet: ScheduledTweetEntity, onDelete: () -> Unit) {
    val dismissState = rememberDismissState(confirmStateChange = {
        if (it == DismissValue.DismissedToStart) {
            onDelete()
            true
        } else false
    })

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            Surface(color = Color.Red) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        },
        dismissContent = {
            Card {
                Text("\"${tweet.title}\" - ${tweet.publication}")
                Text(tweet.status)
            }
        }
    )
}
