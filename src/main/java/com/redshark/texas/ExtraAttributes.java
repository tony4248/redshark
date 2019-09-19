package com.redshark.texas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redshark.entity.User;

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
	
	
	//庄位
	private volatile int buttonSeatNo = -1;
	//小盲注位
	private volatile int smallBlindSeatNo = -1;
	
	@Data
    @SuppressWarnings("serial")
	public class GameArgs{
		//一副牌
		Deck deck;
		//当前准备的人数
		private volatile Map<String, Integer> readyVotes;
		//本局的奖池
		private volatile Pot pot;
		//处于投注的那个阶段
		private BetRound betRound;	
		// The player who last bet or raised. Set to the first actor at the start of a round of betting.
		private volatile int lastRaisedSeatNo = -1;
		// The players who won the current hand.
		private List<User> winners;
		// The amount of chips needed to call in a round of betting.
		private volatile int toCallAmount;
		// The amount of chips needed to call in a round of betting.
		private volatile int minRaiseAmount;
	    // The number of chips the player has. This does not include any chips that the player has
		// contributed to the pot or has put up in the current round of betting.
		private volatile Map<String, Integer> chips;
		// The number of chips the player has put up in the current round of betting. This does not
	    // include any chips from previous rounds of betting, they get added to the pot as soon as
		// the round of betting ends.
		private volatile Map<String, Integer> roundBets;
		// The number of chips awarded to the player after a hand, including chips that are returned
		// because no other player called a bet.
		private volatile Map<String, Integer> chipsAwardeds;
		// True if this player has folded.
		private volatile Map<String, Boolean> isFoldeds;
		//最大带入筹码
		private volatile int maxChips;
		//最小带入筹码
		private volatile int minChips;
		//小盲注的数
		private volatile int smallBlindBet;
		//大盲注的数
		private volatile int bigBlindBet;
		// The cards on the board.
		private volatile List<Poker> boardCards;
		// The player's pocket cards.
		private volatile Map<String, List<Poker>> pocketCards;
		// The best hand the player can make with the board cards.
		private volatile Map<String, Hand> bestHands;
	    //用户本局的积分,key是座位号,最后一个是本局分
	    private volatile Map<Integer, Integer> subScores;
	    //再来一局的人数
	  	private volatile Map<String, Integer> playAgainVotes;
	  	//结束游戏的人数
	  	private volatile Map<String, Integer> gameEndVotes;
	    //最后一手投注的数量
	    private volatile int lastChipsPutted;
	    //最后一手投注的席位号
	    private volatile int lastSeatPutted;
	    //最后一手的席位的操作类型
	    private volatile OpsType lastSeatOps;
	    
		public GameArgs() {
			this.boardCards = new ArrayList<Poker>();
	    	this.subScores = new HashMap<Integer, Integer>(){};
	    	this.readyVotes = new HashMap<String, Integer>() {};
	    	this.chips = new HashMap<String, Integer>() {};
	    	this.roundBets = new HashMap<String, Integer>() {};
	    	this.isFoldeds = new HashMap<String, Boolean>(){};
	    	this.chipsAwardeds = new HashMap<String, Integer>(){};
	    	this.playAgainVotes = new HashMap<String, Integer>() {};
	    	this.gameEndVotes = new HashMap<String, Integer>() {};
	    }
		
		/**
		 * 一轮投注结束,需要充值变量
		 */
		public void resetRoundVariable() {
		    this.toCallAmount = 0;
		    this.minRaiseAmount = 0;
		    this.lastChipsPutted = 0;
		    this.lastSeatOps = OpsType.NONE;
		    this.lastSeatPutted = -1;
		    this.lastRaisedSeatNo = -1;
	    	this.roundBets.clear();

		}
		/**
		 * 一局结束,需要充值的变量
		 */
		public void resetCountValue() {
			//可以重置,因为重新发牌时不检查这个vote的状态
	    	this.boardCards.clear();
	    	this.subScores.clear();
	    	this.readyVotes.clear();
	    	this.chips.clear();
	    	this.roundBets.clear();
	    	this.isFoldeds.clear();
	    	this.chipsAwardeds.clear();
	    	this.playAgainVotes.clear();
	    	this.gameEndVotes.clear();
		}
	 
	    
	}
    
}
