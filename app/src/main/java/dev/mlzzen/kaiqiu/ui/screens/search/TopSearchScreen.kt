package dev.mlzzen.kaiqiu.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.TopItem
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRank: () -> Unit,
    onNavigateToTop100: (tid: String, name: String) -> Unit
) {
    val userState: UserState = LocalUserState.current
    val cityName by userState.selectCity.collectAsState()
    val scope = rememberCoroutineScope()

    var topList by remember { mutableStateOf<List<TopItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadTopList() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = HttpClient.api.getTopView(mapOf("city" to cityName.name))
                if (response.isSuccess) {
                    topList = (response.data?.list ?: emptyList())
                        .filter { item -> item.tid.toIntOrNull() !in listOf(11, 10, 6) }
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

    LaunchedEffect(cityName) {
        loadTopList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排行榜") },
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
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadTopList() }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 开球网积分天梯 - Rank #1
                    item {
                        TopSearchItem(
                            rank = 1,
                            title = "开球网积分天梯",
                            subtitle = null,
                            viewCount = null,
                            isHighlighted = true,
                            onClick = onNavigateToRank
                        )
                    }

                    itemsIndexed(topList) { index, item ->
                        TopSearchItem(
                            rank = index + 2,
                            title = item.name ?: "",
                            subtitle = "top100",
                            viewCount = item.viewnum,
                            isHighlighted = false,
                            onClick = { onNavigateToTop100(item.tid, item.name ?: "") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSearchItem(
    rank: Int,
    title: String,
    subtitle: String?,
    viewCount: String?,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val rankColor = when {
        rank == 1 -> MaterialTheme.colorScheme.error
        rank == 2 -> Color(0xFFE49B37)
        rank == 3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted && rank == 1) rankColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isHighlighted && rank == 1) Color(0xFF77B980) else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // View count
        if (viewCount != null) {
            Text(
                text = "${viewCount}浏览",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}
