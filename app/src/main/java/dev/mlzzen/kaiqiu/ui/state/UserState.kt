package dev.mlzzen.kaiqiu.ui.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.runtime.remember
import com.google.gson.Gson

/**
 * 用户状态管理 (类似 Pinia Store)
 * 使用 CompositionLocal 提供单例
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

    init {
        // 立即检查是否有已保存的 token
        scope.launch {
            val token = dataStore.getTokenSync()
            _isLoggedIn.value = !token.isNullOrBlank()
            token?.let { HttpClient.setAuthToken(it) }
        }
    }

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
        // 同步初始化：恢复登录状态
        val savedToken = dataStore.getTokenSync()
        val savedUserInfo = dataStore.getUserInfoSync()

        android.util.Log.d("ProfileDebug", "=== UserState init ===")
        android.util.Log.d("ProfileDebug", "savedToken: $savedToken")
        android.util.Log.d("ProfileDebug", "savedUserInfo: $savedUserInfo")

        if (!savedToken.isNullOrBlank()) {
            _token.value = savedToken
            _isLoggedIn.value = true
            HttpClient.setAuthToken(savedToken)
            android.util.Log.d("ProfileDebug", "Restored logged in state: true")
        } else {
            android.util.Log.d("ProfileDebug", "No saved token, isLoggedIn: false")
        }

        if (!savedUserInfo.isNullOrBlank()) {
            try {
                _userInfo.value = gson.fromJson(savedUserInfo, UserInfo::class.java)
                android.util.Log.d("ProfileDebug", "Restored userInfo: ${_userInfo.value}")
            } catch (e: Exception) {
                android.util.Log.d("ProfileDebug", "Parse userInfo failed: ${e.message}")
                if (_isLoggedIn.value) {
                    refreshUserInfo()
                }
            }
        } else if (_isLoggedIn.value) {
            android.util.Log.d("ProfileDebug", "No saved userInfo but logged in, refresh...")
            refreshUserInfo()
        }

        // 异步监听 DataStore 变化
        scope.launch {
            dataStore.tokenFlow.collect { token ->
                android.util.Log.d("ProfileDebug", "tokenFlow changed: $token")
                _token.value = token
                _isLoggedIn.value = !token.isNullOrBlank()
                token?.let {
                    HttpClient.setAuthToken(it)
                }
            }
        }

        scope.launch {
            dataStore.userInfoFlow.collect { userInfoJson ->
                android.util.Log.d("ProfileDebug", "userInfoFlow changed: $userInfoJson")
                if (!userInfoJson.isNullOrBlank()) {
                    try {
                        _userInfo.value = gson.fromJson(userInfoJson, UserInfo::class.java)
                    } catch (e: Exception) {
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

    fun login(account: String, password: String) {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            android.util.Log.d("ProfileDebug", "=== login() called ===")

            when (val result = userRepository.login(account, password)) {
                is Result.Success -> {
                    val response = result.data
                    val userInfoData = response.userinfo
                    android.util.Log.d("ProfileDebug", "login success, token: ${userInfoData.token}")
                    android.util.Log.d("ProfileDebug", "login userInfo: $userInfoData")

                    // 先保存 token，再设置给 HttpClient
                    dataStore.setToken(userInfoData.token)
                    HttpClient.setAuthToken(userInfoData.token)

                    _token.value = userInfoData.token
                    _isLoggedIn.value = true
                    android.util.Log.d("ProfileDebug", "set isLoggedIn = true")

                    // 构建一个临时的 UserInfo 用于显示
                    val tempUserInfo = UserInfo(
                        uid = userInfoData.id,
                        image = null,
                        username = userInfoData.username,
                        nickname = null,
                        realname = null,
                        sex = null,
                        city = null,
                        score = null,
                        credit = null,
                        gold = null
                    )
                    _userInfo.value = tempUserInfo
                    val json = gson.toJson(tempUserInfo)
                    android.util.Log.d("ProfileDebug", "saving userInfo json: $json")
                    dataStore.setUserInfo(json)

                    // 登录成功后获取完整用户信息
                    when (val userResult = userRepository.getUserInfo()) {
                        is Result.Success -> {
                            android.util.Log.d("ProfileDebug", "getUserInfo success: ${userResult.data}")
                            _userInfo.value = userResult.data
                            dataStore.setUserInfo(gson.toJson(userResult.data))
                        }
                        is Result.Error -> {
                            android.util.Log.d("ProfileDebug", "getUserInfo failed, keep login userInfo")
                        }
                        is Result.Loading -> { }
                    }
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "登录失败"
                    android.util.Log.d("ProfileDebug", "login failed: ${_error.value}")
                }
                is Result.Loading -> { }
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        scope.launch {
            _isLoading.value = true
            userRepository.logout()
            _token.value = null
            _userInfo.value = null
            _isLoggedIn.value = false
            dataStore.clearToken()
            dataStore.setUserInfo(null)
            HttpClient.clearAuthToken()
            _isLoading.value = false
        }
    }

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
                is Result.Loading -> { }
            }
        }
    }

    fun setSelectCity(city: CityData) {
        scope.launch {
            _selectCity.value = city
            dataStore.setSelectCity(city)
            dataStore.setCitySelectHis(city)
        }
    }

    fun setLocation(locationArr: List<String>) {
        scope.launch {
            _location.value = locationArr
            dataStore.setLocation(gson.toJson(locationArr))
        }
    }

    fun setMoreMode(enabled: Boolean) {
        scope.launch {
            _isMoreMode.value = enabled
            dataStore.setMoreMode(enabled)
        }
    }

    fun setSearchPlayerHis(keyword: String) {
        scope.launch {
            dataStore.setSearchPlayerHis(keyword)
        }
    }

    fun clearSearchPlayerHis() {
        scope.launch {
            dataStore.clearSearchPlayerHis()
        }
    }

    fun clearError() {
        _error.value = null
    }

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

/**
 * CompositionLocal 用于提供 UserState 单例
 */
val LocalUserState = compositionLocalOf<UserState> { throw IllegalStateException("UserState not provided") }

/**
 * 创建 UserState 实例的工厂函数
 */
@Composable
fun rememberUserState(context: Context): UserState {
    return remember(context) { UserState(context) }
}
