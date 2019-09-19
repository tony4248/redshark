package com.redshark.ddz;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DdzDTO {

	//用户ready的请求消息
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BeReadyReq{
		private String cmd;
	}
	
	//服务发牌的消息,单播消息
	@Data
	@NoArgsConstructor
	public static class DealPokersRes
	{
		private String cmd;
		private List<Integer> pokers; //手牌,
		
	}
	
	//客户端收到牌后,服务器的确认
	@Data
	@NoArgsConstructor
	public static class DealPokersEndReq
	{
		private String cmd;
		
	}
	
	//服务器命令客户端叫地主的消息,广播消息
	@Data
	@NoArgsConstructor
	public static class CallLandlordRes{
		private String cmd;
		private int currentSeatNo; //当前的可以叫分的席位
		private int lastCallLandlordScore; //最后的叫分
		private int lastCallLandlordSeatNo; //最后叫分的席位
		private int prevSeatNo; //前一个坐席号
		private boolean prevSeatAction; //前一个座位是否有操作
	}
	
	//用户抢地主的请求消息, 每次都会记次
	//按照抢地主的方法确定地主
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CallLandlordReq{
		private String cmd;
		private int score; //当前用户的交分,0:不抢,3:抢地主,在前面的基础上乘2
	}
	
	//产生地主的命令
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class HasLandlordRes{
		private String cmd;
		private int landLordSeatNo; //地主的席位
		private int landLordScore; //地主的分
		private List<Integer> bottomPokers; //底牌,
		private int prevSeatNo; //前一个坐席号
		private boolean prevSeatAction; //前一个座位是否有操作
	}
	
	//没有产生地主,本局荒牌,广播消息
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NoLandlordRes{
		private String cmd;
		private int prevSeatNo; //前一个坐席号
		private boolean prevSeatAction; //前一个座位是否有操作
	}
	
	//产生地主后,地主扣牌的命令
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddScoreRes{
		private String cmd;
		private int currentSeatNo; //当前的可以加分操作的席位
		private int lastAddScoreSeatNo; //最后加倍的席位
		private int lastAddScore; //最后加倍的倍数
	}
	
	//地主扣牌的请求
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AddScoreReq{
		private String cmd;
		private int score; //乘数，乘子
	}
	
	//开始出牌命令,广播消息，，这个命令可以取消
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BottomScoreRes{
		private String cmd;
		private int landLordScore; //地主的分
		private int lastAddScoreSeatNo; //最后加倍的席位
		private int lastAddScore; //最后加倍的倍数
	}
	
	//用户出牌,发送给服务器
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlayPokersReq{
		private String cmd;
		private List<Integer> pokers; //null 表示不出牌

	}
	
	//服务发送出牌的命令给客户端,广播消息
	@Data
	@NoArgsConstructor
	public static class PlayPokersRes{
		private String cmd;
		private int currentSeatNo; //当前的可以出牌的席位
		private List<Integer> lastPokersPlayed; //上一手牌
		private int lastSeatPlayed; //上一手牌的席位号
		private int lastSeatPokersNum; //上一手牌所在席位的剩余牌数
		private int landLordScore; //最后地主的分
		private int prevSeatNo; //前一个坐席号
		private boolean prevSeatAction; //前一个座位是否有操作
	}
	
	//服务发送子游戏结束的命令给客户端,单播播消息
	@Data
	@NoArgsConstructor
	public static class SubGameEndRes{
		private String cmd;
		private int winnerSeatNo; //本局赢家的坐席号
		private List<UserScore> userScores; //用户的积分
		private boolean someOneBankrupt; //是否有人破产
		private boolean reachGameRounds; //是否达到规定的局数
	}
	
	//游戏的结果,存放用户的积分
	@Data
	@NoArgsConstructor
	public static class UserScore{
		private boolean landlord;
		private boolean bankrupt; 
		private String name; //用户姓名
		private int seatNo; //用户席位号
		private int score; //用户的当前的积分
		private int landlordScore;
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
