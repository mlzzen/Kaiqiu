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
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("userInfo")
    val userInfo: UserInfo
)

/**
 * 用户信息
 */
data class UserInfo(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("city")
    val city: String?
)

/**
 * 高级用户信息
 */
data class AdvProfile(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("realname")
    val realname: String?,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("scores")
    val scores: UserScores?,
    @SerializedName("tags")
    val tags: List<String>?
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
 */
data class EventHistory(
    @SerializedName("eventid")
    val eventid: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("startTime")
    val startTime: String?,
    @SerializedName("endTime")
    val endTime: String?,
    @SerializedName("status")
    val status: String?
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
    val img: String?
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
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("images")
    val images: List<String>?
)

/**
 * 比赛列表响应
 */
data class MatchListResponse(
    @SerializedName("list")
    val list: List<EventItem>,
    @SerializedName("total")
    val total: Int
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
    @SerializedName("name")
    val name: String,
    @SerializedName("matches")
    val matches: List<MatchData>
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
    @SerializedName("players")
    val players: List<GroupPlayer>,
    @SerializedName("games")
    val games: List<GroupMatch>
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
 * 赛事详情
 */
data class EventDetail(
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
    @SerializedName("status")
    val status: String?
)

/**
 * 成员详情
 */
data class MemberDetail(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("group")
    val group: String?,
    @SerializedName("seed")
    val seed: Int?
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
