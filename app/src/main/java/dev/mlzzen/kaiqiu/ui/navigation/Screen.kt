package dev.mlzzen.kaiqiu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航目标
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    // Auth
    data object Login : Screen("login", "登录")

    // Main
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Profile : Screen("profile", "我的", Icons.Default.Person)
    data object Search : Screen("search", "搜索", Icons.Default.Search)
    data object Favorites : Screen("favorites", "收藏", Icons.Default.Favorite)

    // User
    data object UserDetail : Screen("user/{uid}", "用户详情") {
        fun createRoute(uid: String) = "user/$uid"
    }
    data object UserEvents : Screen("user/{uid}/events", "参赛记录") {
        fun createRoute(uid: String) = "user/$uid/events"
    }
    data object FollowedPlayers : Screen("followed_players", "关注列表")

    // Event
    data object EventList : Screen("events", "赛事列表")
    data object EventDetail : Screen("event/{eventid}", "赛事详情") {
        fun createRoute(eventid: String) = "event/$eventid"
    }
    data object EventMembers : Screen("event/{eventid}/members", "参赛名单") {
        fun createRoute(eventid: String) = "event/$eventid/members"
    }

    // Match
    data object MatchDetail : Screen("match/{gameid}", "比赛详情") {
        fun createRoute(gameid: String) = "match/$gameid"
    }
    data object ScoreEntry : Screen("score/{eventid}/{itemid}", "记分") {
        fun createRoute(eventid: String, itemid: String) = "score/$eventid/$itemid"
    }
    data object GroupScore : Screen("group_score/{eventid}/{itemid}", "小组记分") {
        fun createRoute(eventid: String, itemid: String) = "group_score/$eventid/$itemid"
    }

    // Search
    data object TopSearch : Screen("top_search", "排行榜")
    data object Top100 : Screen("top100", "Top100")
    data object Rank : Screen("rank", "排行")
    data object Gym : Screen("gym", "球馆")
    data object CitySelect : Screen("city_select", "城市选择")

    // About
    data object About : Screen("about", "关于")

    companion object {
        val bottomNavItems = listOf(Home, Search, Favorites, Profile)
    }
}
