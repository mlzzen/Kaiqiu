package dev.mlzzen.kaiqiu.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.datastore.AppDataStore
import dev.mlzzen.kaiqiu.data.datastore.CityData
import dev.mlzzen.kaiqiu.data.remote.EventItem
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { AppDataStore(context) }

    var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf(CityData("1", "北京市")) }

    // 加载城市选择
    LaunchedEffect(Unit) {
        dataStore.selectCityFlow.collect { city ->
            selectedCity = city
        }
    }

    // 加载比赛列表
    fun loadEvents() {
        scope.launch {
            isLoading = true
            error = null
            try {
                // 使用固定位置作为示例，实际应该获取用户位置
                val response = HttpClient.api.getMatchListByPage(
                    mapOf(
                        "city" to selectedCity.name,
                        "lat" to "39.9042",
                        "lng" to "116.4074",
                        "page" to "1",
                        "sort" to "4"
                    )
                )
                if (response.isSuccess) {
                    events = response.data?.data ?: emptyList()
                } else {
                    error = response.msg
                }
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCity) {
        loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开球网") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 城市选择栏
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { /* TODO: 跳转到城市选择 */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF77B980))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedCity.name)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 快捷入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionChip(
                    icon = Icons.Default.Star,
                    label = "赛事",
                    onClick = { onNavigateToSearch() }
                )
                QuickActionChip(
                    icon = Icons.Default.Star,
                    label = "排行",
                    onClick = { onNavigateToSearch() }
                )
                QuickActionChip(
                    icon = Icons.Default.Star,
                    label = "球馆",
                    onClick = { onNavigateToSearch() }
                )
                QuickActionChip(
                    icon = Icons.Default.Star,
                    label = "比赛",
                    onClick = { onNavigateToSearch() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "近期比赛",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 比赛列表
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadEvents() }) {
                                Text("重试")
                            }
                        }
                    }
                }
                events.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("暂无比赛信息", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(events) { event ->
                            EventCard(
                                event = event,
                                onClick = { onNavigateToEvent(event.eventid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EventItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 赛事海报
            Box {
                AsyncImage(
                    model = event.poster?.let { encodeUrl(it) },
                    contentDescription = event.title,
                    modifier = Modifier
                        .width(100.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentScale = ContentScale.Crop
                )
                // 城市标签
                event.city?.let { city ->
                    Text(
                        text = city,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFF77B980), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 赛事信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.startTime?.take(10) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = " | ${event.status ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE49B37)
                    )
                }

                Text(
                    text = "比赛地点: ${event.arenaName ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${event.viewnum ?: "0"}人浏览",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${event.membernum ?: "0"}人参加",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF77B980),
                        modifier = Modifier
                            .background(
                                Color(0x3377B980),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun encodeUrl(url: String): String {
    return try {
        if (!url.startsWith("http")) "https:${url}" else url
    } catch (e: Exception) {
        url
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
