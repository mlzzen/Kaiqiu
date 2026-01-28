package dev.mlzzen.kaiqiu.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户详情") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(64.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("用户 $uid", style = MaterialTheme.typography.titleMedium)
                        Text("胜: 0 | 负: 0", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalButton(onClick = onNavigateToEvents) { Icon(Icons.Default.Star, contentDescription = null) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("参赛记录", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalButton(onClick = { }) { Icon(Icons.Default.Favorite, contentDescription = null) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("关注", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalButton(onClick = { }) { Icon(Icons.Default.Person, contentDescription = null) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("粉丝", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("比赛数据", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("总场次", "0")
                StatItem("胜场", "0")
                StatItem("负场", "0")
                StatItem("胜率", "0%")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
