package dev.mlzzen.kaiqiu.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.UserItem
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    val userState: UserState = LocalUserState.current
    val selectCity by userState.selectCity.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var list by remember { mutableStateOf<List<UserItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(1) }

    var tabIndex by remember { mutableIntStateOf(0) }
    var showFilter by remember { mutableStateOf(false) }
    var selectIndexs by remember { mutableStateOf(listOf(0, 0, 0, 0)) }

    val tabs = listOf("全城", "全省", "全球")
    val filterOptions = listOf(
        FilterGroup("年龄", listOf("全部", "10岁以下", "11~16岁", "17~29岁", "30~39岁", "40~49岁", "50~59岁", "60~69岁", "70~79岁", "80岁以上")),
        FilterGroup("性别", listOf("全部", "男", "女")),
        FilterGroup("背景", listOf("全部", "业余", "专业")),
        FilterGroup("积分", listOf("全部", "U1500", "U1700", "U1900", "U2100", "U2300", "U2500"))
    )

    val showFilterActive = selectIndexs.any { it != 0 }

    fun getFilterIndex(): String {
        val first = (tabIndex + 1) * 10 + (selectIndexs[0] + 1)
        return "$first${selectIndexs[1] + 1}${selectIndexs[2] + 1}${selectIndexs[3] + 1}"
    }

    fun loadData(isRefresh: Boolean) {
        if (isRefresh) {
            currentPage = 1
            list = emptyList()
            hasMore = true
        }
        if (!hasMore) return

        scope.launch {
            if (isRefresh) {
                isLoading = true
            } else {
                isLoadingMore = true
            }
            error = null

            try {
                val params = mapOf(
                    "city" to "-${tabIndex + 1}",
                    "now" to selectCity.name,
                    "sort" to "2",
                    "page" to currentPage.toString(),
                    "index" to getFilterIndex()
                )
                val response = HttpClient.api.getPageUserRankList(params)
                if (response.isSuccess) {
                    val data = response.data
                    val newList = data?.list ?: emptyList()
                    if (isRefresh) {
                        list = newList
                    } else {
                        list = list + newList
                    }
                    hasMore = newList.size >= (data?.total ?: 0)
                    if (newList.isNotEmpty()) {
                        currentPage++
                    }
                } else {
                    error = response.msg
                }
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
    }

    fun refresh() {
        showFilter = false
        loadData(isRefresh = true)
    }

    // Load more when reaching end of list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= list.size - 3 && !isLoadingMore && hasMore) {
                    loadData(isRefresh = false)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排行") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "筛选",
                            tint = if (showFilterActive) Color(0xFFF89703) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // City selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Navigate to city select */ }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF77B980),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectCity.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF77B980),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider()

                // Tabs
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = {
                                tabIndex = index
                                refresh()
                            },
                            text = { Text(title) }
                        )
                    }
                }

                // Table header
                RankTableHeader()

                // Table content
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { refresh() }) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(list) { index, item ->
                                RankTableRow(
                                    index = index + 1,
                                    item = item,
                                    onClick = { item.uid?.let { onNavigateToUser(it) } }
                                )
                            }
                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter dropdown
            AnimatedVisibility(
                visible = showFilter,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { showFilter = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                            .clickable(enabled = false) { }
                    ) {
                        filterOptions.forEachIndexed { groupIndex, group ->
                            FilterGroupView(
                                groupName = group.name,
                                options = group.options,
                                selectedIndex = selectIndexs.getOrElse(groupIndex) { 0 },
                                onSelect = { index ->
                                    selectIndexs = selectIndexs.toMutableList().also {
                                        it[groupIndex] = index
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    selectIndexs = listOf(0, 0, 0, 0)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("重置")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = { refresh() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class FilterGroup(
    val name: String,
    val options: List<String>
)

@Composable
private fun FilterGroupView(
    groupName: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = groupName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.take(5).forEachIndexed { index, option ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    label = { Text(option, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (options.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.drop(5).forEachIndexed { index, option ->
                    FilterChip(
                        selected = selectedIndex == index + 5,
                        onClick = { onSelect(index + 5) },
                        label = { Text(option, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 12.dp),
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
            text = "姓名",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = "当前积分",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "最高",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = "最低",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = "性别",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = "生于",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
}

@Composable
private fun RankTableRow(
    index: Int,
    item: UserItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = item.nickname ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = item.scores?.totalGames?.toString() ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "-",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = "-",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
        val sexText = when (item.sex) {
            "1" -> "男"
            "2" -> "女"
            else -> "-"
        }
        Text(
            text = sexText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = "-",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
}
