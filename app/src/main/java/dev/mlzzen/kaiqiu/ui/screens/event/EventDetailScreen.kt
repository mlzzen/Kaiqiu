package dev.mlzzen.kaiqiu.ui.screens.event

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.*
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
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

    // 赛程数据
    var groupDataList by remember { mutableStateOf<List<GroupData>>(emptyList()) }
    var knockoutRounds by remember { mutableStateOf<List<RoundData>>(emptyList()) }

    // 成绩数据
    var honors by remember { mutableStateOf<List<HonorItem>>(emptyList()) }
    var results by remember { mutableStateOf<List<ResultItem>>(emptyList()) }

    // 积分数据
    var scoreChanges by remember { mutableStateOf<Map<String, List<ScoreChange>>>(emptyMap()) }
    var myScoreChanges by remember { mutableStateOf<Map<String, List<MyScoreChange>>>(emptyMap()) }

    val tabList = listOf("详情", "赛程", "成绩", "积分")
    val crtItem: EventItemInfo? = items.find { it.id == activeItemId }

    suspend fun loadScheduleData() {
        try {
            val params = mapOf("eventid" to eventid, "itemid" to (activeItemId ?: ""))
            android.util.Log.d("EventDetail", "loadScheduleData params: $params")

            val groupResponse = HttpClient.api.getGroupGames(params)
            android.util.Log.d("EventDetail", "getGroupGames response: ${groupResponse.code}, data=${groupResponse.data?.size}")
            if (groupResponse.isSuccess) {
                groupDataList = groupResponse.data ?: emptyList()
            }

            val knockoutResponse = HttpClient.api.getArrangeKnockout(params)
            android.util.Log.d("EventDetail", "getArrangeKnockout response: ${knockoutResponse.code}, rounds=${knockoutResponse.data?.rounds?.size}")
            if (knockoutResponse.isSuccess) {
                knockoutRounds = knockoutResponse.data?.rounds ?: emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("EventDetail", "loadScheduleData error", e)
            e.printStackTrace()
        }
    }

    suspend fun loadResultData() {
        try {
            val params = mapOf("eventid" to eventid, "itemid" to (activeItemId ?: ""))
            android.util.Log.d("EventDetail", "loadResultData params: $params")

            val honorResponse = HttpClient.api.getAllHonors(params)
            android.util.Log.d("EventDetail", "getAllHonors response: ${honorResponse.code}, honors=${honorResponse.data?.size}")
            if (honorResponse.isSuccess) {
                honors = honorResponse.data ?: emptyList()
            }

            val resultResponse = HttpClient.api.getAllResult(params)
            android.util.Log.d("EventDetail", "getAllResult response: ${resultResponse.code}, results=${resultResponse.data?.size}")
            if (resultResponse.isSuccess) {
                results = resultResponse.data ?: emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("EventDetail", "loadResultData error", e)
            e.printStackTrace()
        }
    }

    suspend fun loadScoreData() {
        try {
            android.util.Log.d("EventDetail", "loadScoreData eventid: $eventid")

            val scoreResponse = HttpClient.api.getScoreChange(eventid)
            android.util.Log.d("EventDetail", "getScoreChange response: ${scoreResponse.code}, data=${scoreResponse.data?.sc?.size}")
            if (scoreResponse.isSuccess) {
                scoreChanges = scoreResponse.data?.sc ?: emptyMap()
                myScoreChanges = scoreResponse.data?.mysc ?: emptyMap()
            }
        } catch (e: Exception) {
            android.util.Log.e("EventDetail", "loadScoreData error", e)
            e.printStackTrace()
        }
    }

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
                    scope.launch { loadScheduleData() }
                    scope.launch { loadResultData() }
                    scope.launch { loadScoreData() }
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
                title = { Text("比赛详情") },
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
                            item {
                                ScheduleTab(
                                    groupDataList = groupDataList,
                                    knockoutRounds = knockoutRounds,
                                    onNavigateToScore = { onNavigateToScore(activeItemId ?: "") }
                                )
                            }
                        }
                        2 -> {
                            item { ResultTab(honors = honors, results = results) }
                        }
                        3 -> {
                            item {
                                ScoreChangeTab(
                                    scoreChanges = scoreChanges,
                                    myScoreChanges = myScoreChanges,
                                    activeItemId = activeItemId
                                )
                            }
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
        // 比赛标题
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

        // 比赛说明
        if (!eventDetail?.note.isNullOrEmpty()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("比赛说明", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                HtmlText(htmlContent = decodedDetail)
            }
        }
    }
}

