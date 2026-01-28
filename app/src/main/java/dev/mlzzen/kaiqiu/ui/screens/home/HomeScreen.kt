package dev.mlzzen.kaiqiu.ui.screens.home

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
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开球网") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("北京市")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionChip(icon = Icons.Default.Star, label = "赛事", onClick = { })
                QuickActionChip(icon = Icons.Default.Star, label = "排行", onClick = { })
                QuickActionChip(icon = Icons.Default.Star, label = "球馆", onClick = { })
                QuickActionChip(icon = Icons.Default.Star, label = "比赛", onClick = { })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "近期比赛",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无比赛信息", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
