package dev.mlzzen.kaiqiu.data.remote

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 网络请求客户端配置
 */
object HttpClient {

    const val BASE_URL = "https://kaiqiuwang.cc/xcx/public/index.php/api/"
    const val TIMEOUT_SECONDS = 30L
    private const val TAG = "HttpClient"

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
        if (body != null && body.contentType()?.toString() == "application/json") {
            // 读取原始 body 内容
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            val jsonBody = buffer.readUtf8()

            // 转换为 form-urlencoded
            val formBody = parseJsonToFormBody(jsonBody)
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
            // 简单解析 JSON 对象
            val jsonStr = json.trim()
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                val content = jsonStr.substring(1, jsonStr.length - 1)
                content.split(",").forEach { pair ->
                    val keyValue = pair.split(":")
                    if (keyValue.size == 2) {
                        val key = keyValue[0].trim().removeSurrounding("\"")
                        val value = keyValue[1].trim().removeSurrounding("\"")
                        formBuilder.add(key, value)
                    }
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

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
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
