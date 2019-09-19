RedShark棋牌游戏服务端
=====
RedShark的服务端,紧凑的游戏服务端,支持http(s)/websocket,事件/消息驱动,支持高并发,采用MongoDB,功能完善,部署简单。支持金币场,房卡,俱乐部三种模式

## 重要的特点说明:
* 多模式支持:支持金币场,房卡,俱乐部三种模式
* 采用Netty做为网络处理的框架,自定义消息处理接口
* Http(s)/websocket协议的功能：用户注册/登录时采用Http(s),登录成功后续的消息采用WebSocket
* 事件处理引擎,采用多线程并发处理,支持定时事件和实时事件,支持高并发
* 系统任务管理:支持自定义CRON系统任务。
* 基础功能:用户管理,大厅服务,房间管理,游戏管理,商城管理,邮件管理,反馈管理,智能匹配
* 高级功能:无限极的代理管理,俱乐部功能，俱乐部有独立的积分体系
* 游戏模块:目前支持斗地主,德州扑克,扎金花,抢庄牛牛
* AI功能:引入AI算法


## Demo:
* 正在寻找环境


## 依赖的平台
* Netty
* logback
* lombok
* jongo

## 授权
	Copyright (C) 2017 QQ:1248756778
	This code is licensed under The General Public License version 3
	
## 反馈
	Your feedbacks are highly appreciated! :)
