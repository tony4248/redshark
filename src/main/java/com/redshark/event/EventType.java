package com.redshark.event;

public class EventType {

	public final static int ANY = 1000; //任何类型
	public final static int CONNECT = 1010; //连接
	public final static int CONNECT_FAILED = 1011; //连接失败
	public final static int CONNECT_SUCCESS = 1012; //连接成功
	public static final int DISCONNECT = 1013;
	public static final int RECONNECT = 1014; //重连
	public static final int RECONNEC_FAILEDT = 1015; //重连失败
	public static final int RECONNECT_SUCCESS = 1016; //重连成功
	public static final int LOG_IN = 1020; //登录
	public static final int LOG_IN_SUCCESS = 1021; //登录成功
	public static final int LOG_IN_FAILURE = 1022; //登录失败
	public static final int LOG_OUT = 1023; //登出
	public static final int LOG_OUT_SUCCESS = 1024; //登出成功
	public static final int SESSION_IDLE = 1025; //session idle 事件
	public static final int SESSION_TIMEOUT = 1026; //session timeout 事件
	
	public static final int GAME_LIST = 1030; //获取游戏列表
	public static final int ROOM_LIST = 1040; //获取房间列表
	public static final int ROOM_CREATE = 1050; //创建房间
	public static final int ROOM_CREATE_SUCCESS = 1051; //创建房间成功
	public static final int ROOM_CREATE_FAILED = 1052; //创建房间失败
	public static final int ROOM_JOIN = 1060; //加入房间
	public static final int ROOM_JOIN_OTHERS = 1061; //其他人加入房间,用于通知同房间内的其他客户端
	public static final int ROOM_JOIN_SUCCESS = 1062; //加入房间成功
	public static final int ROOM_JOIN_FAILED = 1063; //加入房间失败
	public static final int TABLE_IS_READY = 1065; //桌子已经准备好了,人数够了,可以提示用户准备了
	public static final int ROOM_LEAVE = 1070; //退出房间
	public static final int ROOM_LEAVE_OTHERS = 1071; //有人退出房间,用于通知同房间内的其他客户端
	public static final int ROOM_CLOSE = 1080; //关闭房间
	
	public static final int GAME_DDZ = 2000; //斗地主相关消息
	public static final int GAME_DDZ_TIMED_TASK = 2010; //斗地主相关消息
	public static final int GAME_TEXAS = 2020; //德州扑克相关消息
	public static final int GAME_TEXAS_TIMED_TASK = 2030; //德州扑克相关消息
		
	public static final int USER_MESSAGE= 9970; //用户信息
	public static final int ROOM_MESSAGE = 9980; //房间消息
	public static final int SYSTEM_MESSAGE = 9990; //系统消息
	public static final int EXCEPTION = 9999; //异常
	
}
