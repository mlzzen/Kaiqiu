package dev.mlzzen.kaiqiu.ui.screens.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.AdvProfile
import dev.mlzzen.kaiqiu.data.remote.GameRecord
import dev.mlzzen.kaiqiu.data.remote.GameRecordsResponse
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.HonorIconItem
import dev.mlzzen.kaiqiu.data.remote.ScoreHistory
import dev.mlzzen.kaiqiu.data.repository.Result
import dev.mlzzen.kaiqiu.data.repository.UserRepository
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

import dev.mlzzen.kaiqiu.ui.state.LocalUserState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserDetailScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    val userState = LocalUserState.current
    val currentUserUid = userState.userInfo.value?.uid
    val userRepository = remember { UserRepository() }
    var profile by remember { mutableStateOf<AdvProfile?>(null) }
    var gameRecords by remember { mutableStateOf<List<GameRecord>>(emptyList()) }
    var scoreHistory by remember { mutableStateOf<List<ScoreHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var nextPage by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        isLoading = true
        when (val result = userRepository.getAdvProfile(uid)) {
            is Result.Success -> profile = result.data
            is Result.Error -> profile = null
            is Result.Loading -> {}
        }
        // 加载积分历史
        try {
            val gson = Gson()
            val rawResponse = HttpClient.api.getUserScoresRaw(uid)
            val jsonString = rawResponse.string()
            android.util.Log.d("UserDetailScreen", "getUserScores raw response: $jsonString")
            // 尝试直接解析为数组
            try {
                val type = object : TypeToken<List<ScoreHistory>>() {}.type
                scoreHistory = gson.fromJson(jsonString, type)
            } catch (e: Exception) {
                // 如果直接解析失败，尝试通过 ApiResponse 解析
                try {
                    val apiResponseType = object : TypeToken<dev.mlzzen.kaiqiu.data.remote.ApiResponse<List<ScoreHistory>>>() {}.type
                    val apiResponse: dev.mlzzen.kaiqiu.data.remote.ApiResponse<List<ScoreHistory>> = gson.fromJson(jsonString, apiResponseType)
                    if (apiResponse.isSuccess) {
                        scoreHistory = apiResponse.data ?: emptyList()
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("UserDetailScreen", "Failed to parse score history: ${e2.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 加载近期战绩（优先使用 profile.games）
        val initialGames = profile?.games?.data ?: emptyList()
        gameRecords = initialGames
        nextPage = if (initialGames.isNotEmpty()) 2 else 1
        hasMore = true
        if (initialGames.isEmpty()) {
            loadMoreGames(
                uid = uid,
                page = nextPage,
                onStart = { isLoadingMore = true },
                onFinish = { loadedCount ->
                    isLoadingMore = false
                    if (loadedCount == 0) {
                        hasMore = false
                    } else {
                        nextPage += 1
                    }
                },
                onResult = { newItems ->
                    gameRecords = newItems
                }
            )
        }
        isLoading = false
    }

    LaunchedEffect(listState, hasMore, isLoadingMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val total = listState.layoutInfo.totalItemsCount
                val shouldLoadMore = lastVisibleIndex != null && lastVisibleIndex >= total - 2
                if (shouldLoadMore && hasMore && !isLoadingMore && gameRecords.isNotEmpty()) {
                    scope.launch {
                        loadMoreGames(
                            uid = uid,
                            page = nextPage,
                            onStart = { isLoadingMore = true },
                            onFinish = { loadedCount ->
                                isLoadingMore = false
                                if (loadedCount == 0) {
                                    hasMore = false
                                } else {
                                    nextPage += 1
                                }
                            },
                            onResult = { newItems ->
                                gameRecords = gameRecords + newItems
                            }
                        )
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户详情") },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState
            ) {
                // 用户信息头部
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarUrl = profile?.realpic
                            if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = if (!avatarUrl.startsWith("http")) "https:$avatarUrl" else avatarUrl,
                                    contentDescription = "头像",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(MaterialTheme.shapes.large),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val displayName = profile?.realname ?: profile?.nickname ?: profile?.username ?: "用户"
                                Text(displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "排名: ${profile?.rank ?: "-"} ${profile?.scope ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                // 年龄、性别、城市
                                val ageText = when (val age = profile?.age) {
                                    is Int -> "${age}岁"
                                    is Double -> "${age.toInt()}岁"
                                    is String -> if (age.isNotBlank()) "${age}岁" else ""
                                    else -> ""
                                }
                                val sexText = profile?.sex ?: ""
                                val cityText = profile?.city ?: ""
                                val infoText = listOf(ageText, sexText, cityText).filter { it.isNotBlank() }.joinToString(" | ")
                                if (infoText.isNotBlank()) {
                                    Text(
                                        infoText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 积分信息
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("积分信息", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("当前积分", profile?.score ?: "0")
                        StatItem("年度积分", profile?.maxScoreTheYear ?: "0")
                        StatItem("最高积分", profile?.maxscore ?: "0")
                    }
                }

                // 积分折线图
                if (scoreHistory.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("积分趋势", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        ScoreTrendChart(
                            scoreHistory = scoreHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(horizontal = 16.dp)
                        )
                    }
                }

                // 战绩统计
                profile?.let { p ->
                    val winNum = p.win ?: "0"
                    val loseNum = p.lose ?: "0"
                    val totalNum = p.total ?: "0"
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(winNum, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF39B54A))
                                    Text("胜", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(loseNum, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE6326E))
                                    Text("负", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(totalNum, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("总场", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // 装备信息
                profile?.let { p ->
                    val hasEquipment = !p.bg.isNullOrBlank() || !p.qiupai.isNullOrBlank() ||
                            !p.zhengshou.isNullOrBlank() || !p.fanshou.isNullOrBlank()
                    if (hasEquipment) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("装备信息", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (!p.bg.isNullOrBlank()) {
                                        DetailRow("专业背景", p.bg)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                    if (!p.qiupai.isNullOrBlank() || !p.qiupaitype.isNullOrBlank()) {
                                        DetailRow("底板型号", "${p.qiupai ?: ""} ${p.qiupaitype ?: ""}".trim())
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                    if (!p.zhengshou.isNullOrBlank() || !p.zhengshoutype.isNullOrBlank()) {
                                        DetailRow("正手套胶", "${p.zhengshou ?: ""} ${p.zhengshoutype ?: ""}".trim())
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                    if (!p.fanshou.isNullOrBlank() || !p.fanshoutype.isNullOrBlank()) {
                                        DetailRow("反手套胶", "${p.fanshou ?: ""} ${p.fanshoutype ?: ""}".trim())
                                    }
                                }
                            }
                        }
                    }
                }

                // 个人简介
                profile?.let { p ->
                    if (!p.description.isNullOrBlank()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("个人简介", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = p.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                // 击败分数最高前三名
                profile?.top3OfBeatUsernameScore?.takeIf { it?.isNotEmpty() == true }?.let { top3 ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("击败分数最高前三名", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                top3.filter { it.isNotBlank() }.forEach { item ->
                                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // 交手分数最高前三名
                profile?.topPlayerUsernameScore?.takeIf { it?.isNotEmpty() == true }?.let { top3 ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("交手分数最高前三名", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                top3.filter { it.isNotBlank() }.forEach { item ->
                                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // 击败男子最高前三名
                profile?.top3ManOfBeatUsernameScore?.takeIf { it?.isNotEmpty() == true }?.let { top3 ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("击败男子最高前三名", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                top3.filter { it.isNotBlank() }.forEach { item ->
                                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // 击败女子最高前三名
                profile?.top3WomanOfBeatUsernameScore?.takeIf { it?.isNotEmpty() == true }?.let { top3 ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("击败女子最高前三名", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                top3.filter { it.isNotBlank() }.forEach { item ->
                                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // 经常交手
                profile?.oftenPlayer?.takeIf { it.isNotBlank() }?.let { players ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("经常交手", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                players.split(",").filter { it.isNotBlank() }.forEach { player ->
                                    Text("• $player", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // 曾参加比赛城市
                profile?.allCities?.takeIf { it?.isNotEmpty() == true }?.let { cities ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("曾参加比赛城市", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    cities.forEach { city ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(city, style = MaterialTheme.typography.bodySmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 近期荣耀
                val honorsList = profile?.honors?.let { honorData ->
                    when (honorData) {
                        is List<*> -> honorData.filterIsInstance<HonorIconItem>()
                        else -> emptyList<HonorIconItem>()
                    }
                }?.takeIf { it.isNotEmpty() }
                if (honorsList != null) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("近期荣耀", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                honorsList.forEach { honor ->
                                    HonorRow(honor = honor)
                                }
                            }
                        }
                    }
                }

                // 近期战绩
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("近期战绩", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val displayRecords = gameRecords
                if (displayRecords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("暂无比赛记录", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    item {
                        GameRecordHeaderRow()
                    }
                    itemsIndexed(displayRecords) { index, record ->
                        GameRecordItem(
                            index = index + 1,
                            record = record,
                            currentUid = uid,
                            onNavigateToUser = onNavigateToUser,
                            onNavigateToEvent = onNavigateToEvent
                        )
                    }
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.size(64.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScoreTrendChart(scoreHistory: List<ScoreHistory>, modifier: Modifier = Modifier) {
    val primaryColor = Color(0xFF39B54A)
    val axisColor = Color.Gray
    val textMeasurer = rememberTextMeasurer()

    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (scoreHistory.isEmpty()) {
                Text(
                    text = "暂无积分数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                // 解析真实数据
                val scores = scoreHistory.mapNotNull { it.postScore?.toIntOrNull() }
                val labels = scoreHistory.mapNotNull { it.dateline?.take(10) } // 取日期前10位

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 50f
                    val paddingRight = 10f
                    val paddingTop = 25f
                    val paddingBottom = 35f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val minScore = (scores.minOrNull() ?: 0) - 30f
                    val maxScore = (scores.maxOrNull() ?: 0) + 30f
                    val scoreRange = (maxScore - minScore).coerceAtLeast(100f)

                    val stepX = if (scores.size > 1) chartWidth / (scores.size - 1) else 0f

                    // 绘制Y轴
                    drawLine(
                        color = axisColor,
                        start = Offset(paddingLeft, paddingTop),
                        end = Offset(paddingLeft, height - paddingBottom),
                        strokeWidth = 2f
                    )

                    // 绘制X轴
                    drawLine(
                        color = axisColor,
                        start = Offset(paddingLeft, height - paddingBottom),
                        end = Offset(width - paddingRight, height - paddingBottom),
                        strokeWidth = 2f
                    )

                    // 绘制Y轴标签和网格线
                    val ySteps = 4
                    for (i in 0..ySteps) {
                        val yValue = minScore + (scoreRange * i / ySteps)
                        val y = height - paddingBottom - (chartHeight * i / ySteps)

                        // Y轴标签
                        val textResult = textMeasurer.measure(
                            text = yValue.toInt().toString(),
                            style = TextStyle(fontSize = 9.sp, color = axisColor)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = yValue.toInt().toString(),
                            topLeft = Offset(paddingLeft - textResult.size.width - 4f, y - textResult.size.height / 2),
                            style = TextStyle(fontSize = 9.sp, color = axisColor)
                        )

                        // 水平网格线
                        if (i > 0 && i < ySteps) {
                            drawLine(
                                color = axisColor.copy(alpha = 0.2f),
                                start = Offset(paddingLeft, y),
                                end = Offset(width - paddingRight, y),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // 绘制X轴标签 (只显示部分避免重叠)
                    if (scores.isNotEmpty()) {
                        val labelStep = ((scores.size - 1) / 4).coerceAtLeast(1)
                        scores.forEachIndexed { index, _ ->
                            if (index % labelStep == 0 || index == scores.size - 1) {
                                val x = paddingLeft + index * stepX
                                val labelText = labels.getOrElse(index) { "${index + 1}" }
                                val textResult = textMeasurer.measure(
                                    text = labelText,
                                    style = TextStyle(fontSize = 9.sp, color = axisColor)
                                )
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    topLeft = Offset(x - textResult.size.width / 2, height - paddingBottom + 4f),
                                    style = TextStyle(fontSize = 9.sp, color = axisColor)
                                )
                            }
                        }
                    }

                    // 绘制折线
                    if (scores.size > 1) {
                        val path = Path()
                        scores.forEachIndexed { index, score ->
                            val x = paddingLeft + index * stepX
                            val y = height - paddingBottom - ((score - minScore) / scoreRange * chartHeight)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // 绘制数据点
                    scores.forEachIndexed { index, score ->
                        val x = paddingLeft + index * stepX
                        val y = height - paddingBottom - ((score - minScore) / scoreRange * chartHeight)

                        // 绘制数据点
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 3f,
                            center = Offset(x, y)
                        )
                    }

                    // 只绘制最高点和最低点的数值
                    if (scores.isNotEmpty()) {
                        val maxScoreValue = scores.maxOrNull() ?: return@Canvas
                        val minScoreValue = scores.minOrNull() ?: return@Canvas
                        val maxIndex = scores.indexOf(maxScoreValue)
                        val minIndex = scores.indexOf(minScoreValue)

                        // 最高点
                        val maxX = paddingLeft + maxIndex * stepX
                        val maxY = height - paddingBottom - ((maxScoreValue - minScore) / scoreRange * chartHeight)
                        // 绘制最高点数据点（稍大）
                        drawCircle(
                            color = Color(0xFFFF6B6B),
                            radius = 7f,
                            center = Offset(maxX, maxY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(maxX, maxY)
                        )
                        drawCircle(
                            color = Color(0xFFFF6B6B),
                            radius = 3f,
                            center = Offset(maxX, maxY)
                        )
                        // 最高点数值
                        val maxText = maxScoreValue.toString()
                        val maxTextResult = textMeasurer.measure(
                            text = maxText,
                            style = TextStyle(fontSize = 11.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = maxText,
                            topLeft = Offset(maxX - maxTextResult.size.width / 2, maxY - maxTextResult.size.height - 14f),
                            style = TextStyle(fontSize = 11.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                        )

                        // 最低点
                        val minX = paddingLeft + minIndex * stepX
                        val minY = height - paddingBottom - ((minScoreValue - minScore) / scoreRange * chartHeight)
                        // 绘制最低点数据点（稍大）
                        drawCircle(
                            color = Color(0xFF4DABF7),
                            radius = 7f,
                            center = Offset(minX, minY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(minX, minY)
                        )
                        drawCircle(
                            color = Color(0xFF4DABF7),
                            radius = 3f,
                            center = Offset(minX, minY)
                        )
                        // 最低点数值
                        val minText = minScoreValue.toString()
                        val minTextResult = textMeasurer.measure(
                            text = minText,
                            style = TextStyle(fontSize = 11.sp, color = Color(0xFF4DABF7), fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = minText,
                            topLeft = Offset(minX - minTextResult.size.width / 2, minY - minTextResult.size.height - 14f),
                            style = TextStyle(fontSize = 11.sp, color = Color(0xFF4DABF7), fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRecordItem(
    index: Int,
    record: GameRecord,
    currentUid: String,
    onNavigateToUser: (String) -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    val name1 = record.username1?.ifBlank { "未知" } ?: "未知"
    val name1b = record.username11?.takeIf { it.isNotBlank() }
    val name2 = record.username2?.ifBlank { "未知" } ?: "未知"
    val name2b = record.username22?.takeIf { it.isNotBlank() }
    val dateText = record.matchDate?.take(10) ?: "-"
    val eventId = record.eventid
    val canOpenEvent = !eventId.isNullOrBlank() && eventId != "0"
    val changeValue = record.score1?.toIntOrNull()
    val changeColor = when {
        changeValue == null -> TextSecondary
        changeValue > 0 -> Color(0xFF39B54A)
        changeValue < 0 -> Color(0xFFE6326E)
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(0.5f)
            )

            NameColumn(
                primaryName = name1,
                secondaryName = name1b,
                onPrimaryClick = {
                    val targetUid = record.uid1?.ifBlank { null } ?: currentUid
                    if (targetUid.isNotBlank()) onNavigateToUser(targetUid)
                },
                onSecondaryClick = null,
                modifier = Modifier.weight(2.3f)
            )

            NameColumn(
                primaryName = name2,
                secondaryName = name2b,
                onPrimaryClick = {
                    val targetUid = record.uid2?.ifBlank { null }
                    if (!targetUid.isNullOrBlank()) onNavigateToUser(targetUid)
                },
                onSecondaryClick = null,
                modifier = Modifier.weight(2.3f)
            )

            Text(
                text = record.scoreText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (record.isGroupMatch) Color(0xFFF89703) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(0.6f)
                    .clickable(enabled = canOpenEvent) {
                        onNavigateToEvent(eventId!!)
                    },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = record.scoreChangeText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = changeColor,
                modifier = Modifier
                    .weight(0.6f)
                    .clickable(enabled = canOpenEvent) {
                        onNavigateToEvent(eventId!!)
                    },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .weight(2.3f)
                    .clickable(enabled = canOpenEvent) {
                        onNavigateToEvent(eventId!!)
                    },
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
private fun NameColumn(
    primaryName: String,
    secondaryName: String?,
    onPrimaryClick: (() -> Unit)?,
    onSecondaryClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = primaryName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .clickable(enabled = onPrimaryClick != null) {
                    onPrimaryClick?.invoke()
                }
        )
        if (!secondaryName.isNullOrBlank()) {
            Text(
                text = secondaryName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .clickable(enabled = onSecondaryClick != null) {
                        onSecondaryClick?.invoke()
                    }
            )
        }
    }
}

@Composable
private fun GameRecordHeaderRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell(text = "序号", weight = 0.5f, alignEnd = false)
            HeaderCell(text = "姓名", weight = 2.3f, alignEnd = false)
            HeaderCell(text = "姓名", weight = 2.3f, alignEnd = false)
            HeaderCell(text = "比分", weight = 0.6f, alignEnd = false)
            HeaderCell(text = "变化", weight = 0.6f, alignEnd = false)
            HeaderCell(text = "日期", weight = 2.3f, alignEnd = true)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float, alignEnd: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        modifier = Modifier.weight(weight),
        textAlign = if (alignEnd) androidx.compose.ui.text.style.TextAlign.End
        else androidx.compose.ui.text.style.TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip
    )
}

@Composable
private fun HonorRow(honor: HonorIconItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = honor.honor,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(honor.subject ?: "", style = MaterialTheme.typography.bodyMedium)
    }
}

private suspend fun loadMoreGames(
    uid: String,
    page: Int,
    onStart: () -> Unit,
    onFinish: (loadedCount: Int) -> Unit,
    onResult: (List<GameRecord>) -> Unit
) {
    onStart()
    var loaded = 0
    try {
        val response = HttpClient.api.getPageGamesByUid(uid, page)
        if (response.isSuccess) {
            val list = response.data?.data ?: emptyList()
            onResult(list)
            loaded = list.size
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        onFinish(loaded)
    }
}
