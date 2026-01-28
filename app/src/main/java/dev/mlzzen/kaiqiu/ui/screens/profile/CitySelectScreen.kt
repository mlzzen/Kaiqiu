package dev.mlzzen.kaiqiu.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mlzzen.kaiqiu.data.datastore.CityData
import dev.mlzzen.kaiqiu.ui.state.UserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectScreen(
    onNavigateBack: () -> Unit,
    onCitySelected: () -> Unit
) {
    val userState: UserState = viewModel()
    val selectCity by userState.selectCity.collectAsState()
    val citySelectHis by userState.citySelectHis.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val cities = remember {
        listOf(
            CityData("1", "北京市"), CityData("2", "上海市"), CityData("3", "广州市"),
            CityData("4", "深圳市"), CityData("5", "成都市"), CityData("6", "杭州市"),
            CityData("7", "武汉市"), CityData("8", "南京市"), CityData("9", "西安市"),
            CityData("10", "重庆市")
        )
    }

    val filteredCities = remember(searchText, cities) {
        if (searchText.isBlank()) cities else cities.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择城市") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("搜索城市") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (citySelectHis.isNotEmpty() && searchText.isBlank()) {
                Text("最近选择", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    citySelectHis.take(3).forEach { city ->
                        SuggestionChip(onClick = { userState.setSelectCity(city); onCitySelected() }, label = { Text(city.name) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn { items(filteredCities) { city ->
                ListItem(
                    headlineContent = { Text(city.name) },
                    trailingContent = { if (city.id == selectCity.id) Icon(Icons.Default.Check, contentDescription = "已选择", tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { userState.setSelectCity(city); onCitySelected() }
                )
            }}
        }
    }
}
