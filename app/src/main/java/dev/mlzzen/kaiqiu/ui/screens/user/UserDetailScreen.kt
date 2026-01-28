package dev.mlzzen.kaiqiu.ui.screens.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.AdvProfile
import dev.mlzzen.kaiqiu.data.remote.GameRecord
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.repository.Result
import dev.mlzzen.kaiqiu.data.repository.UserRepository
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    var profile by remember { mutableStateOf<AdvProfile?>(null) }
    var gameRecords by remember { mutableStateOf<List<GameRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        isLoading = true
        when (val result = userRepository.getAdvProfile(uid)) {
            is Result.Success -> profile = result.data
            is Result.Error -> profile = null
            is Result.Loading -> {}
        }
        // 加载近期战绩
        try {
            val response = HttpClient.api.getPageGamesByUid(uid, 1)
            if (response.isSuccess) {
                gameRecords = response.data ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
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
                    .padding(paddingValues)
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
                            }
                        }
                    }
                }

                // 操作按钮
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(icon = Icons.Default.Star, label = "参赛记录", onClick = onNavigateToEvents)
                        ActionButton(icon = Icons.Default.Favorite, label = "关注", onClick = { })
                        ActionButton(icon = Icons.Default.Person, label = "粉丝", onClick = { })
                    }
                }

                // 积分折线图
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("积分趋势", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreTrendChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(horizontal = 16.dp)
                    )
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

                // 专业背景和底板型号
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
                            DetailRow("专业背景", profile?.description ?: "暂未填写")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("底板型号", "暂未填写")
                        }
                    }
                }

                // 击败分数最高前三名
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("击败对手 TOP3", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("暂无击败记录", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }

                // 交手分数最高前三名
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("交手记录 TOP3", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("暂无交手记录", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }

                // 近期战绩
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("近期战绩", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (gameRecords.isEmpty()) {
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
                    items(gameRecords.take(10)) { record ->
                        GameRecordItem(record = record)
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScoreTrendChart(modifier: Modifier = Modifier) {
    val primaryColor = Color(0xFF39B54A)
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 模拟积分趋势折线图
            val scores = listOf(1200, 1250, 1230, 1300, 1280, 1350, 1400)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (scores.size - 1)
                val minScore = scores.minOrNull() ?: 0
                val maxScore = scores.maxOrNull() ?: 0
                val scoreRange = (maxScore - minScore).coerceAtLeast(100)

                val path = Path()
                scores.forEachIndexed { index, score ->
                    val x = index * stepX
                    val y = height - ((score - minScore).toFloat() / scoreRange * height * 0.8f + height * 0.1f)
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    // 绘制数据点
                    drawCircle(
                        color = primaryColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

@Composable
private fun GameRecordItem(record: GameRecord) {
    val isWin = record.result?.contains("胜") == true
    val resultColor = if (isWin) Color(0xFF39B54A) else Color(0xFFE6326E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.result ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = resultColor,
                modifier = Modifier.width(50.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.eventTitle ?: "未知赛事",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = record.createTime ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "${record.myScore ?: 0} : ${record.opponentScore ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
