package dev.mlzzen.kaiqiu.ui.screens.event

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.MemberDetail
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMembersScreen(
    eventid: String,
    matchId: String,
    itemId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var baseInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var members by remember { mutableStateOf<List<MemberDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val response = HttpClient.api.getMemberDetail(
                    mapOf("match_id" to matchId, "id" to itemId)
                )
                if (response.isSuccess) {
                    members = response.data?.list ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        baseInfo = mapOf(
            "event_name" to "赛事",
            "match_name" to "子项目"
        )
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("参赛名单") },
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
                // 赛事信息头
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFEFF))
                    ) {
                        Text(
                            text = "赛事",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF39B54A),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFEFF))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "子项目",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }

                // 表头
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "名称",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "报名积分",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(70.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "确认",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(80.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "性别",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(46.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    HorizontalDivider()
                }

                // 成员列表
                itemsIndexed(members) { index, member ->
                    MemberRow(
                        index = index + 1,
                        member = member,
                        onClick = { /* TODO: 跳转到用户详情 */ }
                    )
                    HorizontalDivider()
                }

                if (members.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无参赛名单", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    index: Int,
    member: MemberDetail,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 序号
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // 名称
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (member.role == 5) {
                Text(
                    text = "*",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE6326E)
                )
            }
            Text(
                text = member.name ?: member.nickname ?: "-",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (member.teamid != null) FontWeight.Medium else FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // 报名积分
        val scoreColor = when {
            member.score == 0 || member.newscore == "无积分" -> Color(0xFFF89703)
            else -> Color(0xFF39B54A)
        }
        Text(
            text = member.newscore ?: (member.score?.toString() ?: "-"),
            style = MaterialTheme.typography.bodySmall,
            color = scoreColor,
            modifier = Modifier.width(70.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // 确认状态
        val paidColor = when (member.paid) {
            0 -> Color(0xFFE6326E)
            1 -> Color(0xFF39B54A)
            2 -> Color(0xFFF89703)
            else -> Color(0xFF666666)
        }
        val paidText = when (member.paid) {
            0 -> "交费处理中"
            1 -> "已交付"
            2 -> "已报名"
            else -> "-"
        }
        Text(
            text = paidText,
            style = MaterialTheme.typography.bodySmall,
            color = paidColor,
            modifier = Modifier.width(80.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // 性别
        val sexText = when (member.sex) {
            1 -> "男"
            2 -> "女"
            else -> "-"
        }
        Text(
            text = sexText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(46.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
