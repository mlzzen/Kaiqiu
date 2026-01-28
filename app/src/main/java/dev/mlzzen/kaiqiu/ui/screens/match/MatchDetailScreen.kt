package dev.mlzzen.kaiqiu.ui.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(gameid: String, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("比赛详情") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("比赛 ID: $gameid", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
