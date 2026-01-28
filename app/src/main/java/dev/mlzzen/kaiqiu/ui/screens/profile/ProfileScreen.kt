package dev.mlzzen.kaiqiu.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    onNavigateToUserEvents: (String) -> Unit,
    onNavigateToCitySelect: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val userState: UserState = LocalUserState.current
    val userInfo by userState.userInfo.collectAsState()
    val isLoggedIn by userState.isLoggedIn.collectAsState()
    val cityName = userState.cityName

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的") })
        }
    ) { paddingValues ->
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
                        .clickable {
                            if (isLoggedIn) {
                                userInfo?.uid?.let { onNavigateToUserDetail(it) }
                            } else {
                                onNavigateToLogin()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 头像
                        if (isLoggedIn) {
                            if (!userInfo?.image.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userInfo?.image?.let {
                                        if (!it.startsWith("http")) "https:$it" else it
                                    },
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
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
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
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLoggedIn) userInfo?.username ?: userInfo?.nickname
                                    ?: userInfo?.realname ?: "用户" else "未登录",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isLoggedIn) cityName else "点击登录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isLoggedIn) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            // 用户积分信息
            if (isLoggedIn) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserStatCard(
                            label = "当前积分",
                            value = userInfo?.score ?: "0",
                            modifier = Modifier.weight(1f)
                        )
                        UserStatCard(
                            label = "当前金币",
                            value = userInfo?.gold ?: "0",
                            modifier = Modifier.weight(1f)
                        )
                        UserStatCard(
                            label = "我的信用",
                            value = userInfo?.credit ?: "0",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // 每日签到
            if (isLoggedIn) {
                item {
                    SignInButton()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ProfileMenuGroup(
                    title = "常用",
                    items = listOf(
                        MenuItem(Icons.Default.LocationOn, "城市选择", cityName, onNavigateToCitySelect),
                        MenuItem(Icons.Default.Star, "我的参赛", "查看参赛记录", {
                            android.util.Log.d("ProfileDebug", "我的参赛 clicked, uid=${userInfo?.uid}")
                            userInfo?.uid?.let { onNavigateToUserEvents(it) }
                        }),
                        MenuItem(Icons.Default.Favorite, "我的关注", "关注的球友", {})
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ProfileMenuGroup(
                    title = "其他",
                    items = listOf(
                        MenuItem(Icons.Default.Info, "关于", "版本信息", onNavigateToAbout),
                        MenuItem(Icons.Default.Settings, "设置", "应用设置", {})
                    )
                )
            }

            if (isLoggedIn) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            userState.logout()
                            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("退出登录")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun UserStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F9FD)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1677FF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun ProfileMenuGroup(title: String, items: List<MenuItem>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.subtitle) },
                        leadingContent = { Icon(item.icon, contentDescription = null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                        modifier = Modifier.clickable { item.onClick() }
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInButton() {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var signedToday by remember { mutableStateOf(false) }

    fun checkSignStatus() {
        scope.launch {
            try {
                val response = HttpClient.api.getDaySign()
                if (response.isSuccess) {
                    signedToday = response.data?.msg?.contains("已签到") == true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        checkSignStatus()
    }

    Button(
        onClick = {
            if (!isLoading && !signedToday) {
                isLoading = true
                scope.launch {
                    try {
                        val response = HttpClient.api.getDaySign()
                        if (response.isSuccess) {
                            signedToday = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        enabled = !signedToday && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (signedToday) Color(0xFF39B54A) else MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            if (signedToday) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (signedToday) "今日已签到" else "每日签到")
    }
}
