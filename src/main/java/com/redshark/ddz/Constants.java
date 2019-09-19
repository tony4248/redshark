package com.redshark.ddz;

public class Constants {
	public static int USER_NUM_PER_TABLE = 3;
	public static int LANDLORD_INIT_SCORE = 3;
	public static int HAS_LANDLORD_SLEEP_IN_MS = 3 * 1000; //发送地主产生的命令后休眠的时间
	public static int NO_LANDLORD_SLEEP_IN_MS = 3 * 1000; //发无地主产生的命令后休眠的时间
	public static int BOTTOM_SCORE_SLEEP_IN_MS = 3 * 1000; //发送最终底分的命令后休眠的时间
	public static int PLAY_AGAIN_SLEEP_IN_MS = 1 * 1000; //发送最终底分的命令后休眠的时间
	public static int CALL_LANDLORD_TIMEOUT = 20 * 1000;
	public static int PLAY_POKERS_TIMEOUT = 20 * 1000;
	public static int ADD_SCORE_TIMEOUT = 20 * 1000;
	public static String EXTRA_ATTRIBUTES_KEY = "extraAttrsKey";
	
}
