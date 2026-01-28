package dev.mlzzen.kaiqiu.ui.screens.search

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import dev.mlzzen.kaiqiu.data.remote.ArenaDetail
import dev.mlzzen.kaiqiu.data.remote.EventItem
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    arenaid: String,
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gymDetail by remember { mutableStateOf<ArenaDetail?>(null) }
    var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showContactSheet by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadGymDetail() {
        scope.launch {
            try {
                val response = HttpClient.api.getArenaDetail(mapOf("id" to arenaid))
                if (response.isSuccess) {
                    gymDetail = response.data
                } else {
                    error = response.msg
                }
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadEvents() {
        scope.launch {
            try {
                val response = HttpClient.api.getArenaMatchList(mapOf("id" to arenaid, "page" to "1"))
                if (response.isSuccess) {
                    events = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently fail for events
            }
        }
    }

    LaunchedEffect(arenaid) {
        loadGymDetail()
        loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gymDetail?.name ?: "球馆详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadGymDetail() }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Poster image
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            // Background decoration
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFFF89703))
                            )
                            // Poster
                            AsyncImage(
                                model = gymDetail?.images?.firstOrNull() ?: "",
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .align(Alignment.BottomCenter),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Name
                    item {
                        Text(
                            text = gymDetail?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Contact row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = gymDetail?.phone ?: "暂无联系人",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { showContactSheet = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF39B54A)
                                )
                            ) {
                                Text("联系Ta")
                            }
                        }
                    }

                    // Address row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    gymDetail?.let { gym ->
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(
                                                "geo:${gym.lat},${gym.lng}?q=${gym.address}"
                                            )
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = gymDetail?.address ?: "暂无地址",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Info section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "球馆信息",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF89703)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = gymDetail?.address ?: "暂无详细信息",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Recent events section
                    if (events.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "近期赛事",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF89703)
                                )
                            }
                        }

                        items(events) { event ->
                            EventCard(
                                event = event,
                                onClick = { onNavigateToEvent(event.eventid) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Contact bottom sheet
    if (showContactSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                gymDetail?.phone?.let { phone ->
                    ListItem(
                        headlineContent = { Text("打电话: $phone") },
                        leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                            showContactSheet = false
                        }
                    )
                }
                gymDetail?.phone?.let { wx ->
                    if (wx.isNotBlank()) {
                        ListItem(
                            headlineContent = { Text("加微信: $wx") },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                            modifier = Modifier.clickable {
                                context.getSystemService(android.content.ClipboardManager::class.java)
                                    ?.setPrimaryClip(android.content.ClipData.newPlainText("wechat", wx))
                                showContactSheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EventItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            // Poster
            AsyncImage(
                model = event.img,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.startTime ?: ""} ${event.endTime ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.arena ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
