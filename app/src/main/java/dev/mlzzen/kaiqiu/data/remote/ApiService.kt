package dev.mlzzen.kaiqiu.data.remote

import retrofit2.http.*

/**
 * API 服务接口
 */
interface ApiService {

    // ============ Login ============

    @POST("user/login")
    suspend fun login(@Body body: Map<String, String>): ApiResponse<LoginResponse>

    @POST("user/logout")
    suspend fun logout(): ApiResponse<Unit>

    // ============ User ============

    @POST("user/get_userinfo")
    suspend fun getUserInfo(@Body body: Map<String, String>): ApiResponse<UserInfo>

    @POST("user/adv_profile")
    suspend fun getAdvProfile(@Query("uid") uid: String): ApiResponse<AdvProfile>

    @GET("User/getGames")
    suspend fun getPageGamesByUid(
        @Query("uid") uid: String,
        @Query("page") page: Int
    ): ApiResponse<List<GameRecord>>

    @POST("center/events")
    suspend fun getMatchListHisByPage(@Body body: Map<String, String>): ApiResponse<EventHistoryResponse>

    @GET("User/followee")
    suspend fun goFolloweeByUid(@Query("uid") uid: String): ApiResponse<Unit>

    @GET("User/cancelFollowee")
    suspend fun goCancelFolloweeByUid(@Query("uid") uid: String): ApiResponse<Unit>

    @GET("User/getUserFolloweesList")
    suspend fun getUserFolloweesList(): ApiResponse<List<UserFollow>>

    @GET("User/getFolloweeEnrolledMatch")
    suspend fun getFolloweeEnrolledMatch(@Query("uid") uid: String): ApiResponse<List<EventItem>>

    @GET("user/lists")
    suspend fun getUserListPageByKey(@QueryMap params: Map<String, String>): ApiResponse<List<UserItem>>

    @POST("user/lists")
    suspend fun getPageUserRankList(@Body body: Map<String, String>): ApiResponse<RankListResponse>

    @POST("user/sign")
    suspend fun getDaySign(): ApiResponse<SignResponse>

    @GET("User/get_tags")
    suspend fun getUserTags(@QueryMap params: Map<String, String>): ApiResponse<List<String>>

    @GET("User/getUserScores")
    suspend fun getUserScores(@Query("uid") uid: String): ApiResponse<UserScores>

    // ============ Top ============

    @POST("Top/lists")
    suspend fun getTopView(@Body body: Map<String, String>): ApiResponse<TopListResponse>

    @GET("Top/getTop100Data")
    suspend fun getTop100Data(@QueryMap params: Map<String, String>): ApiResponse<Top100Response>

    // ============ Arena ============

    @POST("arena/lists")
    suspend fun getArenaListPageByKey(@Body body: Map<String, String>): ApiResponse<List<ArenaItem>>

    @GET("arena/detail")
    suspend fun getArenaDetail(@QueryMap params: Map<String, String>): ApiResponse<ArenaDetail>

    @GET("arena/match_list")
    suspend fun getArenaMatchList(@QueryMap params: Map<String, String>): ApiResponse<List<EventItem>>

    // ============ Match ============

    @POST("match/lists")
    suspend fun getMatchListByPage(@Body body: Map<String, String>): ApiResponse<MatchListResponse>

    @GET("Match/getGameidByUIDAndGroupID")
    suspend fun getGameidByUIDAndGroupID(@QueryMap params: Map<String, String>): ApiResponse<GameIdResponse>

    @GET("Match/getGameidByUIDAndMatchItem")
    suspend fun getGameidByUIDAndMatchItem(@QueryMap params: Map<String, String>): ApiResponse<GameIdResponse>

    @POST("Match/getGameDetail")
    suspend fun getGameDetailByGameid(@Body body: Map<String, String>): ApiResponse<GameDetail>

    @GET("Arrange/knockout")
    suspend fun getArrangeKnockout(@QueryMap params: Map<String, String>): ApiResponse<KnockoutResponse>

    @GET("Match/update_tt_score")
    suspend fun updateTtScore(@QueryMap params: Map<String, String>): ApiResponse<Unit>

    @GET("Match/update_score")
    suspend fun updateScore(@QueryMap params: Map<String, String>): ApiResponse<Unit>

    @GET("Match/init_h_games")
    suspend fun getGroupGames(@QueryMap params: Map<String, String>): ApiResponse<GroupGamesResponse>

    // ============ Event ============

    @GET("enter/detail")
    suspend fun getEventDetaiByIdAndLocation(@QueryMap params: Map<String, String>): ApiResponse<EventDetailResponse>

    @GET("enter/get_member_detail")
    suspend fun getMemberDetail(@QueryMap params: Map<String, String>): ApiResponse<MemberListResponse>

    @GET("Match/get_groups")
    suspend fun getGroups(@QueryMap params: Map<String, String>): ApiResponse<GroupsResponse>

    @GET("Match/get_all_honors")
    suspend fun getAllHonors(@QueryMap params: Map<String, String>): ApiResponse<List<HonorItem>>

    @GET("Match/getResult")
    suspend fun getAllResult(@QueryMap params: Map<String, String>): ApiResponse<List<ResultItem>>

    @GET("Match/getScoreChange2")
    suspend fun getScoreChangeByEventid(@Query("eventid") eventid: String): ApiResponse<List<ScoreChange>>

    // ============ Public ============

    @GET("publicc/GetCities")
    suspend fun getCities(): ApiResponse<List<City>>
}
