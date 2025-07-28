package com.example.twit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class MainActivity : ComponentActivity() {

    private val previewsState = mutableStateListOf<LinkPreviewData>()
    private var isLoadingState = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = AppDatabase.getDatabase(this).scheduledTweetDao()

        fun loadData() {
            previewsState.clear()
            isLoadingState.value = true
            lifecycleScope.launch {
                try {
                    val items = SupabaseManager.client
                        .from("to_process")
                        .select()
                        .decodeList<ToProcessItem>()

                    for (item in items) {
                        item.url?.let { url ->
                            try {
                                val preview = LinkPreviewFetcher.fetchMetadata(url)
                                previewsState.add(preview)
                            } catch (e: Exception) {
                                Log.e("PreviewDebug", "Preview failed for $url", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseDebug", "Fetch error", e)
                } finally {
                    isLoadingState.value = false
                }
            }
        }

        fun scheduleTweets() {
            lifecycleScope.launch {
                val total = previewsState.size
                if (total == 0) return@launch

                val intervalMinutes = 180 / total
                val startTime = System.currentTimeMillis()

                previewsState.forEachIndexed { index, preview ->
                    val postTime = startTime + (index * intervalMinutes * 60 * 1000)
                    dao.insert(
                        ScheduledTweetEntity(
                            title = "\"${preview.title ?: "No Title"}\" -${preview.siteName ?: "Unknown"}",
                            publication = preview.siteName ?: "Unknown",
                            url = preview.url ?: "",
                            scheduledTime = postTime,
                            status = "Pending"
                        )
                    )
                    TweetScheduler.scheduleTweet(this@MainActivity, preview, postTime)
                }
                Log.d("ScheduleDebug", "All tweets scheduled!")
            }
        }

        loadData()

        setContent {
            TwitTheme {
                var isRefreshing by remember { mutableStateOf(false) }

                MainScreen(
                    previews = previewsState,
                    isRefreshing = isRefreshing,
                    isLoading = isLoadingState.value,
                    onDelete = { index ->
                        if (index in previewsState.indices) previewsState.removeAt(index)
                    },
                    onRefresh = {
                        isRefreshing = true
                        loadData()
                        isRefreshing = false
                    },
                    onSchedule = { scheduleTweets() },
                    onOpenSchedule = {
                        startActivity(Intent(this, ScheduleActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    previews: List<LinkPreviewData>,
    isRefreshing: Boolean,
    isLoading: Boolean,
    onDelete: (Int) -> Unit,
    onRefresh: () -> Unit,
    onSchedule: () -> Unit,
    onOpenSchedule: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twit") },
                actions = {
                    IconButton(onClick = { onOpenSchedule() }) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding)
        ) {
            if (previews.isEmpty() && isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Fetching previews...")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp)
                ) {
                    itemsIndexed(previews, key = { _, item -> item.url ?: "" }) { index, preview ->
                        SwipeableCard(preview = preview, onDismiss = { onDelete(index) })
                    }
                    if (!isLoading && previews.isNotEmpty()) {
                        item {
                            Button(
                                onClick = { onSchedule() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Schedule")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableCard(preview: LinkPreviewData, onDismiss: () -> Unit) {
    val dismissState = rememberDismissState()

    if (dismissState.isDismissed(DismissDirection.StartToEnd) ||
        dismissState.isDismissed(DismissDirection.EndToStart)) {
        LaunchedEffect(preview) {
            delay(150)
            onDismiss()
        }
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
                    preview.imageUrl?.let {
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
                    Text(preview.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    )
}
