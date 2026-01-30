package dev.mlzzen.kaiqiu.ui.screens.match

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import dev.mlzzen.kaiqiu.data.remote.GroupData
import dev.mlzzen.kaiqiu.data.remote.GroupPlayerName
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupScoreScreen(
    eventid: String,
    itemid: String,
    onNavigateBack: () -> Unit,
    onNavigateToScoreEntry: () -> Unit
) {
    val userState: UserState = LocalUserState.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var groups by remember { mutableStateOf<List<GroupData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentGroupIndex by remember { mutableIntStateOf(0) }

    // Popup state
    var showPopup by remember { mutableStateOf(false) }
    var selectedMatchInfo by remember { mutableStateOf<MatchInfo?>(null) }
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
                val response = HttpClient.api.getGroupGames(
                    mapOf("eventid" to eventid, "itemid" to itemid)
                )
                if (response.isSuccess) {
                    groups = response.data ?: emptyList()
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
        val match = selectedMatchInfo ?: return
        if (selectedScore.isEmpty()) {
            Toast.makeText(context, "请设置比分", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isSubmitting = true
            try {
                val response = HttpClient.api.updateScore(
                    mapOf(
                        "groupid" to match.groupid,
                        "uid1" to match.uid1,
                        "uid2" to match.uid2,
                        "score" to selectedScore,
                        "eventid" to eventid,
                        "itemid" to itemid
                    )
                )
                if (response.isSuccess) {
                    showPopup = false
                    selectedMatchInfo = null
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
                title = { Text("小组记分") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToScoreEntry() }) {
                        Text("淘汰赛", style = MaterialTheme.typography.bodyMedium)
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
            groups.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无小组数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Group tabs
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(groups) { index, group ->
                            TabButton(
                                text = "第${index + 1}组",
                                isSelected = currentGroupIndex == index,
                                onClick = { currentGroupIndex = index }
                            )
                        }
                    }

                    HorizontalDivider()

                    // Group table
                    val currentGroup = groups.getOrNull(currentGroupIndex)
                    if (currentGroup != null) {
                        GroupTable(
                            group = currentGroup,
                            groupIndex = currentGroupIndex,
                            onCellClick = { matchInfo ->
                                selectedMatchInfo = matchInfo
                                selectedScore = ""
                                showPopup = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Score popup
    if (showPopup && selectedMatchInfo != null) {
        ScorePopup(
            match = selectedMatchInfo!!,
            selectedScore = selectedScore,
            scoreOptions = scoreOptions,
            isSubmitting = isSubmitting,
            onScoreSelect = { score ->
                selectedScore = score
            },
            onSubmit = { submitScore() },
            onDismiss = {
                showPopup = false
                selectedMatchInfo = null
                selectedScore = ""
            }
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1677FF) else Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else Color(0xFF333333)
        )
    }
}

@Composable
private fun GroupTable(
    group: GroupData,
    groupIndex: Int,
    onCellClick: (MatchInfo) -> Unit
) {
    val players = group.names
    if (players.isEmpty()) return

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选手",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(80.dp)
                )
                players.forEachIndexed { index, player ->
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "积分",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text = "名次",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp)
                )
            }
        }

        // Player rows
        itemsIndexed(players) { rowIndex, player ->
            PlayerRow(
                player = player,
                rowIndex = rowIndex,
                players = players,
                scores = group.scores,
                groupid = group.groupid ?: "",
                onCellClick = onCellClick
            )
        }
    }
}

@Composable
private fun PlayerRow(
    player: GroupPlayerName,
    rowIndex: Int,
    players: List<GroupPlayerName>,
    scores: Map<String, String>,
    groupid: String,
    onCellClick: (MatchInfo) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player name
            Text(
                text = "${rowIndex + 1}. ${player.username ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(80.dp)
            )

            // Score cells
            players.forEachIndexed { colIndex, opponent ->
                val key = "${player.uid}:${opponent.uid}"
                val score = scores[key] ?: ""
                val (displayScore, textColor) = parseScore(score)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .then(
                            if (rowIndex == colIndex) {
                                Modifier.background(Color(0xFFF2F1EE))
                            } else if (score.isNotEmpty()) {
                                Modifier.clickable {
                                    onCellClick(
                                        MatchInfo(
                                            groupid = groupid,
                                            uid1 = player.uid,
                                            uid2 = opponent.uid,
                                            username1 = player.username ?: "",
                                            username2 = opponent.username ?: "",
                                            result = score
                                        )
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (rowIndex != colIndex) {
                        Text(
                            text = displayScore,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Sum score
            Text(
                text = player.sumScore ?: "-",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )

            // Rank
            Text(
                text = player.rank ?: "-",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )
        }
        HorizontalDivider()
    }
}

private fun parseScore(score: String): Pair<String, Color> {
    if (score.isEmpty()) return "" to Color.Unspecified

    val parts = score.split(":")
    if (parts.size != 2) return score to Color.Unspecified

    val result1 = parts[0]
    val result2 = parts[1]

    return "$result1:$result2" to when {
        result2 == "wo" || (result1.toIntOrNull() ?: 0) > (result2.toIntOrNull() ?: 0) -> Color(0xFFE6326E)
        result1 == "wo" || (result2.toIntOrNull() ?: 0) > (result1.toIntOrNull() ?: 0) -> Color(0xFFE6326E)
        else -> Color.Unspecified
    }
}

private data class MatchInfo(
    val groupid: String,
    val uid1: String,
    val uid2: String,
    val username1: String,
    val username2: String,
    val result: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScorePopup(
    match: MatchInfo,
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
                text = "第${match.username1}组 ${match.username1}:${match.username2} ${match.result}",
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
