package dev.mlzzen.kaiqiu.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val tabs = listOf("赛事", "球友", "球馆")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("搜索") })
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
                placeholder = { Text("搜索赛事、球友、球馆") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("输入关键词搜索${tabs[selectedTab]}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}
