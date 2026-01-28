package dev.mlzzen.kaiqiu.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kaiqiu_preferences")

/**
 * 应用偏好设置 DataStore
 */
class AppDataStore(private val context: Context) {

    companion object {
        // Auth
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_INFO = stringPreferencesKey("user_info")

        // Location
        private val KEY_LOCATION = stringPreferencesKey("user_location")
        private val KEY_SELECT_CITY = stringPreferencesKey("select_city")
        private val KEY_CITY_SELECT_HIS = stringPreferencesKey("city_select_his")

        // Settings
        private val KEY_IS_MORE_MODE = booleanPreferencesKey("is_more_mode")
        private val KEY_SEARCH_PLAYER_HIS = stringPreferencesKey("search_player_his")
    }

    private val dataStore = context.dataStore

    // 同步获取 token（阻塞式，用于初始化检查）
    fun getTokenSync(): String? = runBlocking(Dispatchers.IO) {
        try {
            dataStore.data.first()[KEY_TOKEN]
        } catch (e: Exception) {
            null
        }
    }

    // ============ Token ============

    val tokenFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_TOKEN]
        }

    suspend fun setToken(token: String?) {
        dataStore.edit { preferences ->
            if (token != null) {
                preferences[KEY_TOKEN] = token
            } else {
                preferences.remove(KEY_TOKEN)
            }
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
        }
    }

    // ============ User Info ============

    val userInfoFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_USER_INFO]
        }

    suspend fun setUserInfo(userInfoJson: String?) {
        dataStore.edit { preferences ->
            if (userInfoJson != null) {
                preferences[KEY_USER_INFO] = userInfoJson
            } else {
                preferences.remove(KEY_USER_INFO)
            }
        }
    }

    // ============ Location ============

    val locationFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LOCATION]
        }

    suspend fun setLocation(locationJson: String?) {
        dataStore.edit { preferences ->
            if (locationJson != null) {
                preferences[KEY_LOCATION] = locationJson
            } else {
                preferences.remove(KEY_LOCATION)
            }
        }
    }

    // ============ Select City ============

    val selectCityFlow: Flow<CityData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[KEY_SELECT_CITY] ?: """{"id":"1","name":"北京市"}"""
            try {
                parseCityData(json)
            } catch (e: Exception) {
                CityData("1", "北京市")
            }
        }

    suspend fun setSelectCity(city: CityData) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECT_CITY] = city.toJson()
        }
    }

    // ============ City Select History ============

    val citySelectHisFlow: Flow<List<CityData>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[KEY_CITY_SELECT_HIS] ?: "[]"
            parseCityList(json)
        }

    suspend fun setCitySelectHis(city: CityData) {
        dataStore.edit { preferences ->
            val currentJson = preferences[KEY_CITY_SELECT_HIS] ?: "[]"
            val currentList = parseCityList(currentJson).toMutableList()

            // 去重
            currentList.removeAll { it.id == city.id }
            // 添加到最前面
            currentList.add(0, city)
            // 限制最大记录数 5
            val trimmedList = currentList.take(5)

            preferences[KEY_CITY_SELECT_HIS] = trimmedList.joinToString(",") { it.toJson() }
        }
    }

    // ============ More Mode ============

    val isMoreModeFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_IS_MORE_MODE] ?: false
        }

    suspend fun setMoreMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_MORE_MODE] = enabled
        }
    }

    // ============ Search History ============

    val searchPlayerHisFlow: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[KEY_SEARCH_PLAYER_HIS] ?: "[]"
            parseStringList(json)
        }

    suspend fun setSearchPlayerHis(keyword: String) {
        if (keyword.isBlank()) return
        dataStore.edit { preferences ->
            val currentJson = preferences[KEY_SEARCH_PLAYER_HIS] ?: "[]"
            val currentList = parseStringList(currentJson).toMutableList()

            // 去重
            currentList.removeAll { it == keyword }
            // 添加到最前面
            currentList.add(0, keyword)
            // 限制最大记录数 20
            val trimmedList = currentList.take(20)

            preferences[KEY_SEARCH_PLAYER_HIS] = trimmedList.toJsonStringList()
        }
    }

    suspend fun clearSearchPlayerHis() {
        dataStore.edit { preferences ->
            preferences[KEY_SEARCH_PLAYER_HIS] = "[]"
        }
    }

    // ============ Clear All ============

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // ============ Helpers ============

    private fun parseCityData(json: String): CityData {
        // 简单解析 {"id":"1","name":"北京市"}
        val id = json.substringAfter("\"id\":\"").substringBefore("\"")
        val name = json.substringAfter("\"name\":\"").substringBefore("\"")
        return CityData(id, name)
    }

    private fun parseCityList(json: String): List<CityData> {
        if (json == "[]" || json.isBlank()) return emptyList()
        val items = json.split("},{")
        return items.map { item ->
            val cleanItem = item.replace("{", "").replace("}", "")
            val id = cleanItem.substringAfter("\"id\":\"").substringBefore("\"")
            val name = cleanItem.substringAfter("\"name\":\"").substringBefore("\"")
            CityData(id, name)
        }
    }

    private fun parseStringList(json: String): List<String> {
        if (json == "[]" || json.isBlank()) return emptyList()
        return json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }

    private fun List<String>.toJsonStringList(): String {
        return this.joinToString(",", "[", "]") { "\"$it\"" }
    }
}

/**
 * 城市数据
 */
data class CityData(
    val id: String,
    val name: String
) {
    fun toJson(): String = """{"id":"$id","name":"$name"}"""
}
