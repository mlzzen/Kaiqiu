package dev.mlzzen.kaiqiu.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.AdvProfile
import dev.mlzzen.kaiqiu.data.remote.UserScores
import dev.mlzzen.kaiqiu.data.repository.Result
import dev.mlzzen.kaiqiu.data.repository.UserRepository
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    android.util.Log.d("UserDetail", "UserDetailScreen init, uid=$uid")
    val userRepository = remember { UserRepository() }
    var profile by remember { mutableStateOf<AdvProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        android.util.Log.d("UserDetail", "LaunchedEffect triggered, uid=$uid")
        isLoading = true
        android.util.Log.d("UserDetail", "=== getAdvProfile uid=$uid ===")
        try {
            val result = userRepository.getAdvProfile(uid)
            android.util.Log.d("UserDetail", "getAdvProfile result type: ${result::class.simpleName}")
            when (result) {
                is Result.Success -> {
                    profile = result.data
                    android.util.Log.d("UserDetail", "getAdvProfile success: $profile")
                    android.util.Log.d("UserDetail", "  nickname: ${profile?.nickname}")
                    android.util.Log.d("UserDetail", "  avatar: ${profile?.avatar}")
                    android.util.Log.d("UserDetail", "  scores: ${profile?.scores}")
                }
                is Result.Error -> {
                    profile = null
                    android.util.Log.d("UserDetail", "getAdvProfile error: ${result.exception}")
                    result.exception.printStackTrace()
                }
                is Result.Loading -> {
                    android.util.Log.d("UserDetail", "getAdvProfile loading")
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("UserDetail", "getAdvProfile exception: ${e.message}")
            e.printStackTrace()
        }
        isLoading = false
        android.util.Log.d("UserDetail", "isLoading=$isLoading, profile=$profile")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户详情") },
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
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarUrl = profile?.avatar
                            if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = if (!avatarUrl.startsWith("http")) "https:$avatarUrl" else avatarUrl,
                                    contentDescription = "头像",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(MaterialTheme.shapes.large),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val displayName = profile?.nickname ?: profile?.realname ?: "用户"
                                Text(displayName, style = MaterialTheme.typography.titleMedium)
                                profile?.scores?.let { scores ->
                                    Text(
                                        "胜: ${scores.wins} | 负: ${scores.losses}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = Icons.Default.Star,
                            label = "参赛记录",
                            onClick = onNavigateToEvents
                        )
                        ActionButton(
                            icon = Icons.Default.Favorite,
                            label = "关注",
                            onClick = { }
                        )
                        ActionButton(
                            icon = Icons.Default.Person,
                            label = "粉丝",
                            onClick = { }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "比赛数据",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    profile?.scores?.let { scores ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("总场次", scores.totalGames.toString())
                            StatItem("胜场", scores.wins.toString())
                            StatItem("负场", scores.losses.toString())
                            StatItem("胜率", scores.winRate ?: "0%")
                        }
                    } ?: run {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("总场次", "0")
                            StatItem("胜场", "0")
                            StatItem("负场", "0")
                            StatItem("胜率", "0%")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "标签",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    profile?.tags?.let { tags ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.take(6).forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
