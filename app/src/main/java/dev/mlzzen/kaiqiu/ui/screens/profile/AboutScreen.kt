package dev.mlzzen.kaiqiu.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Surface(modifier = Modifier.size(96.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text("开球网", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("开球网", style = MaterialTheme.typography.headlineMedium)
            Text("版本 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            Text("开球网 - 乒乓球爱好者社区\n\n提供赛事查询、比分记录、球友交流等功能", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(modifier = Modifier.height(48.dp))
            TextButton(onClick = { }) { Text("访问官网") }
            Spacer(modifier = Modifier.height(16.dp))
            Text("© 2024 Kaiqiu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
