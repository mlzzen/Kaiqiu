package dev.mlzzen.kaiqiu.ui.state

import android.content.Context
import dev.mlzzen.kaiqiu.data.datastore.AppDataStore
import dev.mlzzen.kaiqiu.data.datastore.CityData
import dev.mlzzen.kaiqiu.data.remote.HttpClient
import dev.mlzzen.kaiqiu.data.remote.UserInfo
import dev.mlzzen.kaiqiu.data.repository.Result
import dev.mlzzen.kaiqiu.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.gson.Gson

/**
 * 用户状态管理 (类似 Pinia Store)
 */
class UserState(
    private val context: Context,
    private val dataStore: AppDataStore = AppDataStore(context),
    private val userRepository: UserRepository = UserRepository()
) {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ============ State ============

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _location = MutableStateFlow<List<String>>(emptyList())
    val location: StateFlow<List<String>> = _location.asStateFlow()

    private val _selectCity = MutableStateFlow(CityData("1", "北京市"))
    val selectCity: StateFlow<CityData> = _selectCity.asStateFlow()

    private val _citySelectHis = MutableStateFlow<List<CityData>>(emptyList())
    val citySelectHis: StateFlow<List<CityData>> = _citySelectHis.asStateFlow()

    private val _isMoreMode = MutableStateFlow(false)
    val isMoreMode: StateFlow<Boolean> = _isMoreMode.asStateFlow()

    private val _searchPlayerHis = MutableStateFlow<List<String>>(emptyList())
    val searchPlayerHis: StateFlow<List<String>> = _searchPlayerHis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 初始化从 DataStore 加载数据
        scope.launch {
            dataStore.tokenFlow.collect { token ->
                _token.value = token
                _isLoggedIn.value = !token.isNullOrBlank()
                token?.let { HttpClient.setAuthToken(it) }
            }
        }

        scope.launch {
            dataStore.userInfoFlow.collect { userInfoJson ->
                if (!userInfoJson.isNullOrBlank()) {
                    try {
                        _userInfo.value = gson.fromJson(userInfoJson, UserInfo::class.java)
                    } catch (e: Exception) {
                        // 解析失败，保持 null
                    }
                } else {
                    _userInfo.value = null
                }
            }
        }

        scope.launch {
            dataStore.locationFlow.collect { locationJson ->
                if (!locationJson.isNullOrBlank()) {
                    try {
                        _location.value = gson.fromJson(locationJson, Array<String>::class.java).toList()
                    } catch (e: Exception) {
                        _location.value = emptyList()
                    }
                } else {
                    _location.value = emptyList()
                }
            }
        }

        scope.launch {
            dataStore.selectCityFlow.collect { city ->
                _selectCity.value = city
            }
        }

        scope.launch {
            dataStore.citySelectHisFlow.collect { list ->
                _citySelectHis.value = list
            }
        }

        scope.launch {
            dataStore.isMoreModeFlow.collect { mode ->
                _isMoreMode.value = mode
            }
        }

        scope.launch {
            dataStore.searchPlayerHisFlow.collect { list ->
                _searchPlayerHis.value = list
            }
        }
    }

    // ============ Actions ============

    /**
     * 登录
     */
    fun login(phone: String, code: String) {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = userRepository.login(phone, code)) {
                is Result.Success -> {
                    val response = result.data
                    _token.value = response.token
                    _userInfo.value = response.userInfo
                    _isLoggedIn.value = true

                    // 保存到 DataStore
                    dataStore.setToken(response.token)
                    dataStore.setUserInfo(gson.toJson(response.userInfo))
                    HttpClient.setAuthToken(response.token)
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "登录失败"
                }
                is Result.Loading -> { /* no-op */ }
            }

            _isLoading.value = false
        }
    }

    /**
     * 退出登录
     */
    fun logout() {
        scope.launch {
            _isLoading.value = true

            // 调用服务端登出
            userRepository.logout()

            // 清除本地状态
            _token.value = null
            _userInfo.value = null
            _isLoggedIn.value = false

            // 清除 DataStore
            dataStore.clearToken()
            dataStore.setUserInfo(null)
            HttpClient.clearAuthToken()

            _isLoading.value = false
        }
    }

    /**
     * 刷新用户信息
     */
    fun refreshUserInfo() {
        scope.launch {
            when (val result = userRepository.getUserInfo()) {
                is Result.Success -> {
                    _userInfo.value = result.data
                    dataStore.setUserInfo(gson.toJson(result.data))
                }
                is Result.Error -> {
                    _error.value = result.exception.message
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * 设置选择的城市
     */
    fun setSelectCity(city: CityData) {
        scope.launch {
            _selectCity.value = city
            dataStore.setSelectCity(city)
            dataStore.setCitySelectHis(city)
        }
    }

    /**
     * 设置位置
     */
    fun setLocation(locationArr: List<String>) {
        scope.launch {
            _location.value = locationArr
            dataStore.setLocation(gson.toJson(locationArr))
        }
    }

    /**
     * 设置更多模式
     */
    fun setMoreMode(enabled: Boolean) {
        scope.launch {
            _isMoreMode.value = enabled
            dataStore.setMoreMode(enabled)
        }
    }

    /**
     * 添加搜索历史
     */
    fun setSearchPlayerHis(keyword: String) {
        scope.launch {
            dataStore.setSearchPlayerHis(keyword)
        }
    }

    /**
     * 清空搜索历史
     */
    fun clearSearchPlayerHis() {
        scope.launch {
            dataStore.clearSearchPlayerHis()
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 清除所有数据 (退出时使用)
     */
    fun removeAll() {
        scope.launch {
            _token.value = null
            _userInfo.value = null
            _isLoggedIn.value = false
            _location.value = emptyList()

            dataStore.clearToken()
            dataStore.setUserInfo(null)
            dataStore.setLocation(null)
            HttpClient.clearAuthToken()
        }
    }

    // ============ Computed ============

    val cityName: String
        get() = _selectCity.value.name

    val isAuthenticated: Boolean
        get() = _isLoggedIn.value
}
