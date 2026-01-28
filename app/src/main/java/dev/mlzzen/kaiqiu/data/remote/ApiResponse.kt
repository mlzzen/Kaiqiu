package dev.mlzzen.kaiqiu.data.remote

import com.google.gson.annotations.SerializedName

/**
 * API 统一响应包装类
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: T?
) {
    val isSuccess: Boolean
        get() = code == 1

    fun getOrNull(): T? = if (isSuccess) data else null

    fun getOrThrow(): T = data ?: throw ApiException(msg, code)
}

/**
 * API 异常
 */
class ApiException(
    override val message: String,
    val code: Int
) : Exception(message)

/**
 * 登录响应
 * API 返回结构: {"code":1,"data":{"userinfo":{"token":"...","id":"...","username":"..."}}}
 */
data class LoginResponse(
    @SerializedName("userinfo")
    val userinfo: LoginUserInfo
)

/**
 * 登录返回的用户信息
 */
data class LoginUserInfo(
    @SerializedName("token")
    val token: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String?
)

/**
 * 用户信息
 */
data class UserInfo(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("credit")
    val credit: String?,
    @SerializedName("gold")
    val gold: String?
)

/**
 * 高级用户信息
 * API 返回结构参考源项目 user.vue
 */
data class AdvProfile(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("realpic")
    val realpic: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("maxscore")
    val maxscore: String?,
    @SerializedName("maxScoreTheYear")
    val maxScoreTheYear: String?,
    @SerializedName("rank")
    val rank: String?,
    @SerializedName("scope")
    val scope: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("age")
    val age: String?,
    @SerializedName("resideprovince")
    val resideprovince: String?,
    @SerializedName("description")
    val description: String?
)

/**
 * 用户分数
 */
data class UserScores(
    @SerializedName("wins")
    val wins: Int,
    @SerializedName("losses")
    val losses: Int,
    @SerializedName("winRate")
    val winRate: String?,
    @SerializedName("totalGames")
    val totalGames: Int
)

/**
 * 比赛记录
 */
data class GameRecord(
    @SerializedName("gameid")
    val gameid: String,
    @SerializedName("eventTitle")
    val eventTitle: String?,
    @SerializedName("myScore")
    val myScore: String?,
    @SerializedName("opponentScore")
    val opponentScore: String?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("createTime")
    val createTime: String?
)

/**
 * 赛事历史
 * 参考源项目 eventHis.vue
 */
data class EventHistory(
    @SerializedName("eventid")
    val eventid: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("poster")
    val poster: String?,
    @SerializedName("province")
    val province: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("viewnum")
    val viewnum: String?,
    @SerializedName("membernum")
    val membernum: String?
)

/**
 * 用户关注
 */
data class UserFollow(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?
)

/**
 * 赛事项
 */
data class EventItem(
    @SerializedName("eventid")
    val eventid: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("startTime")
    val startTime: String?,
    @SerializedName("endTime")
    val endTime: String?,
    @SerializedName("arena")
    val arena: String?,
    @SerializedName("img")
    val img: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("poster")
    val poster: String?,
    @SerializedName("arena_name")
    val arenaName: String?,
    @SerializedName("viewnum")
    val viewnum: String?,
    @SerializedName("membernum")
    val membernum: String?,
    @SerializedName("grade")
    val grade: String?
)

/**
 * 用户项（搜索结果）
 */
data class UserItem(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("scores")
    val scores: UserScores?
)

/**
 * 排行榜响应
 */
data class RankListResponse(
    @SerializedName("list")
    val list: List<UserItem>,
    @SerializedName("total")
    val total: Int
)

/**
 * 签到响应
 */
data class SignResponse(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("integral")
    val integral: Int
)

/**
 * Top 列表项
 */
data class TopItem(
    @SerializedName("tid")
    val tid: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("viewnum")
    val viewnum: String?
)

/**
 * Top 列表响应
 */
data class TopListResponse(
    @SerializedName("list")
    val list: List<TopItem>,
    @SerializedName("total")
    val total: Int
)

/**
 * Top 100 数据项
 */
data class Top100Item(
    @SerializedName("uid")
    val uid: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("special")
    val special: String?
)

/**
 * Top 100 数据响应
 */
data class Top100Response(
    @SerializedName("list")
    val list: List<Top100Item>,
    @SerializedName("tabIndex")
    val tabIndex: Int,
    @SerializedName("th")
    val th: String?
)

/**
 * 球馆项
 */
data class ArenaItem(
    @SerializedName("arenaid")
    val arenaid: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("lat")
    val lat: String?,
    @SerializedName("lng")
    val lng: String?,
    @SerializedName("distance")
    val distance: String?
)

/**
 * 球馆详情
 */
data class ArenaDetail(
    @SerializedName("arenaid")
    val arenaid: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("lat")
    val lat: String?,
    @SerializedName("lng")
    val lng: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("intro")
    val intro: String?,
    @SerializedName("images")
    val images: List<String>?
)

/**
 * 比赛列表响应
 */
data class MatchListResponse(
    @SerializedName("data")
    val data: List<EventItem>
)

/**
 * 参赛记录列表响应
 * API 返回结构: {"code":1,"data":{"total":3,"per_page":10,"current_page":1,"last_page":1,"data":[...]}}
 */
data class EventHistoryResponse(
    @SerializedName("data")
    val data: EventHistoryData
)

data class EventHistoryData(
    @SerializedName("total")
    val total: String?,
    @SerializedName("per_page")
    val perPage: String?,
    @SerializedName("current_page")
    val currentPage: String?,
    @SerializedName("last_page")
    val lastPage: String?,
    @SerializedName("data")
    val data: List<EventHistory>?
)

/**
 * 游戏 ID 响应
 */
