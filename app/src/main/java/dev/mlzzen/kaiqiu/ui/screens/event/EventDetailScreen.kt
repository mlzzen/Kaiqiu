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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import dev.mlzzen.kaiqiu.data.remote.EventItemInfo
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventid: String,
    onNavigateBack: () -> Unit,
    onNavigateToMembers: (matchId: String, itemId: String) -> Unit,
    onNavigateToScore: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var eventDetail by remember { mutableStateOf<EventDetail?>(null) }
    var items by remember { mutableStateOf<List<EventItemInfo>>(emptyList()) }
    var activeItemId by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val tabList = listOf("详情", "赛程", "成绩", "积分")
    val crtItem: EventItemInfo? = items.find { it.id == activeItemId }

    fun loadData() {
        isLoading = true
        error = null
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    HttpClient.api.getEventDetaiByIdAndLocation(
                        mapOf("id" to eventid, "lng" to "116.4074", "lat" to "39.9042")
                    )
                }
                if (response.isSuccess && response.data != null) {
                    eventDetail = response.data.detail
                    items = response.data.items
                    if (items.isNotEmpty() && activeItemId == null) {
                        activeItemId = items[0].id
                    }
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

    LaunchedEffect(eventid) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("赛事详情") },
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
                        Text(error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadData() }) {
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
                    // 顶部封面图
                    item {
                        AsyncImage(
                            model = eventDetail?.poster?.let { if (!it.startsWith("http")) "https:$it" else it },
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 子项目选择
                    if (items.size > 1) {
                        item {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(items) { item ->
                                    val itemId = item.id
                                    val isSelected = activeItemId == itemId
                                    Surface(
                                        modifier = Modifier.clickable { activeItemId = itemId },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) Color(0xFF39B54A) else Color.Transparent,
                                        border = ButtonDefaults.outlinedButtonBorder
                                    ) {
                                        Text(
                                            text = item.name ?: "",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            color = if (isSelected) Color.White else Color(0xFF39B54A),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tab 栏
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    }

                    item { HorizontalDivider() }

                    // 详情内容
                    when (activeTab) {
                        0 -> {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        activeItemId?.let { itemId ->
                                            onNavigateToMembers(eventDetail?.eventid ?: "", itemId)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("参赛名单")
                                }
                            }
                            item { MainDetailTab(eventDetail = eventDetail) }
                        }
                        1 -> {
                            item { ScheduleTab(onNavigateToScore = { onNavigateToScore(activeItemId ?: "") }) }
                        }
                        2 -> {
                            item { ResultTab() }
                        }
                        3 -> {
                            item { ScoreChangeTab() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainDetailTab(eventDetail: EventDetail?) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // 赛事标题
        Text(
            text = eventDetail?.title ?: "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider()

        // 发起方
        DetailRow(label = "发起方", value = eventDetail?.tagid ?: eventDetail?.username ?: "-")

        HorizontalDivider()

        // 比赛时间
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("比赛时间", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(eventDetail?.starttime ?: "-", style = MaterialTheme.typography.bodyMedium)
            eventDetail?.endtime?.takeIf { it.isNotEmpty() }?.let {
                Text("至 $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        HorizontalDivider()

        // 报名时间
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("报名时间", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            eventDetail?.startenrolltime?.takeIf { it.isNotEmpty() }?.let { start ->
                Text("$start 开始", style = MaterialTheme.typography.bodyMedium)
            }
            eventDetail?.deadline?.takeIf { it.isNotEmpty() }?.let { deadline ->
                Text("截止 $deadline", style = MaterialTheme.typography.bodyMedium)
            }
        }

        HorizontalDivider()

        // 比赛地点
        DetailRow(
            label = "比赛地点",
            value = eventDetail?.location ?: eventDetail?.arenaName ?: "-"
        )

        HorizontalDivider()

        // 联系人
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("联系人", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
            Text(eventDetail?.contact ?: "-", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (!eventDetail?.mobile.isNullOrEmpty()) {
                Text(
                    text = "联系Ta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF39B54A),
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${eventDetail?.mobile}")
                            }
                            context.startActivity(intent)
                        }
                        .padding(8.dp)
                )
            }
        }

        HorizontalDivider()

        // 微信
        DetailRow(label = "微信", value = eventDetail?.weixin ?: "-")

        HorizontalDivider()

        // 参赛人数
        DetailRow(label = "参赛人数", value = "${eventDetail?.membernum ?: "0"}人")

        HorizontalDivider()

        // 浏览次数
        DetailRow(label = "浏览次数", value = "${eventDetail?.viewnum ?: "0"}")

        HorizontalDivider()

        // 赛事说明
        if (!eventDetail?.note.isNullOrEmpty()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("赛事说明", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = eventDetail?.note ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HorizontalDivider()
        }

        // 比赛信息（HTML内容）
        if (!eventDetail?.detail.isNullOrEmpty()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("比赛信息", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                // 解码URL编码的HTML内容
                val decodedDetail = try {
                    java.net.URLDecoder.decode(eventDetail?.detail ?: "", "UTF-8")
                } catch (e: Exception) {
                    eventDetail?.detail ?: ""
                }
                Text(
                    text = decodedDetail,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScheduleTab(onNavigateToScore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
private fun ResultTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
private fun ScoreChangeTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
