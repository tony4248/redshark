package com.redshark.ddz;

import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExtraAttributes {
	//第几局,从0开始
	private int subGameIndex;
	//席位数量
	private int seatsNum;
	//牌局的列表
	private List<GameArgs> subGames;
	
	//构造函数
	public ExtraAttributes(int seatsNum) {
		this.subGameIndex = 0;
		this.seatsNum = seatsNum;
		this.subGames = new ArrayList<>();
		GameArgs gameArgs =  new GameArgs();
		this.subGames.add(gameArgs);
	}
	
	//增加新的一局
	public void addSubGame() {
		this.subGameIndex ++ ;
		GameArgs gameArgs =  new GameArgs();
		this.subGames.add(gameArgs);
	}
	
	//取得当前的局
	public GameArgs getSubGame() {
		return this.subGames.get(this.subGameIndex);
	}
	
	//取得特定的局
	public GameArgs getSubGame(int gameIndex) {
		return this.subGames.get(gameIndex);
	}
	
	@Data
    @SuppressWarnings("serial")
	public class GameArgs{
		//当前准备的人数
		private volatile Map<String, Integer> readyVotes;
		//当前取得牌的人数
		private volatile int dealPokersEndNum = 0;
		//叫地主的次数,即使不叫也算一次,无效的叫
		private volatile Map<String, Integer> callLadlordVotes;
		//最后一个叫地主的席位号
		private volatile int lastCallLandlordSeatNo = -1;
		//最后一个叫地主有效的叫分
		private volatile int lastCallLandlordScore = 0;
		//底分加倍的次数
		private volatile Map<String, Integer> addScoreVotes;
		//最后一个叫地主的席位号
		private volatile int lastAddScoreSeatNo = -1;
		//最后一个叫地主有效的叫分
		private volatile int lastAddScore = 1;
		//地主的席位号
	    private volatile int landLordSeatNo = -1;
	    //地主的叫分
	    private volatile int landLordScore = -1;
	    //炸弹的数量
	    private volatile int boomNum = 0;
	    //有效出牌的步数,用于动画显示,避免重复
	    private volatile int validPlaySteps = 0; 
	    //赢家的座位号
	    private volatile int winnerSeatNo = -1;
	    //最后一手牌
	    private List<Integer> lastPokersPlayed;
	    //最后一手牌的席位
	    private volatile int lastSeatPlayed = -1;
	    //用户的手中的牌,key是座位号,最后一个是底牌
	    private Map<Integer, List<Integer>> pokers;
	    //用户本局的积分,key是座位号,最后一个是本局分
	    private Map<Integer, Integer> subScores;
	    //再来一局的人数
	  	private volatile Map<String, Integer> playAgainVotes;
	  	//结束游戏的人数
	  	private volatile Map<String, Integer> gameEndVotes;
	    
		public GameArgs() {
			this.lastPokersPlayed = new ArrayList<Integer>() {};
	    	this.pokers = new HashMap<Integer, List<Integer>>(){};
	    	this.subScores = new HashMap<Integer, Integer>(){};
	    	this.readyVotes = new HashMap<String, Integer>() {};
	    	this.callLadlordVotes = new HashMap<String, Integer>() {};
	    	this.addScoreVotes = new HashMap<String, Integer>() {};
	    	this.playAgainVotes = new HashMap<String, Integer>() {};
	    	this.gameEndVotes = new HashMap<String, Integer>() {};
	    }
		
		public List<Integer> getLastPokersPlayed(){
			if (null != this.lastPokersPlayed) {return this.lastPokersPlayed;}
			return new ArrayList<Integer>() {};
		}
		
		/**
		 * 荒局后需要充值这些变量
		 */
		public void resetCountValue() {
			this.dealPokersEndNum = 0;
			this.lastCallLandlordScore = 0;
			this.lastCallLandlordSeatNo = -1;
			this.lastAddScore = 1;
			this.lastAddScoreSeatNo = -1;
			this.pokers = new HashMap<Integer, List<Integer>>(){};
			//可以重置,因为重新发牌时不检查这个vote的状态
			this.readyVotes = new HashMap<String, Integer>() {};
			this.callLadlordVotes = new HashMap<String, Integer>() {};
	    	this.addScoreVotes = new HashMap<String, Integer>() {};
	    	this.playAgainVotes = new HashMap<String, Integer>() {};
	    	this.gameEndVotes = new HashMap<String, Integer>() {};
		}
	 
	    
	}
    
    

    
    
}
