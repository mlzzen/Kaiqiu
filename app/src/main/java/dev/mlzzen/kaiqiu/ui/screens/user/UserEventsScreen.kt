package dev.mlzzen.kaiqiu.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.EventHistory
import dev.mlzzen.kaiqiu.data.repository.Result
import dev.mlzzen.kaiqiu.data.repository.UserRepository
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEventsScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    val userRepository = remember { UserRepository() }
    var events by remember { mutableStateOf<List<EventHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    fun loadData(reset: Boolean = false) {
        android.util.Log.d("UserEvents", "=== loadData reset=$reset ===")
        android.util.Log.d("UserEvents", "uid=$uid, page=$page, isLoadingMore=$isLoadingMore, hasMore=$hasMore")

        if (reset) {
            page = 1
            events = emptyList()
            hasMore = true
        }
        if (isLoadingMore || !hasMore) {
            android.util.Log.d("UserEvents", "skip loadData: isLoadingMore=$isLoadingMore, hasMore=$hasMore")
            return
        }

        isLoadingMore = true
        scope.launch {
            android.util.Log.d("UserEvents", "calling getMatchListHisByPage($page)")
            when (val result = userRepository.getMatchListHisByPage(page)) {
                is Result.Success -> {
                    val newEvents = result.data
                    android.util.Log.d("UserEvents", "getMatchListHisByPage success: ${newEvents.size} events")
                    android.util.Log.d("UserEvents", "events: $newEvents")
                    if (newEvents.isEmpty()) {
                        hasMore = false
                    } else {
                        events = if (reset) newEvents else events + newEvents
                        page++
                    }
                }
                is Result.Error -> {
                    android.util.Log.d("UserEvents", "getMatchListHisByPage error: ${result.exception}")
                    hasMore = false
                }
                is Result.Loading -> {}
            }
            isLoading = false
            isLoadingMore = false
            android.util.Log.d("UserEvents", "loadData complete: events.size=${events.size}, hasMore=$hasMore")
        }
    }

    LaunchedEffect(uid) {
        android.util.Log.d("UserEvents", "LaunchedEffect triggered, uid=$uid")
        loadData(true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("参赛记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            events.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无参赛记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(events) { event ->
                        EventHistoryItem(
                            event = event,
                            onClick = { onNavigateToEvent(event.eventid) }
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (!hasMore && events.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "没有更多了",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EventHistoryItem(
    event: EventHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 比赛海报
            if (!event.poster.isNullOrEmpty()) {
                AsyncImage(
                    model = if (!event.poster.startsWith("http")) "https:${event.poster}" else event.poster,
                    contentDescription = event.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title ?: "未知比赛",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.province ?: ""} ${event.city ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${event.viewnum ?: "0"}人浏览",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${event.membernum ?: "0"}人参加",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
