package dev.mlzzen.kaiqiu.ui.screens.event

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.EventDetail
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventid: String,
    onNavigateBack: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToScore: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var eventDetail by remember { mutableStateOf<EventDetail?>(null) }
    var subEventList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var ifTT by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var activeItemId by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val tabList = listOf("详情", "赛程", "成绩", "积分")

    val crtItem: Map<String, String> = subEventList.find { it["id"] == activeItemId } ?: emptyMap()
    val crtIfTT = activeItemId?.let { ifTT[it] } ?: 0

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    HttpClient.api.getEventDetaiByIdAndLocation(
                        mapOf("id" to eventid, "lng" to "116.4074", "lat" to "39.9042")
                    )
                }
                if (response.isSuccess) {
                    eventDetail = response.data
                    if (subEventList.isNotEmpty() && activeItemId == null) {
                        activeItemId = subEventList.getOrNull(0)?.get("id")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(eventid) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(eventDetail?.title ?: "赛事详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 顶部海报
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = eventDetail?.img?.let { if (!it.startsWith("http")) "https:$it" else it },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Tab 栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabList.forEachIndexed { index, tab ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = index }
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (activeTab == index) Color(0xFF39B54A) else Color.Gray,
                                fontWeight = if (activeTab == index) FontWeight.Medium else FontWeight.Normal
                            )
                            if (activeTab == index) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(40.dp)
                                        .height(2.dp)
                                        .background(Color(0xFF39B54A))
                                )
                            }
                        }
                    }
                }

                // 子项目选择
                if (subEventList.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subEventList) { item ->
                            val itemId = item["id"]
                            val isSelected = activeItemId == itemId
                            Surface(
                                modifier = Modifier.clickable { activeItemId = itemId },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0xFF39B54A) else Color.Transparent,
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    text = item["name"]?.toString() ?: "",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.White else Color(0xFF39B54A),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 内容区域
                when (activeTab) {
                    0 -> MainDetailTab(
                        eventDetail = eventDetail,
                        crtItem = crtItem,
                        onNavigateToMembers = onNavigateToMembers
                    )
                    1 -> ScheduleTab(
                        crtItem = crtItem,
                        crtIfTT = crtIfTT,
                        onNavigateToScore = { onNavigateToScore(activeItemId ?: "") }
                    )
                    2 -> ResultTab(
                        crtItem = crtItem,
                        crtIfTT = crtIfTT
                    )
                    3 -> ScoreChangeTab(
                        eventid = eventid,
                        activeItemId = activeItemId
                    )
                }
            }
        }
    }
}

@Composable
private fun MainDetailTab(
    eventDetail: EventDetail?,
    crtItem: Map<String, String>,
    onNavigateToMembers: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("联系人", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
                Text(eventDetail?.status ?: "-", style = MaterialTheme.typography.bodyMedium)
                if (eventDetail?.status?.isNotEmpty() == true) {
                    Text(
                        text = "联系Ta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF39B54A),
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${eventDetail.status}") }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("比赛时间", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
                Column {
                    Text(eventDetail?.startTime ?: "", style = MaterialTheme.typography.bodyMedium)
                    eventDetail?.endTime?.takeIf { it.isNotEmpty() }?.let {
                        Text("至 $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("比赛地点", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
                Text(eventDetail?.arena ?: "-", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
        item {
            Box(modifier = Modifier.height(16.dp))
        }
        item {
            OutlinedButton(
                onClick = onNavigateToMembers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("参赛名单")
            }
        }
    }
}

@Composable
private fun ScheduleTab(
    crtItem: Map<String, String>,
    crtIfTT: Int,
    onNavigateToScore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Text("暂无赛程信息", color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToScore) {
                Text("录入成绩")
            }
        }
    }
}

@Composable
private fun ResultTab(
    crtItem: Map<String, String>,
    crtIfTT: Int
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Text("暂无成绩信息", color = TextSecondary)
        }
    }
}

@Composable
private fun ScoreChangeTab(
    eventid: String,
    activeItemId: String?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Text("暂无积分变化信息", color = TextSecondary)
        }
    }
}
