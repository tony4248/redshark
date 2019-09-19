package com.redshark.ddz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.redshark.ddz.ExtraAttributes.GameArgs;
import com.redshark.entity.Table;

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
	 * 取得当前收到牌的数量
	 * @param table
	 * @return
	 */
	public static int addDealPokersEndNum(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setDealPokersEndNum(gameArgs.getDealPokersEndNum() + 1);
		return gameArgs.getDealPokersEndNum();
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
	 * 为新一局增加一组变量
	 * @param table
	 */
	public static void addSubGame(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		extraAttributes.addSubGame();
	}
	
	/**
	 * 取得最后一个叫分
	 * @param table
	 * @return
	 */
	public static int getLastCallLandlordScore(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastCallLandlordScore(); //地主叫分
	}
	
	
	/**
	 * 取得最后一个叫席
	 * @param table
	 * @return
	 */
	public static int getLastCallLandlordSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastCallLandlordSeatNo(); //地主席位
	}
	
	/**
	 * 查看该用户是否准备好了
	 * @param table
	 * @param userId
	 * @return
	 */
	public static boolean isReady(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getReadyVotes().containsKey(userId)) {return true;}
		return false;
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
	 * 查看该用户是否叫过地主了
	 * @param table
	 * @param userId
	 * @return
	 */
	public static boolean isCallLandlord(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getCallLadlordVotes().containsKey(userId)) {return true;}
		return false;
	}
	
	/**
	 * 设置该用户叫过地主了
	 * @param table
	 * @param userId
	 */
	public static void setCallLandlord(Table table, String userId, DdzDTO.CallLandlordReq callLandlordReq) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		//加入该用户的叫分, 0分表示不抢,3分表示抢
		Map<String, Integer> callLandlordVotes = gameArgs.getCallLadlordVotes();
		callLandlordVotes.put(userId, callLandlordReq.getScore());
		//如果用户抢地主了,则应该更新
		if(Constants.LANDLORD_INIT_SCORE == callLandlordReq.getScore()) {
			//更新最后的叫分
			gameArgs.setLastCallLandlordScore(calcLandlordScore(table));
			//更新最后的叫席位
			gameArgs.setLastCallLandlordSeatNo(table.getSeatNoByUserId(userId));
		}
		//如果都叫过了
		if(isCallLandlordComplete(table)) {
			//可能没有人叫,席位是-1,分是0
			gameArgs.setLandLordSeatNo(gameArgs.getLastCallLandlordSeatNo());
			gameArgs.setLandLordScore(calcLandlordScore(table));
		}
		//返回
		return;
	}
	
	/**
	 * 更加各用户叫地主的情况,计算最后的分
	 * @param table
	 * @return
	 */
	public static int calcLandlordScore(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		int count = 1;
		int score = 0;
		for (Map.Entry<String, Integer> entry : gameArgs.getCallLadlordVotes().entrySet()) {
			if(Constants.LANDLORD_INIT_SCORE == entry.getValue()) {
				if(1 == count) {
					score = Constants.LANDLORD_INIT_SCORE;
					continue;
				} //第一个直接跳过
				count ++;
				score = score * 2;
			}
	    }
		return score;
	}
	/**
	 * 是否有地主产生
	 * @param table
	 * @return
	 */
	public static boolean isLandlordElected(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(-1 != gameArgs.getLastCallLandlordSeatNo()) {return true;}
		return false;
	}
	
	/**
	 * 是否全部叫过地主了
	 * @param table
	 * @return
	 */
	public static boolean isCallLandlordComplete(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getCallLadlordVotes().size() == table.getSeatsNum()) {return true;}
		return false;
	}
	
	/**是否加过分
	 * @param table
	 * @param userId
	 * @return
	 */
	public static boolean isAddScore(Table table, String userId) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getAddScoreVotes().containsKey(userId)) {return true;}
		return false;
	}
	
	/**
	 * 是否全部加过分了
	 * @param table
	 * @return
	 */
	public static boolean isAddScoreComplete(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		if(gameArgs.getAddScoreVotes().size() == table.getSeatsNum()) {return true;}
		return false;
	}
	
	/**
	 * 设置加分
	 * @param table
	 * @param userId
	 * @param score
	 */
	public static void setAddScore(Table table, String userId, int score) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<String, Integer> addScoreVotes = gameArgs.getAddScoreVotes();
		addScoreVotes.put(userId, new Integer(score));
		//score 为1表示不加倍
		updateLandlordScore(table, score);
		//更新最后加分的席位
		setLastAddScoreSeatNo(table, table.getCurrentSeatNo());
		//更新最后加分
		setLastAddScore(table, score);
	}
	

	
	/**
	 * 取得地主的席位
	 * @param table
	 * @return
	 */
	public static int getLandLordSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLandLordSeatNo(); //地主席位
	}
	
	/**
	 * 取得地主的分
	 * @param table
	 * @return
	 */
	public static int getLandLordScore(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLandLordScore(); //地主席位
	}
	
	/**
	 * 返回地主的席位号和叫分
	 * @param table
	 * @return
	 */
	public static int[] getLandlordSeatNoAndScore(Table table) {
		int[] landlordRes =  new int[2];
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		landlordRes[0] = gameArgs.getLandLordSeatNo(); //地主席位
		landlordRes[1] = gameArgs.getLandLordScore(); //地主叫分
		return landlordRes;
	}
	
	
	/**
	 * 更新地主的底分
	 * @param table
	 * @param score
	 */
	public static void updateLandlordScore(Table table, int score) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		int landLordScore = gameArgs.getLandLordScore() * score;
		gameArgs.setLandLordScore(landLordScore);
	}

	/**
	 * 更新最新的一手牌
	 * @param table
	 * @param pokers
	 */
	public static void setLastPokersPlayed(Table table, List<Integer> pokers) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastPokersPlayed(pokers);
	}
	
	/**
	 * 取得最新的一手牌
	 * @param table
	 * @return
	 */
	public static List<Integer> getLastPokersPlayed(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastPokersPlayed();
	}
	
	/**
	 * 设定最后一手牌的座位号
	 * @param table
	 * @param seatNo
	 */
	public static void setlastSeatPlayed(Table table, int seatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastSeatPlayed(seatNo);
	}
	
	/**
	 * 取得最后一手牌的座位号
	 * @param table
	 * @return
	 */
	public static int getlastSeatPlayed(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastSeatPlayed();
	}
	
	/**
	 * 取得上一手牌所在席位的剩余牌数
	 * @param table
	 * @return
	 */
	public static int getLastSeatPokersNum(Table table) {
		Map<Integer, List<Integer>> pokers = getPokers(table);
		Integer seatNo = getlastSeatPlayed(table);
		if(null!= pokers && seatNo >= 0) {
			if (pokers.containsKey(seatNo)) {
				if(null != pokers.get(seatNo)) {
					return pokers.get(seatNo).size();
				}
			}
		}
		return -1;
	}
	
	/**
	 * 取得炸弹的数
	 * @param table
	 * @return
	 */
	public static int getBoomNum(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getBoomNum();
	}
	
	
	/**
	 * 增加炸弹的数
	 * @param table
	 */
	public static int addBoomNum(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setBoomNum(gameArgs.getBoomNum() + 1);
		return gameArgs.getBoomNum();
	}
	
	/**
	 * 如果是炸或王炸则增加炸弹的记录数
	 * @param table
	 * @param pokerIds
	 */
	public static void updateBoomNum(Table table, List<Integer> pokerIds) {
		List<Poker> pokers = PokerUtils.parsePokers(pokerIds);
		if(-1 != PokerTypeUtils.isBoom(pokers) 
				|| -1 != PokerTypeUtils.isKingBoom(pokers)) {
			addBoomNum(table);
			//更新底分
			updateLandlordScore(table, 2);
		}
	}
	
	/**
	 * 取得胜利的座号
	 * @param table
	 * @return
	 */
	public static int getWinnerSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getWinnerSeatNo();
	}
	
	
	/**
	 * 设置胜利的座号
	 * @param table
	 * @param seatNo
	 */
	public static void setWinnerSeatNo(Table table, int seatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setWinnerSeatNo(seatNo);
	}
	
	
	/**
	 * 取得所有用户的牌
	 * @param table
	 * @return
	 */
	public static Map<Integer, List<Integer>> getPokers(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		Map<Integer, List<Integer>> pokers= gameArgs.getPokers();
		return pokers;
	}
	
	/**
	 * 设定所有用户的牌
	 * @param table
	 * @param pokers
	 */
	public static void setPokers(Table table, Map<Integer, List<Integer>> pokers) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setPokers(pokers);
	}
	
	/**
	 * 从当前桌席中减掉扑克数列
	 * @param table
	 * @param toToRemovedPokers
	 */
	public static void removePokersFromCurrentSeat(Table table, List<Integer> toToRemovedPokers) {
		int seatNo = table.getCurrentSeatNo();
		getPokers(table).get(seatNo).removeAll(toToRemovedPokers);
	}
	
	/**
	 * 更新底牌
	 * @param table
	 * @param bottomPokers
	 */
	public static void setBottomPokers(Table table, List<Integer> bottomPokers) {
		getPokers(table).put(table.getSeatsNum() + 1, bottomPokers);
	}
	
	/**
	 * 取得底牌
	 * @param table
	 * @return
	 */
	public static List<Integer> getBottomPokers(Table table) {
		List<Integer> pokers = getPokers(table).get(table.getSeatsNum() + 1);
		return pokers;
	}
	
	
	/**
	 * 将底牌加入到当前席位中
	 * @param table
	 */
	public static void addBottomToLandLord(Table table, int landLordSeatNo) {
		//底牌在最后
		List<Integer> bottomPokers = getPokers(table).get(table.getSeatsNum() + 1);
		getPokers(table).get(landLordSeatNo).addAll(bottomPokers);
	}
	
	/**
	 * 是否当前席位的牌已经多出完
	 * @param table
	 * @return
	 */
	public static boolean isCurrentSeatWin(Table table) {
		int seatNo = table.getCurrentSeatNo();
		if(getPokers(table).get(seatNo).size() == 0) {return true;}
		return false;
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
	
	public static void setLastAddScore(Table table, int multiplicator) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastAddScore(multiplicator);
	}
	
	public static int getLastAddScore(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastAddScore();
	}
	
	public static int[] getLastAddScoreSeatNoAndScore(Table table) {
		int[] addScorRes =  new int[2];
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		addScorRes[0] = gameArgs.getLastAddScoreSeatNo(); //最后加分的席位
		addScorRes[1] = gameArgs.getLastAddScore(); //最后加分
		return addScorRes;
	}
	
	public static void setLastAddScoreSeatNo(Table table, int lastAddScoreSeatNo) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setLastAddScoreSeatNo(lastAddScoreSeatNo);
	}
	
	/**
	 * @param table
	 * @param lastAddScoreSeatNo
	 * @return
	 */
	public static int getLastAddScoreSeatNo(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getLastAddScoreSeatNo();
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
	 * 设置用户有效出牌的步骤数
	 * @param table
	 */
	public static void setValidPlaySteps(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		gameArgs.setValidPlaySteps(gameArgs.getValidPlaySteps() + 1);
	}
	
	/**
	 * 取得用户有效出牌的步骤数
	 * @param table
	 */
	public static int getValidPlaySteps(Table table) {
		ExtraAttributes extraAttributes = getExtraAttributes(table);
		GameArgs gameArgs = extraAttributes.getSubGame();
		return gameArgs.getValidPlaySteps();
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
		//如果收到2票,需要对比是否是相同的票
//		int voteScore = 0;
//		if(playAgainVotes.size() == (table.getSeatsNum() -1)) 
//		{
//			for (Map.Entry<String, Integer> vote : playAgainVotes.entrySet())
//			{
//			    voteScore += vote.getValue();
//			}
//		}
//		//如果2票的结果不同,则返回false
//		if(voteScore == 0 || voteScore == 2) {return true;};
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
