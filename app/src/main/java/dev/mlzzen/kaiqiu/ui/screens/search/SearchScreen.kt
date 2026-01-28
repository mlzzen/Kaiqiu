package dev.mlzzen.kaiqiu.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToEvent: (String) -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFilter by remember { mutableStateOf(false) }
    val tabs = listOf("赛事", "球馆", "球友")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索") },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { showFilter = !showFilter }) {
                            Icon(Icons.Default.Settings, contentDescription = "筛选")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("搜索${tabs[selectedTab]}") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            if (selectedTab == 0 && showFilter) {
                SearchFilterPanel(
                    onSearch = { /* 搜索 */ },
                    onClose = { showFilter = false }
                )
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入关键词搜索${tabs[selectedTab]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterPanel(
    onSearch: (Map<String, String>) -> Unit,
    onClose: () -> Unit
) {
    var dateRange by remember { mutableStateOf<DateRange?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var distanceIndex by remember { mutableIntStateOf(0) }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

    val distanceOptions = listOf("不限", "<=3公里", "<=5公里", "<=10公里", "<=20公里")
    val distanceValues = listOf(0, 3, 5, 10, 20)
    val tags = listOf("网红", "大奖赛", "青少年")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 起止日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("起止日期", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* 显示日期选择器 */ },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = dateRange?.let { "${it.start} 至 ${it.end}" } ?: "不限",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 举办城市
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("举办城市", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* 选择城市 */ },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = selectedCity ?: "不限",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 距离区间
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("距离区间", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = distanceOptions[distanceIndex],
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    distanceOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                distanceIndex = index
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 赛事标签
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("赛事标签", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(80.dp))
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTags = if (isSelected) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        },
                        label = { Text(tag) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    dateRange = null
                    selectedCity = null
                    distanceIndex = 0
                    selectedTags = emptyList()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("重置")
            }
            Button(
                onClick = {
                    val params = buildMap {
                        dateRange?.let {
                            put("startMatchTimestamp", it.startTimestamp)
                            put("endMatchTimestamp", it.endTimestamp)
                        }
                        selectedCity?.let { put("city", it) }
                        if (distanceIndex > 0) {
                            put("distance", "lt${distanceValues[distanceIndex]}")
                        }
                        if (selectedTags.isNotEmpty()) {
                            put("quickTags", selectedTags.joinToString(","))
                        }
                    }
                    onSearch(params)
                    onClose()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("确定")
            }
        }
    }

    HorizontalDivider()
}

private data class DateRange(
    val start: String,
    val end: String,
    val startTimestamp: String,
    val endTimestamp: String
)
