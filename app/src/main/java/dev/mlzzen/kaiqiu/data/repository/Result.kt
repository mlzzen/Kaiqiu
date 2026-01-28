package dev.mlzzen.kaiqiu.data.repository

import dev.mlzzen.kaiqiu.data.remote.ApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * API 结果包装类
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Loading -> Loading
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(exception)
        is Loading -> Loading
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading

        suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
            return try {
                Success(apiCall())
            } catch (e: ApiException) {
                Error(e)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}

/**
 * Flow 结果扩展
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    var flow: Flow<Result<T>> = this.map { value: T ->
        @Suppress("USELESS_CAST")
        Result.Success(value) as Result<T>
    }
    flow = flow.catch { e: Throwable ->
        emit(Result.Error(e))
    }
    return flow
}
