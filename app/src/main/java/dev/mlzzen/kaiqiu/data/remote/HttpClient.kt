package dev.mlzzen.kaiqiu.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络请求客户端配置
 */
object HttpClient {

    const val BASE_URL = "https://kaiqiuwang.cc/xcx/public/index.php/api/"
    const val TIMEOUT_SECONDS = 30L
    private const val TAG = "HttpClient"
    private const val TAG_GSON = "GsonParse"
    private val gson = Gson()

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.newBuilder()
            .apply {
                authToken?.let { token ->
                    addHeader("token", token)
                }
            }
            .build()

        // 转换 JSON body 为 form-urlencoded
        val body = originalRequest.body
        if (body != null && body.contentType()?.toString()?.contains("json") == true) {
            // 读取原始 body 内容
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            val jsonBody = buffer.readUtf8()
            Log.d(TAG, "Original JSON body: $jsonBody")

            // 转换为 form-urlencoded
            val formBody = parseJsonToFormBody(jsonBody)
            Log.d(TAG, "Converted form body: ${formBody.size} params")

            val newRequest = requestWithToken.newBuilder()
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

        chain.proceed(requestWithToken)
    }

    private fun parseJsonToFormBody(json: String): FormBody {
        val formBuilder = FormBody.Builder()
        try {
            // 移除首尾的 { 和 }
            var jsonStr = json.trim()
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                jsonStr = jsonStr.substring(1, jsonStr.length - 1)
            }

            // 按逗号分割，处理嵌套括号
            var i = 0
            val pairs = mutableListOf<String>()
            var current = StringBuilder()
            var braceDepth = 0

            jsonStr.forEach { char ->
                when (char) {
                    '{' -> { braceDepth++; current.append(char) }
                    '}' -> { braceDepth--; current.append(char) }
                    ',' -> {
                        if (braceDepth == 0) {
                            pairs.add(current.toString().trim())
                            current = StringBuilder()
                        } else {
                            current.append(char)
                        }
                    }
                    else -> current.append(char)
                }
            }
            if (current.isNotEmpty()) {
                pairs.add(current.toString().trim())
            }

            pairs.forEach { pair ->
                val keyValue = pair.split(":")
                if (keyValue.size >= 2) {
                    val key = keyValue[0].trim().removeSurrounding("\"")
                    val value = keyValue.drop(1).joinToString(":").trim().removeSurrounding("\"")
                    formBuilder.add(key, value)
                    Log.d(TAG, "Added form param: $key = $value")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON body", e)
        }
        return formBuilder.build()
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Gson 解析日志拦截器
     * 在响应返回后尝试用 Gson 解析，打印解析日志
     */
    private val gsonLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer
        val jsonString = buffer?.clone()?.readUtf8() ?: ""

        if (jsonString.isNotEmpty()) {
            Log.d(TAG_GSON, "=== Gson 解析开始 ===")
            Log.d(TAG_GSON, "URL: ${request.url}")
            Log.d(TAG_GSON, "原始 JSON (前500字符): ${jsonString.take(500)}")

            // 首先检查是否是纯数组响应（非 ApiResponse 包装）
            val trimmedJson = jsonString.trim()
            if (trimmedJson.startsWith("[")) {
                Log.d(TAG_GSON, "检测到纯数组响应，无需通过 ApiResponse 包装解析")
                Log.d(TAG_GSON, "=== Gson 解析结束 ===")
                return@Interceptor response
            }

            // 尝试解析为 ApiResponse
            try {
                val apiResponse = gson.fromJson(jsonString, ApiResponse::class.java)
                Log.d(TAG_GSON, "ApiResponse.code: ${apiResponse.code}, msg: ${apiResponse.msg}")

                if (apiResponse.data == null) {
                    Log.d(TAG_GSON, "ApiResponse.data 为 null")
                } else {
                    Log.d(TAG_GSON, "ApiResponse.data 类型: ${apiResponse.data::class.simpleName}")

                    // 如果 data 是数字类型
                    if (apiResponse.data is Number) {
                        Log.d(TAG_GSON, "data 是数字类型: ${apiResponse.data}")
                    }

                    // 安全地检查 data 是否是数组
                    try {
                        val dataJson = gson.toJson(apiResponse.data)
                        if (dataJson.trim().startsWith("[")) {
                            Log.d(TAG_GSON, "data 是数组类型，不需要嵌套解析")
                        } else {
                            // 尝试将 data 解析为 EventHistoryResponse
                            try {
                                val eventHistoryResponse = gson.fromJson(dataJson, EventHistoryResponse::class.java)
                                val dataType = eventHistoryResponse.data?.javaClass?.simpleName ?: "null"
                                Log.d(TAG_GSON, "EventHistoryResponse.data 类型: $dataType")
                            } catch (e: Exception) {
                                Log.e(TAG_GSON, "EventHistoryResponse 解析失败: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG_GSON, "检查 data 类型时出错: ${e.message}")
                    }
                }
                Log.d(TAG_GSON, "Gson 解析成功")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG_GSON, "Gson 解析失败!!!")
                Log.e(TAG_GSON, "错误类型: ${e.javaClass.simpleName}")
                Log.e(TAG_GSON, "错误信息: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG_GSON, "Gson 解析异常: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }

            Log.d(TAG_GSON, "=== Gson 解析结束 ===")
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(gsonLoggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
