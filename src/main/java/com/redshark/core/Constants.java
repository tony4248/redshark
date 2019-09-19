package com.redshark.core;

public class Constants {

	public static String HTTP_HOST = "localhost";
	public static int HTTP_PORT = 9688;
	public static String WEBSOCKET_HOST = "localhost";
    public static int WEBSOCKET_PORT = 9688;
    public static String WEBSOCKET_URL = "/websocket";
    public static String HTTP_API_REGISTER = "/api/register";
    public static String HTTP_API_LOGIN = "/api/login";
    public static int ACTIVE_SESSION_CACHE_MAX_CAPACITY = 50000;
    public static int ACTIVE_SESSION_CACHE_TTL_MS = 0;
    public static int ACTIVE_SESSION_TTL_MS = 3 * 60 * 1000; //有数据通讯的TTL
    public static int ACTIVE_ROOM_CACHE_MAX_CAPACITY = 50000;
    public static int ACTIVE_ROOM_CACHE_TTL_MS = 0;
    public static int CHANNEL_CLEARUP_REPEAT_IN_MS = 1 * 30 * 1000;
    public static String MSG_VERSION = "1.0.0";
    public static int NUMBERS_OF_THREADS_IN_THREAD_POOL = 4;
    public static String SESSION_ID_KEY = "sessionid";
    public static String DATABASE_IP = "localhost";
    public static int DATABASE_PORT = 27017;
    public static String DATABASE_NAME = "mydb";
}
