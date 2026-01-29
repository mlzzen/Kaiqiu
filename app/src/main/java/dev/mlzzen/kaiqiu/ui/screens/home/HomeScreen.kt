package dev.mlzzen.kaiqiu.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import dev.mlzzen.kaiqiu.data.datastore.AppDataStore
import dev.mlzzen.kaiqiu.data.datastore.CityData
import dev.mlzzen.kaiqiu.data.remote.EventItem
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCitySelect: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { AppDataStore(context) }

    var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf(CityData("1", "北京市")) }

    // 获取当前城市
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationObtained: (lat: String, lng: String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            onLocationObtained(location.latitude.toString(), location.longitude.toString())
        } else {
            // 如果无法获取位置，使用默认
            onLocationObtained("39.9042", "116.4074")
        }
    }

    // 权限请求
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation { lat, lng ->
                scope.launch {
                    // 保存位置到 DataStore
                    dataStore.setLocation("""["$lng","$lat"]""")
                }
            }
        }
    }

    // 检查并请求位置权限
    fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation { lat, lng ->
                    scope.launch {
                        dataStore.setLocation("""["$lng","$lat"]""")
                    }
                }
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // 加载城市选择
    LaunchedEffect(Unit) {
        dataStore.selectCityFlow.collect { city ->
            selectedCity = city
        }
    }

    // 启动时自动定位
    LaunchedEffect(Unit) {
        checkLocationPermission()
    }

    // 加载比赛列表
    fun loadEvents() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val locationJson = dataStore.getLocationSync()
                val (lng, lat) = if (locationJson.isNullOrBlank()) {
                    "116.4074" to "39.9042"
                } else {
                    try {
                        val loc = locationJson.removeSurrounding("[\"", "\"]").split("\",\"")
                        if (loc.size == 2) loc[0] to loc[1] else "116.4074" to "39.9042"
                    } catch (e: Exception) {
                        "116.4074" to "39.9042"
                    }
                }
                val response = HttpClient.api.getMatchListByPage(
                    mapOf(
                        "city" to selectedCity.name,
                        "lat" to lat,
                        "lng" to lng,
                        "page" to "1",
                        "sort" to "4"
                    )
                )
                if (response.isSuccess) {
                    events = response.data?.data ?: emptyList()
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

    LaunchedEffect(selectedCity) {
        loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("开球网")
                        Spacer(modifier = Modifier.width(8.dp))
                        // 城市选择按钮
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNavigateToCitySelect() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF39B54A)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                selectedCity.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF39B54A)
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF39B54A)
                            )
                        }
                    }
                },
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "近期比赛",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 比赛列表
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadEvents() }) {
                                Text("重试")
                            }
                        }
                    }
                }
                events.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("暂无比赛信息", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
}

@Composable
private fun EventCard(
    event: EventItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 比赛海报
            Box {
                AsyncImage(
                    model = event.poster?.let { if (!it.startsWith("http")) "https:$it" else it },
                    contentDescription = event.title,
                    modifier = Modifier
                        .width(100.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentScale = ContentScale.Crop
                )
                // 城市标签
                event.city?.let { city ->
                    Text(
                        text = city,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFF77B980), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 比赛信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.startTime?.take(10) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = " | ${event.status ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE49B37)
                    )
                }

                Text(
                    text = "比赛地点: ${event.arenaName ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${event.viewnum ?: "0"}人浏览",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${event.membernum ?: "0"}人参加",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF77B980),
                        modifier = Modifier
                            .background(
                                Color(0x3377B980),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
