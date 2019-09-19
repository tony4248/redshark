package com.redshark.texas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redshark.texas.ExtraAttributes.GameArgs;
import com.redshark.util.CommonUtil;
import com.redshark.entity.Room;
import com.redshark.entity.Table;
import com.redshark.entity.User;

/**
 * @author weswu
 *
 */
public class ExtraAttrUtils {
	
	/**
	 * 取得ExtraAttributes
	 * @param table
	 * @return
	 */
	public static ExtraAttributes getExtraAttributes(Table table) {
		return (ExtraAttributes) table.getAttribute(Constants.EXTRA_ATTRIBUTES_KEY);
	}
	
	/**
	 * 重置计数相关的变量
	 */
	public static void resetCountValue(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.resetCountValue();
	}
	/**
	 * 一局结束后充值变量
	 */
	public static void resetRoundVariable(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.resetRoundVariable();
	}
	
	/**
	 * 为新一局增加一组变量
	 * @param table
	 */
	public static void addSubGame(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		extraAttributes.addSubGame();
	}
	
	public static Deck getDeck(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getDeck();
	}
	
	public static void setDeck(Table table, Deck deck) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setDeck(deck);
	}
	
	/**
	 * 设置该用户准备好了
	 * @param table
	 * @param userId
	 */
	public static void setReady(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> readyVotes = gameArgs.getReadyVotes();
		readyVotes.put(userId, new Integer(1));
	}
	
	/**
	 * 设置该用户准备好了
	 * @param table
	 * @param userId
	 */
	public static boolean isReady(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> readyVotes = gameArgs.getReadyVotes();
		if(null != readyVotes && readyVotes.containsKey(userId)) 
		{
			if (readyVotes.get(userId) == 1) {return true;}
		}
		return false;
	}
	
	/**
	 * 是否全部都准备好了
	 * @param table
	 * @return
	 */
	public static boolean isReadyComplete(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getReadyVotes().size() == table.getSeatsNum()) {return true;}
		return false;
	}
	
	/**
	 * 取得button位置
	 * @param table
	 * @return
	 */
	public static int getButtonSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		return extraAttributes.getButtonSeatNo(); //庄位
	}
	
	/**
	 * 设置button位置
	 * @param table
	 * @return
	 */
	public static void setButtonSeatNo(Table table, int SeatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		extraAttributes.setButtonSeatNo(SeatNo);
	}
	
	/**
	 * 取得小盲注位置
	 * @param table
	 * @return
	 */
	public static int getSmallBlindSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		return extraAttributes.getSmallBlindSeatNo(); //小盲注位
	}
	
	/**
	 * 设置小盲注位置
	 * @param table
	 * @return
	 */
	public static void setSmallBlindSeatNo(Table table, int smallBlindSeatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		extraAttributes.setSmallBlindSeatNo(smallBlindSeatNo);
	}
	
	public static int getLastRaisedSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		int lastRaisedSeatNo = gameArgs.getLastRaisedSeatNo(); //地主叫分
		return lastRaisedSeatNo; //地主叫分
	}
	
	public static void setLastRaisedSeatNo(Table table, int SeatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastRaisedSeatNo(SeatNo);
	}
	
	
	public static List<User> getWinners(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getWinners();
	}
	
	public static void setWinners(Table table, List<User> winners) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setWinners(winners);
	}
	
	public static int getMaxChips(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getMaxChips();
	}
	
	public static void setMaxChips(Table table, int maxChips) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setMaxChips(maxChips);
	}
	
	public static int getMinChips(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getMinChips();
	}
	
	public static void setMinChips(Table table, int minChips) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setMinChips(minChips);
	}
	
	
	/**
	 * 取得当前的押注的阶段
	 * @param table
	 * @return
	 */
	public static BetRound getBetRound(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getBetRound();
	}
	
	/**
	 * 设置当前的押注阶段
	 * @param table
	 * @param betRound
	 */
	public static void setBetRound(Table table, BetRound betRound) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setBetRound(betRound);
	}
	
	/**
	 * 取得本局的奖池
	 * @param table
	 * @return
	 */
	public static Pot getPot(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getPot();
	}
	
	/**
	 * 设置本局的奖池
	 * @param table
	 * @param pots
	 */
	public static void setPot(Table table, Pot pot) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setPot(pot);
	}
	
	public static int getToCallAmount(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getToCallAmount();
	}
	
	/**
	 * 设置跟注的额度，永远是这轮已投注的最大值
	 * @param table
	 */
	public static void setToCallAmount(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		int maxRoundBet = CommonUtil.getMaxValue(gameArgs.getRoundBets());
		int toCallAmount = Math.max(gameArgs.getBigBlindBet(), maxRoundBet);
		gameArgs.setToCallAmount(toCallAmount);
	}
	
	public static int getSmallBlindBet(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getSmallBlindBet();
	}
	
	public static void setSmallBlindBet(Table table, int smallBlindBet) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setSmallBlindBet(smallBlindBet);
	}
	
	public static int getBigBlindBet(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getBigBlindBet();
	}
	
	public static void setBigBlindBet(Table table, int bigBlindBet) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setBigBlindBet(bigBlindBet);
	}
	
	@SuppressWarnings("serial")
	public static List<Poker> getBoardCards(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		List<Poker> boardCards = gameArgs.getBoardCards();
		if (null == boardCards) {boardCards = new ArrayList<Poker>(){};}
		gameArgs.setBoardCards(boardCards);
		return gameArgs.getBoardCards();
	}
	
	public static void setBoardCards(Table table, List<Poker> boardCards) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setBoardCards(boardCards);
	}
	
	public static void setPocketCards(Table table, String userId, List<Poker> pocketCards) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, List<Poker>> allPCards = gameArgs.getPocketCards();
		allPCards.put(userId, pocketCards);
	}
	
	public static List<Poker> getPocketCards(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, List<Poker>> allPCards = gameArgs.getPocketCards();
		if(null == allPCards) {
			allPCards = new HashMap<>();
			gameArgs.setPocketCards(allPCards);
		}
		List<Poker> pocketCards= null;
		if(null != allPCards && allPCards.containsKey(userId)){ pocketCards = allPCards.get(userId);}
		return pocketCards;
	}
	
	public static void setBestHand(Table table, String userId, Hand bestHand) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Hand> allBestHands = gameArgs.getBestHands();
		if(null == allBestHands) {allBestHands = new HashMap<String, Hand>();}
		allBestHands.put(userId, bestHand);
		gameArgs.setBestHands(allBestHands);
	}
	
	public static Hand getBestHand(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Hand> allBestHands = gameArgs.getBestHands();
		Hand bestHand= allBestHands.get(userId);
		return bestHand;
	}
	
	public static void setChip(Table table, String userId, int chip) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> chips = gameArgs.getChips();
		if(null == chips) {chips = new HashMap<String, Integer>();}
		chips.put(userId, chip);
		gameArgs.setChips(chips);
	}
	/**
	   * @return the number of chips to bet in the current round of betting.
	   */
	public static int getChip(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> chips = gameArgs.getChips();
		int chip = 0;
		if(null != chips && chips.containsKey(userId)) {chip = chips.get(userId);}
		return chip;
	}
	
	/**
	 * 设置本论的下注额,采用直接设置模式
	 * @param table
	 * @param userId
	 * @param roundBet
	 */
	public static void setRoundBet(Table table, String userId, int roundBet) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> roundBets = gameArgs.getRoundBets();
		if(null == roundBets) {roundBets = new HashMap<>();}
		roundBets.put(userId, roundBet);
		gameArgs.setRoundBets(roundBets);
	}
	
	/**
	  * @return the number of chips to bet in the current round of betting.
	  */
	public static int getRoundBet(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> roundBets = gameArgs.getRoundBets();
		int roundBet = 0;
		if(null != roundBets && roundBets.containsKey(userId)){roundBet = roundBets.get(userId);}
		return roundBet;
	}
	
	/**
	 * 取得这轮下注的Map对象
	 * @param table
	 * @return
	 */
	public static Map<String, Integer> getRoundBets(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> roundBets = gameArgs.getRoundBets();
		return roundBets;
	}
	/**
	 * 取得用户在本轮可用的最大下注额,包括本轮已下注的筹码+剩余的筹码
	 * @return The maximum bet that the player can make for the current betting round.
	 *     Betting this amount would have the player go all-in.
	 */
	public static int getMaxBet(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> roundBets = gameArgs.getRoundBets();
		Map<String, Integer> chips = gameArgs.getChips();
		int roundBet = 0;
		int chip = 0;
		if(null != roundBets && roundBets.containsKey(userId)){roundBet = roundBets.get(userId);}
		if(null != chips && chips.containsKey(userId)){chip = chips.get(userId);}
		return roundBet + chip;
	}
	
	/**
	  * @return true if this player is out of the game.
	  */
	public static boolean isBroken(Table table, String userId){
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> rountBets = gameArgs.getRoundBets();
		Map<String, Integer> chips = gameArgs.getChips();
		int roundBet = 0;
		double chip = -0.01;//先置为负数,有指定的值后会更新,只要其他的值大于0,这和为大于0;
		int potContribution = 0;
		if(null != rountBets && rountBets.containsKey(userId)) {roundBet = rountBets.get(userId);}
		if(null != chips && chips.containsKey(userId)) {chip = chips.get(userId);}
		if(null != gameArgs.getPot())
		{potContribution = gameArgs.getPot().getBet(userId);}
		return roundBet + chip + potContribution <= 0; //为-1表示没有chips,
	}
	
	/**
	  * @return true if this player is allin.
	  */
	public static boolean isAllIn(Table table, String userId){
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> rountBets = gameArgs.getRoundBets();
		Map<String, Integer> chips = gameArgs.getChips();
		int roundBet = 0;
		double chip = 0;
		int potContribution = 0;
		if(null != rountBets && rountBets.containsKey(userId)) {roundBet = rountBets.get(userId);}
		if(null != chips && chips.containsKey(userId)) {chip = chips.get(userId);}
		if(null != gameArgs.getPot())
		{potContribution = gameArgs.getPot().getBet(userId);}
		if ((potContribution > 0 || roundBet > 0) &&  chip == 0) {return true;}
		return false;
	}
	
	/**
	  * Gives this player the given number of chips.
	  * @param chipsAwarded The number of chips to give.
	  */
	public static void setChipsAwarded(Table table, String userId, int chipsAwarded) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> chipsAwardeds = gameArgs.getChipsAwardeds();
		int fianlCW = chipsAwardeds.getOrDefault(userId, 0) + chipsAwarded;
		chipsAwardeds.put(userId, fianlCW);
		gameArgs.setChipsAwardeds(chipsAwardeds);
	}
	
	public static int getChipsAwarded(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> chipsAwardeds = gameArgs.getChipsAwardeds();
		int chipsAwarded = 0;
		if(null != chipsAwardeds && chipsAwardeds.containsKey(userId)) 
		{chipsAwarded = chipsAwardeds.get(userId);}
		return chipsAwarded;
	}
	
	public static void setFolded(Table table, String userId, boolean isFolde) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Boolean> isFoldeds = gameArgs.getIsFoldeds();
		if(null == isFoldeds) {isFoldeds = new HashMap<>();}
		isFoldeds.put(userId, isFolde);
		gameArgs.setIsFoldeds(isFoldeds);
	}
	
	public static boolean isFolded(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Boolean> isFoldeds = gameArgs.getIsFoldeds();
		boolean isFolded = false;
		if(null != isFoldeds && isFoldeds.containsKey(userId)) {isFolded = isFoldeds.get(userId);}
		return isFolded;
	}

	public static int getLastChipsPutted(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastChipsPutted();
	}
	
	public static void setLastChipsPutted(Table table, int lastChipsPutted) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastChipsPutted(lastChipsPutted);
	}
	
	public static int getLastSeatPutted(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastSeatPutted();
	}
	
	public static void setLastSeatPutted(Table table, int lastSeatPutted) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastSeatPutted(lastSeatPutted);
	}
	
	public static void setLastSeatOps(Table table, OpsType lastSeatOps) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastSeatOps(lastSeatOps);
	}
	
	public static OpsType getLastSeatOps(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastSeatOps();
	}
	
	/**
	 * 取得本局用户的积分
	 * @param table
	 * @return
	 */
	public static Map<Integer, Integer> getSubScores(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getSubScores();
	}
	
	public static int getSubScore(Table table, int seatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getSubScores().get(seatNo);
	}
	
	public static void setSubScore(Table table, int seatNo, int score) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.getSubScores().put(seatNo, score);
	}

	/**
	 * 设置用户再来一局的vote
	 * @param table
	 * @param userId
	 * @param res, 1:同意，0:不同意
	 */
	public static void setPlayAgain(Table table, String userId, int res ) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> playAgainVotes = gameArgs.getPlayAgainVotes();
		playAgainVotes.put(userId, new Integer(res));
	}
	
	/**
	 * 是否全部都投票了
	 * @param table
	 * @return
	 */
	public static boolean isPlayAgainComplete(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> playAgainVotes = gameArgs.getPlayAgainVotes();
		//如果全部收到,返回
		if(playAgainVotes.size() == table.getSeatsNum()){return true;} 
		return false;
	}
	
	/**
	 * 投票结束后,检查结果
	 * @param table
	 * @return
	 */
	public static boolean shouldPlayAgain(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> playAgainVotes = gameArgs.getPlayAgainVotes();
		int voteScore = 0;
		for (Map.Entry<String, Integer> vote : playAgainVotes.entrySet())
		{
		    voteScore += vote.getValue();
		}
		//如果有两票以上同意
		if(voteScore >= 2) {return true;};
		//否则
		return false;
	}
	/**
	 * 取得玩到第几局了
	 */
	public static int getCurrentRoundNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		return extraAttributes.getSubGameIndex() + 1;
	}
}
