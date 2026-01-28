package dev.mlzzen.kaiqiu.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mlzzen.kaiqiu.ui.state.UserState
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    onNavigateToCitySelect: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val userState: UserState = viewModel()
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
                        .clickable(enabled = isLoggedIn) {
                            userInfo?.uid?.let { uid -> onNavigateToUserDetail(uid) }
                        },
                    onClick = { if (!isLoggedIn) onNavigateToLogin() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userInfo?.nickname ?: "未登录", style = MaterialTheme.typography.titleMedium)
                            Text(if (isLoggedIn) cityName else "点击登录", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (isLoggedIn) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ProfileMenuGroup(
                    title = "常用",
                    items = listOf(
                        MenuItem(Icons.Default.LocationOn, "城市选择", cityName, onNavigateToCitySelect),
                        MenuItem(Icons.Default.Star, "我的参赛", "查看参赛记录", { userInfo?.uid?.let { onNavigateToUserDetail(it) } }),
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
