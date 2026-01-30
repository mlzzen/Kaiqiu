package dev.mlzzen.kaiqiu.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.mlzzen.kaiqiu.data.remote.*
import dev.mlzzen.kaiqiu.data.remote.HttpClient

/**
 * 用户相关 API 仓库
 */
class UserRepository(
    private val api: ApiService = HttpClient.api
) {
    private val gson = Gson()

    suspend fun login(account: String, password: String): Result<LoginResponse> {
        return Result.safeApiCall {
            api.login(mapOf("account" to account, "password" to password))
                .getOrThrow()
        }
    }

    suspend fun logout(): Result<Unit> {
        return Result.safeApiCall {
            api.logout()
            Unit
        }
    }

    suspend fun getUserInfo(): Result<UserInfo> {
        return Result.safeApiCall {
            api.getUserInfo(emptyMap()).getOrThrow()
        }
    }

    suspend fun getAdvProfile(uid: String): Result<AdvProfile> {
        return Result.safeApiCall {
            api.getAdvProfile(uid).getOrThrow()
        }
    }

    suspend fun getPageGamesByUid(uid: String, page: Int): Result<GameRecordsResponse> {
        return Result.safeApiCall {
            api.getPageGamesByUid(uid, page).getOrThrow()
        }
    }

    suspend fun getMatchListHisByPage(page: Int): Result<List<EventHistory>> {
        android.util.Log.d("UserEventsAPI", "=== getMatchListHisByPage ===")
        return Result.safeApiCall {
            android.util.Log.d("UserEventsAPI", "calling API...")
            try {
                // 使用原始 Response 拿到 JSON 字符串
                val rawResponse = api.getMatchListHisByPageRaw(mapOf("page" to page.toString(), "index" to "0"))
                val jsonString = rawResponse.string()

                android.util.Log.d("UserEventsAPI", "raw JSON: $jsonString")

                // 解析外层 ApiResponse
                val apiResponseType = object : TypeToken<ApiResponse<*>>() {}.type
                val apiResponse: ApiResponse<*> = gson.fromJson(jsonString, apiResponseType)

                android.util.Log.d("UserEventsAPI", "apiResponse.code=${apiResponse.code}")

                if (apiResponse.isSuccess) {
                    // 解析内层 data 为 EventHistoryData
                    val dataJson = gson.toJson(apiResponse.data)
                    android.util.Log.d("UserEventsAPI", "dataJson: $dataJson")

                    val eventHistoryData: EventHistoryData = gson.fromJson(dataJson, EventHistoryData::class.java)
                    val list = eventHistoryData.data ?: emptyList()

                    android.util.Log.d("UserEventsAPI", "events count: ${list.size}")
                    list
                } else {
                    android.util.Log.d("UserEventsAPI", "API failed: ${apiResponse.msg}")
                    emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.d("UserEventsAPI", "Exception during API call: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun followUser(uid: String): Result<Unit> {
        return Result.safeApiCall {
            api.goFolloweeByUid(uid).getOrThrow()
            Unit
        }
    }

    suspend fun unfollowUser(uid: String): Result<Unit> {
        return Result.safeApiCall {
            api.goCancelFolloweeByUid(uid).getOrThrow()
            Unit
        }
    }

    suspend fun getUserFolloweesList(): Result<List<UserFollow>> {
        return Result.safeApiCall {
            api.getUserFolloweesList().getOrThrow().followeesList ?: emptyList()
        }
    }

    suspend fun getFolloweeEnrolledMatch(uid: String): Result<List<EventItem>> {
        return Result.safeApiCall {
            api.getFolloweeEnrolledMatch(uid).getOrThrow()
        }
    }

    suspend fun searchUsers(keyword: String, page: Int = 1): Result<List<UserItem>> {
        return Result.safeApiCall {
            api.getUserListPageByKey(
                mapOf("keyword" to keyword, "page" to page.toString())
            ).getOrThrow()
        }
    }

    suspend fun getUserRankList(
        city: String,
        page: Int = 1,
        sort: String = "2"
    ): Result<RankListResponse> {
        return Result.safeApiCall {
            api.getPageUserRankList(
                mapOf(
                    "city" to city,
                    "page" to page.toString(),
                    "sort" to sort
                )
            ).getOrThrow()
        }
    }

    suspend fun daySign(): Result<SignResponse> {
        return Result.safeApiCall {
            api.getDaySign().getOrThrow()
        }
    }

    suspend fun getUserTags(uid: String, limitByCount: Int = 6): Result<List<String>> {
        return Result.safeApiCall {
            api.getUserTags(
                mapOf(
                    "uid" to uid,
                    "limitByCount" to limitByCount.toString(),
                    "getNegative" to "false"
                )
            ).getOrThrow()
        }
    }

    suspend fun getUserScores(uid: String): Result<List<ScoreHistory>> {
        return Result.safeApiCall {
            api.getUserScores(uid).getOrThrow()
        }
    }
}

/**
 * Top 排行相关 API 仓库
 */
class TopRepository(
    private val api: ApiService = HttpClient.api
) {
    suspend fun getTopView(city: String): Result<TopListResponse> {
        return Result.safeApiCall {
            api.getTopView(mapOf("city" to city)).getOrThrow()
        }
    }

    suspend fun getTop100Data(
        city: String,
        tabIndex: Int = 1,
        tid: String = "2"
    ): Result<Top100Response> {
        return Result.safeApiCall {
            api.getTop100Data(
                mapOf(
                    "city" to city,
                    "tabIndex" to tabIndex.toString(),
                    "tid" to tid
                )
            ).getOrThrow()
        }
    }
}

/**
 * 球馆相关 API 仓库
 */
class ArenaRepository(
    private val api: ApiService = HttpClient.api
) {
    suspend fun getArenaList(
        city: String,
        page: Int = 1,
        keyword: String? = null
    ): Result<List<ArenaItem>> {
        return Result.safeApiCall {
            val body = mutableMapOf<String, String>("city" to city, "page" to page.toString())
            keyword?.let { body["keyword"] = it }
            api.getArenaListPageByKey(body).getOrThrow()
        }
    }

    suspend fun getArenaDetail(arenaid: String): Result<ArenaDetail> {
        return Result.safeApiCall {
            api.getArenaDetail(mapOf("arenaid" to arenaid)).getOrThrow()
        }
    }

    suspend fun getArenaMatchList(arenaid: String): Result<List<EventItem>> {
        return Result.safeApiCall {
            api.getArenaMatchList(mapOf("arenaid" to arenaid)).getOrThrow()
        }
    }
}

/**
 * 比赛相关 API 仓库
 */
class MatchRepository(
    private val api: ApiService = HttpClient.api
) {
    suspend fun getMatchList(
        city: String,
        page: Int = 1,
        keyword: String? = null
    ): Result<MatchListResponse> {
        return Result.safeApiCall {
            val body = mutableMapOf<String, String>("city" to city, "page" to page.toString())
            keyword?.let { body["search"] = "1"; body["eventTitle"] = it }
            api.getMatchListByPage(body).getOrThrow()
        }
    }

    suspend fun getGameidByUIDAndGroupID(
        groupid: String,
        uid1: String,
        uid2: String
    ): Result<String?> {
        return Result.safeApiCall {
            api.getGameidByUIDAndGroupID(
                mapOf(
                    "groupid" to groupid,
                    "uid1" to uid1,
                    "uid2" to uid2
                )
            ).getOrThrow()?.gameid
        }
    }

    suspend fun getGameidByUIDAndMatchItem(
        eventid: String,
        itemid: String,
        uid1: String,
        uid2: String
    ): Result<String?> {
        return Result.safeApiCall {
            api.getGameidByUIDAndMatchItem(
                mapOf(
                    "eventid" to eventid,
                    "itemid" to itemid,
                    "uid1" to uid1,
                    "uid2" to uid2
                )
            ).getOrThrow()?.gameid
        }
    }

    suspend fun getGameDetail(gameid: String): Result<GameDetail> {
        return Result.safeApiCall {
            api.getGameDetailByGameid(mapOf("gameid" to gameid)).getOrThrow()
        }
    }

    suspend fun getKnockout(eventid: String, itemid: String): Result<KnockoutResponse> {
        return Result.safeApiCall {
            api.getArrangeKnockout(
                mapOf("eventid" to eventid, "itemid" to itemid)
            ).getOrThrow()
        }
    }

    suspend fun updateTtScore(
        groupid: String,
        uid1: String,
        uid2: String,
        score: String,
        eventid: String,
        itemid: String,
        gameid: String
    ): Result<Unit> {
        return Result.safeApiCall {
            api.updateTtScore(
                mapOf(
                    "groupid" to groupid,
                    "uid1" to uid1,
                    "uid2" to uid2,
                    "score" to score,
                    "eventid" to eventid,
                    "itemid" to itemid,
                    "gameid" to gameid
                )
            ).getOrThrow()
            Unit
        }
    }

    suspend fun updateScore(
        groupid: String,
        uid1: String,
        uid2: String,
        score: String,
        eventid: String,
        itemid: String
    ): Result<Unit> {
        return Result.safeApiCall {
            api.updateScore(
                mapOf(
                    "groupid" to groupid,
                    "uid1" to uid1,
                    "uid2" to uid2,
                    "score" to score,
                    "eventid" to eventid,
                    "itemid" to itemid
                )
            ).getOrThrow()
            Unit
        }
    }

    suspend fun getGroupGames(eventid: String, itemid: String): Result<List<GroupData>> {
        return Result.safeApiCall {
            api.getGroupGames(
                mapOf("eventid" to eventid, "itemid" to itemid)
            ).getOrThrow()
        }
    }
}

/**
 * 比赛相关 API 仓库
 */
class EventRepository(
    private val api: ApiService = HttpClient.api
) {
    suspend fun getEventDetail(eventid: String): Result<EventDetailResponse> {
        return Result.safeApiCall {
            api.getEventDetaiByIdAndLocation(mapOf("id" to eventid)).getOrThrow()
        }
    }

    suspend fun getMemberDetail(matchId: String, itemId: String): Result<List<MemberDetail>> {
        return Result.safeApiCall {
            api.getMemberDetail(mapOf("match_id" to matchId, "id" to itemId)).getOrThrow().list
        }
    }

    suspend fun getGroups(eventid: String, itemid: String): Result<GroupsResponse> {
        return Result.safeApiCall {
            api.getGroups(mapOf("eventid" to eventid, "itemid" to itemid)).getOrThrow()
        }
    }

    suspend fun getAllHonors(eventid: String, itemid: String): Result<List<HonorItem>> {
        return Result.safeApiCall {
            api.getAllHonors(mapOf("eventid" to eventid, "itemid" to itemid)).getOrThrow()
        }
    }

    suspend fun getAllResult(eventid: String, itemid: String): Result<List<ResultItem>> {
        return Result.safeApiCall {
            api.getAllResult(mapOf("eventid" to eventid, "itemid" to itemid)).getOrThrow()
        }
    }

    suspend fun getScoreChange(eventid: String): Result<ScoreChangeResponse> {
        return Result.safeApiCall {
            api.getScoreChange(eventid).getOrThrow()
        }
    }
}

/**
 * 公共 API 仓库
 */
class PublicRepository(
    private val api: ApiService = HttpClient.api
) {
    suspend fun getCities(): Result<List<City>> {
        return Result.safeApiCall {
            api.getCities().getOrThrow()
        }
    }
}
