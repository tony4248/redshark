package com.redshark.texas;

public class Constants {
	public static int USER_NUM_PER_TABLE = 3;
	public static int PICK_DEALER_SLEEP_IN_MS = 3 * 1000; //发送找到庄的命令后休眠的时间
	public static int START_BLIND_BETS_SLEEP_IN_MS = 3 * 1000; //发盲注命令后休眠的时间
	public static int START_PUT_BETS_SLEEP_IN_MS = 3 * 1000; //发盲注命令后休眠的时间
	public static int START_DEAL_POCKET_POKERS_SLEEP_IN_MS = 1 * 1000; //发盲注命令后休眠的时间
	public static int PLAY_AGAIN_SLEEP_IN_MS = 1 * 1000; //发送最终底分的命令后休眠的时间
	public static int PUT_BETS_TIMEOUT = 60 * 1000;
	public static String EXTRA_ATTRIBUTES_KEY = "extraAttrsKey";
	
}
