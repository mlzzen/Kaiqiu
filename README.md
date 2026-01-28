# Kaiqiu Android

这是 [kaiqiu-app-uni](https://github.com/xiaojia001/kaiqiu-app-uni) 项目的 Android 原生实现版本。

## 关于

本项目是将原 uni-app (Vue 3) 项目迁移到 Android 平台的原生应用，采用 **Kotlin + Jetpack Compose** 技术栈开发。

本项目完全使用 MiniMax 2.1 从零开发，包含项目初始化、架构设计、代码编写、调试优化等全部流程。

## 致谢

非常感谢 [kaiqiu-app-uni](https://github.com/xiaojia001/kaiqiu-app-uni) 项目提供的思路和参考，本项目的很多设计、API 对接、页面逻辑都参考了该开源项目。

原项目是一个优秀的乒乓球约球赛事平台，功能丰富，架构清晰。在此特别感谢作者的开源分享。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **网络**: Retrofit + OkHttp
- **图片**: Coil
- **状态管理**: Compose State + DataStore
- **导航**: Compose Navigation

## 编译

```bash
./gradlew assembleDebug    # Debug 构建
./gradlew assembleRelease  # Release 构建
```

## API 文档

**Base URL**: `https://kaiqiuwang.cc/xcx/public/index.php/api/`

### Login

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `user/login` | 用户登录 |
| POST | `user/logout` | 用户退出 |

### User

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `user/get_userinfo` | 获取用户信息 |
| POST | `user/adv_profile` | 获取高级用户资料 (Query: uid) |
| GET | `User/getGames` | 获取用户比赛记录 (Query: uid, page) |
| POST | `center/events` | 获取用户赛事历史 |
| GET | `User/followee` | 关注用户 (Query: uid) |
| GET | `User/cancelFollowee` | 取消关注 (Query: uid) |
| GET | `User/getUserFolloweesList` | 获取关注列表 |
| GET | `User/getFolloweeEnrolledMatch` | 获取关注用户的参赛列表 (Query: uid) |
| GET | `user/lists` | 搜索用户 |
| POST | `user/lists` | 获取用户排行榜 |
| POST | `user/sign` | 每日签到 |
| GET | `User/get_tags` | 获取用户标签 |
| GET | `User/getUserScores` | 获取用户得分 (Query: uid) |

### Top

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `Top/lists` | 获取热门榜单 |
| GET | `Top/getTop100Data` | 获取 Top 100 数据 |

### Arena

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `arena/lists` | 获取球馆列表 |
| GET | `arena/detail` | 获取球馆详情 |
| GET | `arena/match_list` | 获取球馆赛事列表 |

### Match

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `match/lists` | 获取赛事列表 |
| GET | `Match/getGameidByUIDAndGroupID` | 根据分组获取比赛 ID |
| GET | `Match/getGameidByUIDAndMatchItem` | 根据赛事项目获取比赛 ID |
| POST | `Match/getGameDetail` | 获取比赛详情 |
| GET | `Arrange/knockout` | 获取淘汰赛安排 |
| GET | `Match/update_tt_score` | 更新乒乓球比赛成绩 |
| GET | `Match/update_score` | 更新比赛成绩 |
| GET | `Match/init_h_games` | 获取小组赛比赛 |

### Event

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `enter/detail` | 获取赛事详情 |
| GET | `enter/get_member_detail` | 获取参赛成员详情 |
| GET | `Match/get_groups` | 获取赛事分组 |
| GET | `Match/get_all_honors` | 获取所有荣誉 |
| GET | `Match/getResult` | 获取比赛结果 |
| GET | `Match/getScoreChange2` | 获取积分变化 (Query: eventid) |

### Public

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `publicc/GetCities` | 获取城市列表 |

## License

MIT License

Copyright (c) 2024

