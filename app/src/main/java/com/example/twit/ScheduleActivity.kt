package com.example.twit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.twit.data.AppDatabase
import com.example.twit.data.ScheduledTweetEntity
import com.example.twit.ui.theme.TwitTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

class ScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = AppDatabase.getDatabase(this).scheduledTweetDao()
        val tweetsState = mutableStateListOf<ScheduledTweetEntity>()

        lifecycleScope.launch {
            dao.getAllTweets().collectLatest { list ->
                tweetsState.clear()
                tweetsState.addAll(list)
            }
        }

        setContent {
            TwitTheme {
                ScheduleScreen(
                    tweets = tweetsState,
                    onDelete = { tweet -> lifecycleScope.launch { dao.delete(tweet) } },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    tweets: List<ScheduledTweetEntity>,
    onDelete: (ScheduledTweetEntity) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheduled Tweets") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (tweets.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No scheduled tweets")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                items(tweets, key = { it.id }) { tweet ->
                    SwipeableScheduledCard(tweet, onDelete)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableScheduledCard(tweet: ScheduledTweetEntity, onDelete: (ScheduledTweetEntity) -> Unit) {
    val dismissState = rememberDismissState()

    if (dismissState.isDismissed(DismissDirection.StartToEnd) ||
        dismissState.isDismissed(DismissDirection.EndToStart)) {
        LaunchedEffect(tweet) { onDelete(tweet) }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = { Box(Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(MaterialTheme.shapes.medium),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(12.dp)) {
                    tweet.url.let {
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tweet.title, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    )
}




https://github.com/NewZBoi/webappbottwit