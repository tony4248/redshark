package com.redshark.texas;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TexasDTO {

	//用户ready的请求消息
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BeReadyReq{
		private String cmd;
	}
	//产生庄的命令
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PickDealerRes{
		private String cmd;
		private int dealerSeatNo; //庄的席位
	}
	//开始在大小盲注位置下注
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StartBlindBetRes{
		private String cmd;
		private int smallBlindBetSeatNo; //小盲注位置
		private int bigBlindBetSeatNo; //大盲注位置
		private int smallBlindBets; //小盲注下注量
		private int bigBlindBets; //一注的下注量,小盲注位是1/2,大盲注位是1.
	}
	//服务发翻牌圈的消息,单播消息
	@Data
	@NoArgsConstructor
	public static class DealBoardPokersRes
	{
		private String cmd;
		private List<Integer> pokers; //手牌,
		
	}
	//服务发牌的消息,单播消息,每个人的底牌
	@Data
	@NoArgsConstructor
	public static class DealPocketPokersRes
	{
		private String cmd;
		private List<Integer> pokers; //手牌,
		
	}
	
	//用户下注,发送给服务器
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PutBetsReq{
		private String cmd;
		private String opsType; //操作类型
		private int chips; //筹码的数量

	}
	
	//服务发送出牌的命令给客户端,广播消息
	@Data
	@NoArgsConstructor
	public static class PutBetsRes{
		private String cmd;
		private int currentSeatNo; //当前的可以出牌的席位, -1表示本轮结束
		private int minBetAmount; //最低下注额
		private int minRaiseAmount; //最低加注额
		private int toCallAmount; //跟注数额
		private List<String> availOpsTypes; //可用的操作类型
		private int lastChipsPutted; //上一手注
		private int lastSeatPutted; //上一手注的席位号
		private String lastSeatOps; //上一席位是操作类型
	}
	
	//服务发送子游戏结束的命令给客户端,单播播消息
	@Data
	@NoArgsConstructor
	public static class SubGameEndRes{
		private String cmd;
		private int winnerSeatNo; //本局赢家的坐席号
		private List<UserScore> userScores; //用户的积分
		private boolean someOneBroken; //是否有人破产
		private boolean reachGameRounds; //是否达到规定的局数
	}
	
	//游戏的结果,存放用户的积分
	@Data
	@NoArgsConstructor
	public static class UserScore{
		private boolean bankrupt; 
		private String name; //用户姓名
		private int seatNo; //用户席位号
		private int score; //用户的当前的积分
		private int finalScore;
	}
	//用户再来一局的请求,和结束游戏一起投标
	@Data
	@NoArgsConstructor
	public static class PlayGameAgainReq{
		private String cmd;
		private int playAgain; //1:再来一局, 0:结束游戏
	}
	//用户再来一局的命令
	@Data
	@NoArgsConstructor
	public static class PlayGameAgainRes
	{
		private String cmd;
		private int round; //第几局
	}
	//游戏结束的请求,用来处理用户强制离开的情况
	@Data
	@NoArgsConstructor
	public static class GameEndReq{
		private String cmd;
		private boolean isOthersLeave;
	}
	//服务发送游戏结束的命令给客户端,单播播消息
	@Data
	@NoArgsConstructor
	public static class GameEndRes{
		private String cmd;
		private int totalScore; //本房间积分
	}
}