/**
 * 渲染 HTML 内容的组件
 * 使用 WebView 完整渲染 HTML，包括图片、链接等
 */
@Composable
fun HtmlText(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    // 允许文件访问
                    allowFileAccess = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {}
            }
        },
        update = { webView ->
            // 构建完整的 HTML 文档
            val fullHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * {
                            max-width: 100%;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            font-size: 14px;
                            line-height: 1.6;
                            color: #1a1a1a;
                            padding: 8px;
                            margin: 0;
                        }
                        img {
                            max-width: 100% !important;
                            width: 100% !important;
                            height: auto !important;
                            display: block;
                            margin: 8px 0;
                        }
                        div, p, span {
                            max-width: 100% !important;
                            word-wrap: break-word;
                            overflow-wrap: break-word;
                        }
                        a {
                            color: #39B54A;
                            text-decoration: none;
                        }
                        p {
                            margin: 8px 0;
                        }
                        table {
                            max-width: 100% !important;
                            width: 100% !important;
                        }
                    </style>
                </head>
                <body>
                    $htmlContent
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
        }
    )
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
private fun ScheduleTab(
    groupDataList: List<GroupData>,
    knockoutRounds: List<RoundData>,
    onNavigateToScore: () -> Unit
) {
    val hasData = groupDataList.isNotEmpty() || knockoutRounds.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasData) {
            // 小组赛 - 交叉对阵表
            groupDataList.forEach { group ->
                // 显示组名（可能是 matchName 或 tablenum）
                val groupTitle = group.matchName?.takeIf { it.isNotEmpty() }
                    ?: group.tablenum?.takeIf { it.isNotEmpty() }
                    ?: "小组赛"
                Text(
                    text = "第${groupTitle}台",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()

                // 构建交叉对阵表
                if (group.names?.isNotEmpty() == true) {
                    CrossMatchTable(
                        names = group.names,
                        scores = group.scores
                    )
                }
            }

            // 淘汰赛
            if (knockoutRounds.isNotEmpty()) {
                Text(
                    text = "淘汰赛",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()
                knockoutRounds.forEach { round ->
                    Text(
                        text = round.roundname,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF39B54A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    round.games?.forEach { game ->
                        GameResultRow(game = game)
                    }
                }
            }
        } else {
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
    }
}

/**
 * 交叉对阵表组件
 * 显示选手之间的比赛结果矩阵
 */
@Composable
private fun CrossMatchTable(
    names: List<GroupPlayerName>,
    scores: Map<String, String>
) {
    Column(modifier = Modifier.padding(8.dp)) {
        // 表头行
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 左上角空白单元格
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .padding(4.dp)
            )
            // 选手序号
            names.forEachIndexed { index, _ ->
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // 数据行
        names.forEachIndexed { rowIndex, rowPlayer ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 行首：选手序号 + 名字
                Row(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${rowIndex + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = rowPlayer.username?.substringAfter(" ")?.substringBefore(" ") ?: rowPlayer.username ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                // 交叉对阵单元格
                names.forEachIndexed { colIndex, colPlayer ->
                    val scoreKey = "${rowPlayer.uid}:${colPlayer.uid}"
                    val score = scores[scoreKey]
                    val isDiagonal = rowIndex == colIndex

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .background(
                                if (isDiagonal) Color(0xFFF5F5F5) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDiagonal) "-" else score ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDiagonal) Color.Gray else Color.Unspecified,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameResultRow(game: TtGameData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = game.nickname1 ?: game.username1 ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${game.result1 ?: 0} : ${game.result2 ?: 0}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = game.nickname2 ?: game.username2 ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun ResultTab(honors: List<HonorItem>, results: List<ResultItem>) {
    val hasData = honors.isNotEmpty() || results.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasData) {
            // 荣誉榜
            if (honors.isNotEmpty()) {
                Text(
                    text = "荣誉榜",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()
                honors.forEach { honor ->
                    ResultRow(
                        rank = honor.ranking,
                        nickname = honor.nickname ?: "-",
                        score = honor.score ?: "-",
                        avatar = honor.avatar
                    )
                    HorizontalDivider()
                }
            }

            // 比赛结果
            if (results.isNotEmpty()) {
                Text(
                    text = "比赛结果",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()
                results.forEach { result ->
                    ResultRow(
                        rank = result.ranking,
                        nickname = result.nickname ?: "-",
                        score = "${result.wins}胜 ${result.losses}负",
                        avatar = result.avatar
                    )
                    HorizontalDivider()
                }
            }
        } else {
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
    }
}

@Composable
private fun ResultRow(rank: Int, nickname: String, score: String, avatar: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排名
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = when (rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> Color.Gray
            },
            modifier = Modifier.width(40.dp)
        )

        // 头像
        AsyncImage(
            model = avatar?.let { if (!it.startsWith("http")) "https:$it" else it },
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .padding(end = 8.dp),
            contentScale = ContentScale.Crop
        )

        // 昵称
        Text(
            text = nickname,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // 分数/战绩
        Text(
            text = score,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ScoreChangeTab(
    scoreChanges: Map<String, List<ScoreChange>>,
    myScoreChanges: Map<String, List<MyScoreChange>>,
    activeItemId: String?
) {
    // 获取当前选中项目的积分数据，如果没有选中则显示所有
    val currentItemChanges = when {
        activeItemId != null && scoreChanges.containsKey(activeItemId) -> {
            scoreChanges[activeItemId] ?: emptyList()
        }
        else -> {
            // 合并所有项目的积分变化
            scoreChanges.values.flatten()
        }
    }

    val hasData = currentItemChanges.isNotEmpty()

    // 获取当前登录用户信息
    val userState = LocalUserState.current
    val currentUserUid = userState.userInfo.value?.uid

    // 查找当前用户的积分变化记录
    val currentUserChange = currentItemChanges.find { it.uid == currentUserUid }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasData) {
            // 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选手", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("赛前积分", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(70.dp))
                Text("变化", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
                Text("赛后积分", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(70.dp))
            }
            HorizontalDivider()

            // 积分变化列表
            currentItemChanges.forEach { change ->
                ScoreChangeRow(change = change)
                HorizontalDivider()
            }

            // 当前用户的比赛记录
            if (currentUserChange != null) {
                val myRecords = activeItemId?.let { myScoreChanges[it] } ?: emptyList()
                if (myRecords.isNotEmpty()) {
                    UserMatchRecordTable(
                        userName = currentUserChange.realname ?: currentUserChange.username ?: "我",
                        myRecords = myRecords
                    )
                }
            }

            // 个人积分变化详情
            val changesWithDetail = currentItemChanges.filter { !it.detail.isNullOrEmpty() }
            if (changesWithDetail.isNotEmpty()) {
                Text(
                    text = "个人积分变化",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()

                changesWithDetail.forEach { change ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = change.realname ?: change.username ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${change.change}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF39B54A)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = change.detail ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    HorizontalDivider()
                }
            }
        } else {
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
    }
}

/**
 * 当前用户的比赛记录表格
 * 表头：序号、我、对手、比分、变化、去评价
 */
@Composable
private fun UserMatchRecordTable(
    userName: String,
    myRecords: List<MyScoreChange>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "个人积分变化",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        HorizontalDivider()

        // 表头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("序号", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
            Text(userName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("对手", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("比分", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
            Text("变化", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(50.dp))
            Text("去评价", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
        }
        HorizontalDivider()

        // 真实数据行
        myRecords.forEachIndexed { index, record ->
            val displayScore = when {
                !record.result1.isNullOrEmpty() && !record.result2.isNullOrEmpty() -> "${record.result1}:${record.result2}"
                else -> "-"
            }
            val opponentName = record.username2 ?: "-"
            val changeValue = record.score1 ?: ""
            val changeColor = when {
                changeValue.startsWith("+") -> Color(0xFF39B54A)
                changeValue.startsWith("-") -> Color(0xFFE6326E)
                else -> Color.Unspecified
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text((index + 1).toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
                Text("我", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(opponentName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(displayScore, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
                Text(
                    changeValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = changeColor,
                    modifier = Modifier.width(50.dp)
                )
                Text("", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ScoreChangeRow(change: ScoreChange) {
    val changeValue = change.change?.toDoubleOrNull() ?: 0.0
    val changeColor = when {
        changeValue > 0 -> Color(0xFF39B54A)
        changeValue < 0 -> Color(0xFFE6326E)
        else -> Color.Gray
    }
    val changeText = when {
        changeValue > 0 -> "+${change.change}"
        else -> change.change ?: "0"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 姓名优先显示 realname，其次 username
        Text(
            text = change.realname ?: change.username ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = change.prescore ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = changeText,
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = change.postscore ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(70.dp)
        )
    }
}
