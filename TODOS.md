# Kaiqiu 迁移计划

将 `kaiqiu-app-uni` uni-app 项目迁移到 Android Kotlin Compose 架构

## 执行准则
按阶段按顺序完成每一步，完成每一步后，先gradle同步，再尝试编译，编译通过了再更新TODOS.md，再把commit message 输出出来，我先尝试成功了，我自己再去手动git commit

## 项目信息
- **源项目**: D:\workspace\kaiqiu-app-uni
- **目标项目**: D:\workspace\Kaiqiu
- **技术栈**: uni-app (Vue 3) → Android Kotlin + Jetpack Compose

---

## 阶段一：项目基础架构

- [x] 1.1 配置项目签名和发布配置 (keystore)
- [x] 1.2 添加网络权限和必要 Android 权限
- [x] 1.3 配置 Retrofit/OkHttp 网络库
- [x] 1.4 配置 Kotlin 序列化 (Kotlinx Serialization)
- [x] 1.5 配置 Compose Navigation
- [x] 1.6 配置 Coil 图片加载库

---

## 阶段二：数据层迁移

- [x] 2.1 创建基础网络请求封装 (HttpClient/Dio -> Retrofit)
- [x] 2.2 创建 API 响应包装类 (Result<T>)
- [x] 2.3 迁移 login API (api/login.js)
- [x] 2.4 迁移 user API (api/user.js)
- [x] 2.5 迁移 top API (api/top.js)
- [x] 2.6 迁移 arena API (api/arena.js)
- [x] 2.7 迁移 match API (api/match.js)
- [x] 2.8 迁移 event API (api/event.js)
- [x] 2.9 迁移 publicc API (api/publicc.js)

---

## 阶段三：状态管理迁移

- [x] 3.1 创建 AppDataStore (DataStore 持久化存储)
- [x] 3.2 迁移 Pinia user store -> UserState
- [x] 3.3 创建 UserRepository (业务层仓库)

---

## 阶段四：页面迁移

### 4.1 登录模块
- [x] 4.1.1 迁移 login/login.vue → LoginScreen.kt

### 4.2 首页模块
- [x] 4.2.1 迁移 main/main.vue → HomeScreen.kt
- [x] 4.2.2 迁移 main/selectCityPage.vue → CitySelectScreen.kt
- [x] 4.2.3 迁移 main/about.vue → AboutScreen.kt
- [x] 4.2.4 迁移 main/my.vue → ProfileScreen.kt

### 4.3 用户模块
- [x] 4.3.1 迁移 user/user.vue → UserDetailScreen.kt
- [x] 4.3.2 迁移 user/eventHis.vue → UserEventsScreen.kt
- [x] 4.3.3 迁移 user/followPlayers.vue → FollowedPlayersScreen.kt

### 4.4 搜索模块
- [x] 4.4.1 迁移 search/searchIndex.vue → SearchScreen.kt
- [x] 4.4.2 迁移 search/topSearch.vue → TopSearchScreen.kt
- [x] 4.4.3 迁移 search/top100Data.vue → Top100Screen.kt
- [x] 4.4.4 迁移 search/rank.vue → RankScreen.kt
- [x] 4.4.5 迁移 search/gym.vue → GymScreen.kt

### 4.5 赛事模块
- [x] 4.5.1 迁移 event/eventMain.vue → EventListScreen.kt
- [x] 4.5.2 迁移 event/memberList.vue → EventMembersScreen.kt
- [x] 4.5.3 迁移 event/components/* → Event 组件
- [x] 4.5.4 修复 EventDetailScreen 赛事信息显示问题（添加 detail 字段支持）

### 4.6 比赛模块
- [x] 4.6.1 迁移 match/matchInfo.vue → MatchDetailScreen.kt

### 4.7 记分模块
- [x] 4.7.1 迁移 setScore/ttGame.vue → ScoreEntryScreen.kt
- [x] 4.7.2 迁移 setScore/group.vue → GroupScoreScreen.kt

---

## 阶段五：组件迁移

由于大部分组件已内嵌到各页面中，此阶段主要验证组件功能的完整性：
- [x] 5.1 检查并完善通用组件 (已集成到页面)
- [x] 5.2 检查并完善赛事组件 (已集成到 EventDetailScreen)
- [x] 5.3 检查并完善比赛组件 (已集成到 MatchDetailScreen)

---

## 阶段六：工具函数迁移

- [ ] 6.1 迁移 request 封装 (utils/request/)
- [ ] 6.2 迁移 goPage.js 路由工具
- [ ] 6.3 迁移 jumpMap.js 地图工具

---

## 阶段七：资源迁移

- [ ] 7.1 迁移 static/ 图片资源
- [ ] 7.2 迁移 unocss 样式到 Compose Theme
- [ ] 7.3 迁移 App.vue 全局样式

---

## 阶段八：测试与优化

- [ ] 8.1 运行单元测试
- [ ] 8.2 运行 lint 检查
- [ ] 8.3 性能优化
- [ ] 8.4 打包测试 (assembleRelease)

---

## 页面优先级排序

| 优先级 | 页面 | 说明 |
|--------|------|------|
| P0 | LoginScreen | 登录入口 |
| P0 | HomeScreen | 主页面 |
| P0 | ProfileScreen | 个人中心 |
| P1 | SearchScreen | 搜索功能 |
| P1 | UserDetailScreen | 用户详情 |
| P2 | EventListScreen | 赛事列表 |
| P2 | EventDetailScreen | 赛事详情 |
| P3 | 其他页面 | 详细功能 |

---

## 注意事项

1. uni-app API 返回格式与 Android 差异需适配
2. 页面路由改为 Compose Navigation
3. 状态管理改用 Kotlin Flow + DataStore
4. 样式系统从 UnoCSS 迁移到 Compose Material 3
