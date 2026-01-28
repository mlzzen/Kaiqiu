package dev.mlzzen.kaiqiu.ui.screens.match

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.TtGameData
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScoreEntryScreen(
    eventid: String,
    itemid: String,
    onNavigateBack: () -> Unit,
    onNavigateToGroupScore: () -> Unit
) {
    val userState: UserState = LocalUserState.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rounds by remember { mutableStateOf<List<dev.mlzzen.kaiqiu.data.remote.RoundData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Popup state
    var showPopup by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<TtGameData?>(null) }
    var selectedScore by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scoreOptions = listOf(
        "0:0", "2:0", "2:1", "1:2", "0:2",
        "3:0", "3:1", "3:2", "2:3", "1:3", "0:3",
        "4:0", "4:1", "4:2", "4:3", "3:4", "2:4", "1:4", "0:4"
    )

    fun loadData() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val response = HttpClient.api.getArrangeKnockout(
                    mapOf("eventid" to eventid, "itemid" to itemid)
                )
                if (response.isSuccess) {
                    rounds = response.data?.rounds ?: emptyList()
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

    fun submitScore() {
        val game = selectedGame ?: return
        if (selectedScore.isEmpty()) {
            Toast.makeText(context, "请设置比分", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedScore == "wo:wo") {
            Toast.makeText(context, "淘汰赛不支持同时弃权", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isSubmitting = true
            try {
                val response = HttpClient.api.updateTtScore(
                    mapOf(
                        "groupid" to "-1",
                        "uid1" to (game.uid1 ?: ""),
                        "uid2" to (game.uid2 ?: ""),
                        "score" to selectedScore,
                        "eventid" to eventid,
                        "itemid" to itemid,
                        "gameid" to (game.gameid ?: "")
                    )
                )
                if (response.isSuccess) {
                    showPopup = false
                    selectedGame = null
                    selectedScore = ""
                    loadData()
                } else {
                    Toast.makeText(context, response.msg ?: "提交失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "提交失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSubmitting = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记分") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToGroupScore() }) {
                        Text("小组赛", style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
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
                    rounds.forEach { round ->
                        item {
                            RoundSection(
                                roundName = round.roundname,
                                games = round.games,
                                onGameClick = { game ->
                                    selectedGame = game
                                    selectedScore = ""
                                    showPopup = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Score popup
    if (showPopup && selectedGame != null) {
        ScorePopup(
            game = selectedGame!!,
            selectedScore = selectedScore,
            scoreOptions = scoreOptions,
            isSubmitting = isSubmitting,
            onScoreSelect = { score ->
                selectedScore = score
            },
            onSubmit = { submitScore() },
            onDismiss = {
                showPopup = false
                selectedGame = null
                selectedScore = ""
            }
        )
    }
}

@Composable
private fun RoundSection(
    roundName: String,
    games: List<TtGameData>,
    onGameClick: (TtGameData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Round header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = roundName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF89703),
                modifier = Modifier
                    .border(1.dp, Color(0xFFF89703), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Table
        ScoreTable(
            games = games,
            onGameClick = onGameClick
        )
    }
}

@Composable
private fun ScoreTable(
    games: List<TtGameData>,
    onGameClick: (TtGameData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "序号",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )
            Text(
                text = "选手1",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "选手2",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "比分",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(72.dp)
            )
            Text(
                text = "详情",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(56.dp)
            )
        }

        // Rows
        games.forEachIndexed { index, game ->
            ScoreTableRow(
                index = index + 1,
                game = game,
                onClick = { onGameClick(game) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ScoreTableRow(
    index: Int,
    game: TtGameData,
    onClick: () -> Unit
) {
    val result1Color = when {
        game.result2 == "wo" || (game.result2?.toIntOrNull() ?: 0) < (game.result1?.toIntOrNull() ?: 0) -> Color(0xFFF89703)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val result2Color = when {
        game.result1 == "wo" || (game.result1?.toIntOrNull() ?: 0) < (game.result2?.toIntOrNull() ?: 0) -> Color(0xFFF89703)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = game.username1 ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = result1Color,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = game.username2 ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = result2Color,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${game.result1 ?: "-"}:${game.result2 ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = game.gameRemark ?: "-",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
    }
    HorizontalDivider()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScorePopup(
    game: TtGameData,
    selectedScore: String,
    scoreOptions: List<String>,
    isSubmitting: Boolean,
    onScoreSelect: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "第${game.nickname1 ?: ""}组 ${game.username1}:${game.username2} ${game.result1}:${game.result2}",
                fontSize = 14.sp
            )
        },
        text = {
            Column {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Score options
                Text("比分", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scoreOptions.forEach { score ->
                        ScoreChip(
                            score = score,
                            isSelected = selectedScore == score,
                            onClick = { onScoreSelect(score) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Abandon options would go here (simplified)
                Text("弃权", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "如需弃权，请联系裁判处理",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && selectedScore.isNotEmpty()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("确定")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun ScoreChip(
    score: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(32.dp)
            .background(
                color = if (isSelected) Color(0xFF77B980) else Color.White,
                shape = RoundedCornerShape(4.dp)
            )
            .border(1.dp, Color(0xFF77B980), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = score,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