data class GameIdResponse(
    @SerializedName("gameid")
    val gameid: String?
)

/**
 * 比赛详情
 */
data class GameDetail(
    @SerializedName("gameid")
    val gameid: String,
    @SerializedName("eventTitle")
    val eventTitle: String?,
    @SerializedName("player1")
    val player1: PlayerInfo?,
    @SerializedName("player2")
    val player2: PlayerInfo?,
    @SerializedName("scores")
    val scores: List<String>,
    @SerializedName("result")
    val result: String?
)

/**
 * 球员信息
 */
data class PlayerInfo(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?
)

/**
 * 淘汰赛响应
 */
data class KnockoutResponse(
    @SerializedName("rounds")
    val rounds: List<RoundData>
)

/**
 * 轮次数据
 */
data class RoundData(
    @SerializedName("roundname")
    val roundname: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("games")
    val games: List<TtGameData>
)

/**
 * TT游戏数据
 */
data class TtGameData(
    @SerializedName("gameid")
    val gameid: String?,
    @SerializedName("uid1")
    val uid1: String?,
    @SerializedName("uid2")
    val uid2: String?,
    @SerializedName("username1")
    val username1: String?,
    @SerializedName("username2")
    val username2: String?,
    @SerializedName("result1")
    val result1: String?,
    @SerializedName("result2")
    val result2: String?,
    @SerializedName("gameRemark")
    val gameRemark: String?,
    @SerializedName("nickname1")
    val nickname1: String?,
    @SerializedName("nickname2")
    val nickname2: String?
)

/**
 * 比赛数据
 */
data class MatchData(
    @SerializedName("gameid")
    val gameid: String?,
    @SerializedName("uid1")
    val uid1: String?,
    @SerializedName("uid2")
    val uid2: String?,
    @SerializedName("nickname1")
    val nickname1: String?,
    @SerializedName("nickname2")
    val nickname2: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("winner")
    val winner: String?
)

/**
 * 小组赛响应
 */
data class GroupGamesResponse(
    @SerializedName("groups")
    val groups: List<GroupData>
)

/**
 * 分组数据
 */
data class GroupData(
    @SerializedName("groupName")
    val groupName: String,
    @SerializedName("names")
    val names: List<GroupPlayerName>,
    @SerializedName("scores")
    val scores: Map<String, String>,
    @SerializedName("groupid")
    val groupid: String?
)

/**
 * 分组球员名称
 */
data class GroupPlayerName(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("username")
    val username: String?,
    @SerializedName("sumScore")
    val sumScore: String?,
    @SerializedName("rank")
    val rank: String?
)

/**
 * 分组球员
 */
data class GroupPlayer(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("wins")
    val wins: Int,
    @SerializedName("losses")
    val losses: Int
)

/**
 * 分组比赛
 */
data class GroupMatch(
    @SerializedName("gameid")
    val gameid: String?,
    @SerializedName("uid1")
    val uid1: String?,
    @SerializedName("uid2")
    val uid2: String?,
    @SerializedName("score")
    val score: String?,
    @SerializedName("winner")
    val winner: String?
)

/**
 * 赛事详情响应
 */
data class EventDetailResponse(
    @SerializedName("items")
    val items: List<EventItemInfo>,
    @SerializedName("detail")
    val detail: EventDetail
)

/**
 * 赛项信息
 */
data class EventItemInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("match_type")
    val matchType: String?,
    @SerializedName("qualNum")
    val qualNum: Int?
)

/**
 * 赛事详情
 */
data class EventDetail(
    @SerializedName("eventid")
    val eventid: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("starttime")
    val starttime: String?,
    @SerializedName("endtime")
    val endtime: String?,
    @SerializedName("arena_name")
    val arenaName: String?,
    @SerializedName("poster")
    val poster: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("mobile")
    val mobile: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("lat")
    val lat: String?,
    @SerializedName("lng")
    val lng: String?,
    @SerializedName("startenrolltime")
    val startenrolltime: String?,
    @SerializedName("deadline")
    val deadline: String?,
    @SerializedName("weixin")
    val weixin: String?,
    @SerializedName("note")
    val note: String?,
    @SerializedName("detail")
    val detail: String?,
    @SerializedName("tagid")
    val tagid: String?,
    @SerializedName("shopid")
    val shopid: String?,
    @SerializedName("membernum")
    val membernum: String?,
    @SerializedName("viewnum")
    val viewnum: String?
)

/**
 * 成员详情
 */
data class MemberDetail(
    @SerializedName("uid")
    val uid: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("group")
    val group: String?,
    @SerializedName("seed")
    val seed: Int?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("newscore")
    val newscore: String?,
    @SerializedName("paid")
    val paid: Int?,
    @SerializedName("sex")
    val sex: Int?,
    @SerializedName("teamid")
    val teamid: String?,
    @SerializedName("mobile")
    val mobile: String?,
    @SerializedName("role")
    val role: Int?
)

/**
 * 参赛名单响应
 */
data class MemberListResponse(
    @SerializedName("list")
    val list: List<MemberDetail>
)

/**
 * 分组响应
 */
data class GroupsResponse(
    @SerializedName("groups")
    val groups: List<GroupData>
)

/**
 * 荣誉项
 */
data class HonorItem(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("ranking")
    val ranking: Int,
    @SerializedName("score")
    val score: String?
)

/**
 * 结果项
 */
data class ResultItem(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("ranking")
    val ranking: Int,
    @SerializedName("wins")
    val wins: Int,
    @SerializedName("losses")
    val losses: Int
)

/**
 * 分数变更
 */
data class ScoreChange(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("beforeScore")
    val beforeScore: String?,
    @SerializedName("afterScore")
    val afterScore: String?,
    @SerializedName("change")
    val change: String?
)

/**
 * 城市
 */
data class City(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)
