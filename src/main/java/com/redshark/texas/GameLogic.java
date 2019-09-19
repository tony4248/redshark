package com.redshark.texas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.core.ExceptionType;
import com.redshark.core.RsException;
import com.redshark.entity.FuncResult;
import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.entity.Table;
import com.redshark.entity.Table.Status;
import com.redshark.entity.User;
import com.redshark.entity.UserSession;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;
import com.redshark.texas.TexasDTO.UserScore;
import com.redshark.util.CommonUtil;
import com.redshark.util.StdRandom;

/**
 * 游戏核心逻辑
 * @author weswu
 *
 */
/**
 * @author weswu
 *
 */
public class GameLogic {
	//单例
	private static GameLogic instance;
	protected static final Logger logger = LoggerFactory.getLogger(GameLogic.class);
	protected static final ObjectMapper objectMapper = new ObjectMapper();
	//游戏全局变量
	//User需要额外增加的属性
	//table需要额外增加的属性
	private GameLogic() { }
	public static GameLogic getInstance(){
	       if(instance == null){
	           synchronized (GameLogic.class) {
	               if(instance == null){
	                   instance = new GameLogic();
	               }
	           }
	       }
	       return instance;
	}
	/**
	 * 从事件中提取命令
	 * @param event
	 * @return
	 */
	public String getCommand(Event event) 
	{
		String command = (String) event.getArgs().get("cmd");
		return command;
	}
	
	public FuncResult processCmd(Event event) throws Exception 
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		String command = getCommand(event);
		switch (command) {
		//玩家点击准备按钮后发送的请求
		case CommandType.BE_READY_REQ:
			funcResult = processBeReadyRequest(event);
			break;
		//玩家收到发牌介绍后发送的请求
		case CommandType.PUT_BETS_REQ:
			funcResult = processPutBetsRequest(event);
			break;
		//玩家点击再玩一次后发送的请求
		case CommandType.PLAY_GAME_AGAIN_REQ:
			funcResult = processPlayAgainRequest(event);
			break;
		//玩家点击出牌后发送的请求
		case CommandType.GAME_END_REQ:
			funcResult = processGameEndRequest(event);
			break;
		default:
			formattedCont = String.format("Command:%s does not exist.", command);
			funcResult = new FuncResult(false, formattedCont);
			break;
		}
		return funcResult;
	}
	
	
	/**
	 * 给房间增加额外的属性值
	 * @param room
	 */
	public void AddAttributesToTable(Room room) {
		if(null == room.getTable().getAttribute(Constants.EXTRA_ATTRIBUTES_KEY)) {
			ExtraAttributes extraAttributes = new ExtraAttributes(room.getTable().getSeatsNum());
			room.getTable().setAttribute(Constants.EXTRA_ATTRIBUTES_KEY, extraAttributes);
			//初始化游戏
			initGame(room);
		}
	}
	
	/**
	 * 初始化化一些游戏的变量
	 * @param table
	 */
	private void initGame(Room room) {
		//设置房间最大，最小带入
		ExtraAttrUtils.setMaxChips(room.getTable(), 2500);
		ExtraAttrUtils.setMinChips(room.getTable(), 250);
		//设置房间的大,小盲注值
		ExtraAttrUtils.setSmallBlindBet(room.getTable(), 5);
		ExtraAttrUtils.setBigBlindBet(room.getTable(), 10);
	}
	
	/**
	 * 启动游戏的请求, 每个用户点击"准备"按钮后就会进入这个处理环节
	 * 如果全部用户都准备了,则会产生一个发牌的事件
	 * @param event
	 * @return:FuncResult, null 表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processBeReadyRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		//返回的命令
		System.out.println("游戏开始请求");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//对table增加额外的属性
		AddAttributesToTable(room);
		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//检查是否已经投票了
		if(ExtraAttrUtils.isReady(room.getTable(), userId)){
			formattedCont = String.format("Please don't vote again!");
			funcResult = new FuncResult(false, formattedCont);
			return funcResult;
		}
		//设置该用户准备
		ExtraAttrUtils.setReady(room.getTable(), userId);
		//带入筹码
		assignChipsToUserForInRoom(room, event.getSession().getUser());
		//检查是否全部都已经准备了
		if(ExtraAttrUtils.isReadyComplete(room.getTable())) {
			//设置大小盲注的数量
			ExtraAttrUtils.setSmallBlindBet(room.getTable(), 5);
			ExtraAttrUtils.setBigBlindBet(room.getTable(), 10);
			//选庄
			pickDealerRes(event);
			//延时一定时间
			Thread.sleep(Constants.PICK_DEALER_SLEEP_IN_MS);
			//投放大小盲注
			startBlindBetRes(event);
			//返回null,表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
			return funcResult;
		}
		//否则,发送等待信息给请求者
		formattedCont = String.format("Please wait other to be ready...");
		funcResult = new FuncResult(true, formattedCont);
		return funcResult;
	}
	
	/**
	 * 向用户发送有选庄的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public FuncResult pickDealerRes(Event event) throws Exception {
		//返回的命令
		FuncResult funcResult = null;
		System.out.println("选庄...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//更新桌面的状态
		room.getTable().setStatus(Status.RUNNING);
		//设置 Pot
		Pot pot = new Pot(room.getTable().getUsers());
		ExtraAttrUtils.setPot(room.getTable(), pot);
		//随机选择一个位置为庄位
		int btnSeatNo = StdRandom.uniform(1, room.getTable().getSeatsNum() + 1);
		ExtraAttrUtils.setButtonSeatNo(room.getTable(), btnSeatNo);
		//移动桌面指针到庄位
		room.getTable().setCurrentSeatNo(btnSeatNo);
		//构建产生庄位的命令
		TexasDTO.PickDealerRes pdRes = new TexasDTO.PickDealerRes();
		pdRes.setCmd(CommandType.PICK_DEALER_RES);
		pdRes.setDealerSeatNo(btnSeatNo);
		Map<String, Object> args = objectMapper.convertValue(pdRes, Map.class);
		EventResponse pdResp = new EventResponse(0, EventType.GAME_TEXAS, true, args );
		room.sendBroadcastOnTable(pdResp.encode());
		return funcResult;
	}
	
	/**
	 * 为进入房间的用户分配筹码
	 * 
	 * @param room
	 * @param player
	 */
	public void assignChipsToUserForInRoom(Room room, User user) {
		int takeChip = ExtraAttrUtils.getMaxChips(room.getTable());
		// 如果玩家的所剩筹码不超过房间规定的最大带入筹码，则该玩家筹码全部带入
		if (user.getScore() < takeChip) {
			takeChip = user.getScore();
		}
		synchronized (user) {
			user.setScore(user.getScore() - takeChip);
			ExtraAttrUtils.setChip(room.getTable(), user.getId(), takeChip);
		}
	}
	

	/**
	 * 为用户初始化为房间最大带入
	 * 
	 * @param room
	 * @param player
	 */
	public void assignChipsToUserToRoomMax(Room room, User user) {
		// 玩家的钱多退少补，使其等于房间最大带入
		int maxChips = ExtraAttrUtils.getMaxChips(room.getTable());
		int inGameChips = ExtraAttrUtils.getChip(room.getTable(), user.getId());
		int takeChip = maxChips - inGameChips;
		// 如果玩家的所剩筹码不超过需要补足的，则带入所有
		if (user.getScore() < takeChip) {
			takeChip = user.getScore();
		}
		//同步玩家的筹码
		synchronized (user) {
			user.setScore(user.getScore() - takeChip);
			ExtraAttrUtils.setChip(room.getTable(), user.getId(), takeChip + inGameChips);
		}
	}
	
	/**
	 * 向用户发送有选庄的命令
	 * @param event
	 * @return
	 * @throws RsException
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult startBlindBetRes(Event event) throws Exception {
		//返回的命令
		FuncResult funcResult = null;
		System.out.println("下大小盲注...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//设置投注的轮
		ExtraAttrUtils.setBetRound(room.getTable(), BetRound.PREFLOP);
		//构建产生庄位的命令
		TexasDTO.StartBlindBetRes sbbRes = new TexasDTO.StartBlindBetRes();
		sbbRes.setCmd(CommandType.START_BLIND_BET_RES);
		//移动到小盲注位置
		UserSession smallBlindBetUserSession = getNextUserNotBroken(room);
		int smallBlindBetSeatNo = smallBlindBetUserSession.getUser().getSeatNo();
		sbbRes.setSmallBlindBetSeatNo(smallBlindBetSeatNo);
		//取得小盲注位用户Id
		String smallBlindBetUID = smallBlindBetUserSession.getUser().getId();
		//设置小盲注位的bet
		int smallBlindBet = ExtraAttrUtils.getSmallBlindBet(room.getTable());
		int sMaxBets = ExtraAttrUtils.getMaxBet(room.getTable(), smallBlindBetUID);
		int finalSBets = Math.min(smallBlindBet,sMaxBets);
		//投小盲注
		placeBet(room.getTable(), smallBlindBetUID, finalSBets);
		sbbRes.setSmallBlindBets(finalSBets);
		//移动到大盲注位置
		UserSession bigBlindBetUserSession = getNextUserNotBroken(room);
		int bigBlindBetSeatNo = bigBlindBetUserSession.getUser().getSeatNo();
		sbbRes.setBigBlindBetSeatNo(bigBlindBetSeatNo);
		//取得大盲注位用户Id
		String bigBlindBetUID = bigBlindBetUserSession.getUser().getId();
		//设置大盲注位的bet
		int bigBlindBet = ExtraAttrUtils.getBigBlindBet(room.getTable());
		int bMaxBets = ExtraAttrUtils.getMaxBet(room.getTable(), bigBlindBetUID);
		int finalBBets = Math.min(bigBlindBet,bMaxBets);
		//投大盲注
		placeBet(room.getTable(), bigBlindBetUID, finalBBets);
		sbbRes.setBigBlindBets(finalBBets);
		//更新最后一手相关的变量
		ExtraAttrUtils.setSmallBlindBet(room.getTable(), smallBlindBetSeatNo);
		ExtraAttrUtils.setLastChipsPutted(room.getTable(), finalBBets);
		ExtraAttrUtils.setLastSeatPutted(room.getTable(), room.getTable().getCurrentSeatNo());
		ExtraAttrUtils.setLastSeatOps(room.getTable(), OpsType.BET);
		//设置游戏状态
		//设置跟注的数量
		ExtraAttrUtils.setToCallAmount(room.getTable());
		Map<String, Object> args = objectMapper.convertValue(sbbRes, Map.class);
		EventResponse sbbResp = new EventResponse(0, EventType.GAME_TEXAS, true, args );
		room.sendBroadcastOnTable(sbbResp.encode());
		
		//延时一定时间,发送手牌
		Thread.sleep(Constants.START_DEAL_POCKET_POKERS_SLEEP_IN_MS);
		dealPocketPokersRes(event);
		
		//延时一定时间,发送投注命令
		Thread.sleep(Constants.START_PUT_BETS_SLEEP_IN_MS);
		//移动到大盲注位
		room.getTable().setCurrentSeatNo(bigBlindBetSeatNo);
		//移动到枪口位,发送投注的命令
		UserSession nextUserSession = getNextValidUser(room);
		//设置本轮加注位为枪口位
		ExtraAttrUtils.setLastRaisedSeatNo(room.getTable(), nextUserSession.getUser().getSeatNo());
		//设置跟注额
		ExtraAttrUtils.setToCallAmount(room.getTable());
		//构造通知投注的命令
		Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
		evtArgs.put("cmd", CommandType.PUT_BETS_RES);
		Event putBetsEvt = EventFactory.createRTEvent(0, nextUserSession, EventType.GAME_TEXAS, evtArgs);
		putBetsRes(putBetsEvt, false);
		//返回
		return funcResult;
	}
	
	/**
	* Places a bet for a specific user. The stack of the user is lowered with the bet amount and this amount is then
	* added to the pot. If the given bet size is larger than the remaining stack size of the user, then the bet is
	* lowered to the remaining stack size of the user, thereby putting him all-in. The bet size that is actually placed
	* is returned.
	* @param size : the desired size of the bet.
	* @param botIndex : the seat index of the user that places the bet.
	*/
	private int placeBet(Table table, String userId, int size)
	{
		int chips = ExtraAttrUtils.getChip(table, userId);
		//all in
		if (chips < size)
			size = chips;
		//更新用的筹码
		ExtraAttrUtils.setChip(table, userId, chips - size);
		//设置用户本轮的投注额,累加值
		ExtraAttrUtils.setRoundBet(table, userId, size);
		//将投注额加入奖池对象中
		Pot pot = ExtraAttrUtils.getPot(table);
		BetRound betRound = ExtraAttrUtils.getBetRound(table);
		pot.addBet(userId, size, betRound);
		return size;
	}
	
	/**
	 * 将用户提出房间
	 * @param room
	 * @param userId
	 */
	private void kickOutTheRoom(Room room, User user) {
		UserSession theUserSession = room.getTable().getUserSessionByUserId(user.getId());
		//触发room leave事件
		Map<String, Object> roomLeaveArgs = new HashMap<String, Object>();					
		roomLeaveArgs.put("id", room.getId().toString());
		roomLeaveArgs.put("content", "筹码不足.");
		Event roomLeaveEvt = EventFactory.createRTEvent(0, theUserSession, EventType.ROOM_LEAVE, roomLeaveArgs);
		ExecutorEventDispatcher.getInstance().fireEvent(roomLeaveEvt);
	}
	
	 /**
	   * @return the minimum amount needed to raise. This is the number of chips to raise to in the
	   *     current round of betting and does not include any pot contribution from previous betting
	   *     rounds. The player can also raise by going all-in if they do not have enough chips.
	   */
	public int getUseMinRaiseAmount(Room room, String userId) {
		int bigBlind = ExtraAttrUtils.getBigBlindBet(room.getTable());
		int toCallAmount = ExtraAttrUtils.getToCallAmount(room.getTable());
		int umrAmount = Math.max(bigBlind * 2, toCallAmount * 2);
		umrAmount = umrAmount - ExtraAttrUtils.getRoundBet(room.getTable(), userId);
		return umrAmount;
		
	}
	
	/**
	   * @return the minimum amount needed to call. This is the number of chips to call to in the
	   *     current round of betting and does not include any pot contribution from previous betting
	   *     rounds. The player can also call by going all-in if they do not have enough chips.
	   */
	public int getUserToCallAmount(Room room, String userId) {
		int toCallAmount = ExtraAttrUtils.getToCallAmount(room.getTable());
		int utcAmount = toCallAmount - ExtraAttrUtils.getRoundBet(room.getTable(), userId);
	    return utcAmount;
	}
	
	/**
	 * 取得当前用户可用的操作选项
	 * @param room
	 * @param userId
	 * @return
	 */
	public List<String> getAavilOpsTypes(Room room, String userId){
		List<String> opsTypes = new ArrayList<>();
		Map<String, Integer> roundBets = ExtraAttrUtils.getRoundBets(room.getTable());
		int toCallAmount = ExtraAttrUtils.getToCallAmount(room.getTable());
		//一直可用的选项
		opsTypes.add(OpsType.ALLIN.toString());
		opsTypes.add(OpsType.FOLD.toString());
		//如果没有人下注
		if(null == roundBets || 0 == CommonUtil.getMaxValue(roundBets)) {
			opsTypes.add(OpsType.BET.toString());
			opsTypes.add(OpsType.CHECK.toString());
		} else if(roundBets.containsKey(userId) && 0 != roundBets.get(userId) 
				&& roundBets.get(userId) == toCallAmount){
			opsTypes.add(OpsType.CHECK.toString());
			opsTypes.add(OpsType.RAISE.toString());
		} else {
			opsTypes.add(OpsType.CALL.toString());
			opsTypes.add(OpsType.RAISE.toString());
		}
		return opsTypes;
		
		
	}
	
	/**
	   * @return true if the hand is in the DONE state and all players except one folded.
	   */
	public boolean isWinDueToFolding(Room room) {
		BetRound betRound = ExtraAttrUtils.getBetRound(room.getTable());
	    return betRound == BetRound.HAND_DONE && getUsersInHand(room.getTable()).size() == 1;
	}
	
	/**
	 * 生成三张公共牌
	 * @param room
	 */
	public void generateBoardCars(Room room) {
		System.out.println("生成公共牌...");
		List<Poker> boardCards = new ArrayList<>();
		//取得Deck
		Deck deck = ExtraAttrUtils.getDeck(room.getTable());
		for (int i = 0; i < 3; i++ ) {
			Poker p = deck.drawCard();
			boardCards.add(p);
		}
		//保存Deck
		ExtraAttrUtils.setDeck(room.getTable(), deck);
		//保存
		ExtraAttrUtils.setBoardCards(room.getTable(), boardCards);
	}
	/**
	 * 向用户发公共牌-Flop(3),Turn(1),River(1)
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public FuncResult dealBoardPokersRes(Event event, boolean isFlop) throws Exception {
		//返回的命令
		FuncResult funcResult = null;
		System.out.println("发公共牌...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		List<Poker> boardCards = new ArrayList<Poker>();
		//发FLOP牌
		if(isFlop) {
			//取得三张公共牌
			boardCards = ExtraAttrUtils.getBoardCards(room.getTable());
		}else {
			Deck deck = ExtraAttrUtils.getDeck(room.getTable());
			//扔掉一张
			deck.drawCard();
			//取得一张牌
			boardCards.add(deck.drawCard());
			//保存Deck
			ExtraAttrUtils.setDeck(room.getTable(), deck);
		}
		//转换成可发送的格式
		List<Integer> boardCardIds = new ArrayList<>();
		for(Poker p: boardCards) {
			boardCardIds.add(p.toIndex());
		}
		//构建发公共牌的命令
		TexasDTO.DealBoardPokersRes dfpRes = new TexasDTO.DealBoardPokersRes();
		dfpRes.setCmd(CommandType.DEAL_BOARD_POKERS_RES);
		dfpRes.setPokers(boardCardIds);
		Map<String, Object> args = objectMapper.convertValue(dfpRes, Map.class);
		EventResponse dfpResp = new EventResponse(0, EventType.GAME_TEXAS, true, args );
		room.sendBroadcastOnTable(dfpResp.encode());
		return funcResult;
	}

	/**
	 * 向用户发手牌
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public FuncResult dealPocketPokersRes(Event event) throws Exception {
		//返回的命令
		FuncResult funcResult = null;
		System.out.println("发单牌...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//取得一幅新牌
		Deck deck = new Deck();
		deck.shuffle();
		//取得庄位,并移动到庄位
		int buttonSeatNo = ExtraAttrUtils.getButtonSeatNo(room.getTable());
		room.getTable().setCurrentSeatNo(buttonSeatNo);
		//取得小盲注位
		int smallBlindSeatNo = ExtraAttrUtils.getSmallBlindBet(room.getTable());
		//轮数的计数器
		int roundCount = 0;
		while(true) {
			//取得下一位用户,第一次应该是小盲注位
			UserSession nextSession = room.getTable().getNextUser();
			//如果是小盲注位,+1
			if(nextSession.getUser().getSeatNo() == smallBlindSeatNo ) {roundCount ++;}
			//如果轮数超过3,则退出
			if(roundCount == 3) {break;}
			//发牌
			//抽取一张牌
			Poker poker = deck.drawCard();
			//保存该用户的手牌
			List<Poker> pockeCards = ExtraAttrUtils.getPocketCards(room.getTable(), nextSession.getUser().getId());
			if(null == pockeCards) {pockeCards = new ArrayList<>();}
			pockeCards.add(poker);
			ExtraAttrUtils.setPocketCards(room.getTable(), nextSession.getUser().getId(), pockeCards);
			//构建发手牌的命令
			List<Integer> pocketPokerIds = new ArrayList<>();
			//将牌的Id加入发牌的数组
			pocketPokerIds.add(poker.toIndex());
			TexasDTO.DealPocketPokersRes dbpRes = new TexasDTO.DealPocketPokersRes();
			dbpRes.setCmd(CommandType.DEAL_POCKET_POKERS_RES);
			dbpRes.setPokers(pocketPokerIds);
			Map<String, Object> args = objectMapper.convertValue(dbpRes, Map.class);
			EventResponse dcResp = new EventResponse(0, EventType.GAME_TEXAS, true, args );
			nextSession.send(dcResp.encode());
		}
		//保存deck
		ExtraAttrUtils.setDeck(room.getTable(), deck);
		//生成公共牌,以防在PreFlop阶段所有人都弃牌,只剩一个用户
		generateBoardCars(room);
		//返回
		return funcResult;
	}
	
	 /**
	   * @return the players who are still in the hand (have not folded).
	 */
	private List<User> getUsersInHand(Table table) {
	    List<User> usersInHand = new ArrayList<User>();
	    List<User> allUsers = table.getUsers();
	    for(User u: allUsers) {
	    	boolean isFolded = ExtraAttrUtils.isFolded(table, u.getId());
	    	boolean isBroken = ExtraAttrUtils.isBroken(table, u.getId());
	    	if(!isFolded && ! isBroken) {
	    		usersInHand.add(u);
	    	}
	    }
	    return usersInHand;
	}
	
	/**
	   * @return the players who still have chips (have not gone broke).
	   */
	private List<User> getUsersWithChips(Table table) {
	    List<User> usersWithChips = new ArrayList<User>();
	    List<User> allUsers = table.getUsers();
	    for(User u: allUsers) {
	    	if(u.getScore() > 0) {
	    		usersWithChips.add(u);
	    	}
	    }
	    return usersWithChips;
	}
	
	/**
	 * 取得下一位没有破产的用户,如果全部破产则返回Null
	 * @param room
	 * @return null 表示全部都破产
	 * @throws RsException 
	 */
	private UserSession getNextUserNotBroken(Room room) throws RsException {
		UserSession nextUserSession = null;
		boolean isBroken = true;
		int count = 0;
		while (isBroken) {
			count ++;
			nextUserSession = room.getTable().getNextUser();
			isBroken = ExtraAttrUtils.isBroken(room.getTable(), nextUserSession.getUser().getId());
			//如果循环了一遍,没有找到用户，则跳出循环
			if(count >= room.getTable().getUsers().size() - 1) {
				nextUserSession = null;
				break;
			}
		}
		if(null == nextUserSession) {throw new RsException(ExceptionType.ALL_USERS_ARE_BROKEN);}
		return nextUserSession;
	}
	
	/**
	 * 一轮结束是发送的通知命令
	 * @param nextUserSession
	 * @param isRoundEnd
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	private void concludeBettingRound(UserSession nextUserSession, boolean isRoundEnd) throws Exception {
		//本轮结束,通知大家最后一个玩家的动作
    	Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
  		evtArgs.put("cmd", CommandType.PUT_BETS_RES);
  		Event putBetsEvt = EventFactory.createRTEvent(0, nextUserSession, EventType.GAME_TEXAS, evtArgs);
    	putBetsRes(putBetsEvt, true);
	}
	
	/**
	   * @return the next player to act in the current round of betting or null if the current round
	   *     of betting has concluded. The current round of betting concludes when the current actor
	   *     returns to the player who made the last raise.
	 * @throws Exception 
	   */
    @SuppressWarnings("serial")
	private UserSession getNextValidUser(Room room) throws Exception {
    	UserSession nextUserSession = getNextUserNotBroken(room);
    	//如果已经弃牌,或者AllIn,则跳过
	    while (ExtraAttrUtils.isFolded(room.getTable(), nextUserSession.getUser().getId())
	    		|| ExtraAttrUtils.isAllIn(room.getTable(), nextUserSession.getUser().getId())) {
	    	if (nextUserSession.getUser().getSeatNo() == ExtraAttrUtils.getLastRaisedSeatNo(room.getTable())) {
	    		//发送本轮结束的命令
	    		concludeBettingRound(nextUserSession, true);
	    		// Should only happen during the first betting round due to blinds.
	    		return null;
	    	}
	    	nextUserSession = getNextUserNotBroken(room);
	    }
	    if (nextUserSession.getUser().getSeatNo() == ExtraAttrUtils.getLastRaisedSeatNo(room.getTable())) {
	    	//发送本轮结束的命令
    		concludeBettingRound(nextUserSession, true);
    		//返回空
	    	return null;
	    }
	    return nextUserSession;
    }

    /**
	 * 开始新的一轮
	 * @param event
	 * @param commandType
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("serial")
	public void startBettingRound(Event event) throws Exception {
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//重置上一轮的变量
		ExtraAttrUtils.resetRoundVariable(room.getTable());
		//取得Button位,并移动到Button位
		int buttonSeatNo = ExtraAttrUtils.getButtonSeatNo(room.getTable());
		room.getTable().setCurrentSeatNo(buttonSeatNo);
		//移动桌面坐席,取得下一用户
		UserSession nextUserSession = getNextUserNotBroken(room);
		while (ExtraAttrUtils.isFolded(room.getTable(), nextUserSession.getUser().getId())) {
		      nextUserSession = getNextUserNotBroken(room);
		}
		//设置变量,最后一个加注的席位号
		ExtraAttrUtils.setLastRaisedSeatNo(room.getTable(), nextUserSession.getUser().getSeatNo());
		//构建事件
		Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
		evtArgs.put("cmd", CommandType.PUT_BETS_RES);
		Event nextUserEvt = EventFactory.createRTEvent(0, nextUserSession, EventType.GAME_DDZ, evtArgs);
		//发送投注命令
		putBetsRes(nextUserEvt, false);
	}
	
	/**
	 * 每个用户得到牌后,在UI端全部展现之后,应该发送一得到牌的请求,服务端会统计这个请求
	 * 当所有用户都确认得到牌之后，服务端会随机产生一个座位号,然后向用户发送叫地主的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial", "incomplete-switch" })
	public FuncResult processPutBetsRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		String formattedCont = null;
		System.out.println("投注请求...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		String userId = event.getSession().getUser().getId();
		//将Event的Args部分转成相应的类
		TexasDTO.PutBetsReq putBetsReq = EventFactory.getClass(event,  TexasDTO.PutBetsReq.class);
		//操作类型
		String opsType = putBetsReq.getOpsType();
		//校验当前用户的是否是轮到该进行操作的用户
		funcResult = userTurnValidation(event);
		if(null != funcResult){ return  funcResult;}
		//校验用户的操作是否合法
		List<String> availOpsType = getAavilOpsTypes(room, userId);
		boolean isFolded = ExtraAttrUtils.isFolded(room.getTable(), userId);
		if(!availOpsType.contains(opsType) || isFolded) {
			formattedCont = String.format("Invalid operation type: %s!", putBetsReq.getOpsType());
			funcResult = new FuncResult(false, formattedCont);
			return funcResult;
		}
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//这轮目前跟注的数量
		int toCallAmount = ExtraAttrUtils.getToCallAmount(room.getTable());
		//该用户可下的最大的注
		int maxBet = ExtraAttrUtils.getMaxBet(room.getTable(), userId);
		//这次投注的数量
		int amount = putBetsReq.getChips(); 
		//本轮全部的投注
		int roundTotalBets = amount + ExtraAttrUtils.getRoundBet(room.getTable(), userId);
		//如果是投注操作，最小的投注额
		int minBetAmount = ExtraAttrUtils.getBigBlindBet(room.getTable());
		//最小加注额
		int minRaiseAmount = getUseMinRaiseAmount(room, userId);
		//FOLD
		if(opsType.equals(OpsType.FOLD.toString())) {
			//设置该用户弃牌
	        ExtraAttrUtils.setFolded(room.getTable(), userId, true);
		//BET
		} else {
			if (roundTotalBets > toCallAmount && roundTotalBets > minBetAmount) {
				
			    // Bet or raise or All in.
	       	 	//下注,客户端需要控制类型和数量
	       	 	placeBet(room.getTable(), userId, amount);
	    		//设置最后加注的席位
				ExtraAttrUtils.setLastRaisedSeatNo(room.getTable(), event.getSession().getUser().getSeatNo());
			} else if ((roundTotalBets < toCallAmount|| roundTotalBets < minBetAmount) 
					&& maxBet > roundTotalBets) {
			    // Illegal action. Player did not bet at least the call amount
				// and has more chips (so not going all-in). Treat as fold.
				opsType = OpsType.FOLD.toString();
				//设置该用户棋牌
		        ExtraAttrUtils.setFolded(room.getTable(), userId, true);
			} else {
			    // Check or call or All in
				if(amount == 0) {opsType = OpsType.CHECK.toString();}
				//下注
	       	 	placeBet(room.getTable(), userId, amount);
			}
			//设置跟注的数量,这个必须在投注之后设置
       	 	ExtraAttrUtils.setToCallAmount(room.getTable());
			//检查是否是ALLIN,如果是更新操作类型
			if(roundTotalBets > 0 && roundTotalBets == maxBet) {opsType = OpsType.ALLIN.toString();}
		} 
		//更新最后一个席位的操作类型
        ExtraAttrUtils.setLastSeatOps(room.getTable(), OpsType.valueOf(opsType));
        //更新最后一个席位下注的数量
        ExtraAttrUtils.setLastChipsPutted(room.getTable(), amount);
        //更新最后一个操作的席位号
        ExtraAttrUtils.setLastSeatPutted(room.getTable(), event.getSession().getUser().getSeatNo());
		//继续游戏
        continueGame(event);
		//返回null
		return funcResult;
	}
	
	/**
	 * Continues the game after the current actor takes an action.
	 * @throws Exception 
	*/
	@SuppressWarnings("serial")
	private void continueGame(Event event) throws Exception {
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//检查是否是最后一个用户，如果是结束游戏
		List<User> usersInHand = getUsersInHand(room.getTable());
        // Hand ends due to only one player remaining.
		if (usersInHand.size() == 1) {
	        //设置最后一轮
	        ExtraAttrUtils.setBetRound(room.getTable(), BetRound.HAND_DONE);
	        //发送round结束的命令
	        putBetsRes(event, true);
	        //分配筹码
	        distributePot(room.getTable());
	        //本局游戏结束
	        subGameEndRes(event);
	        //返回
	        return;
		}
		//取得下一个用户
	   	UserSession uSession = getNextValidUser(room);
		//如果下一个用户是有效用户，表示本轮没有结束
	    if (null != uSession) {
	    	//向下一个用户发送投注命令
			Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
			evtArgs.put("cmd", CommandType.PUT_BETS_RES);
			Event putBetsEvt = EventFactory.createRTEvent(0, uSession, EventType.GAME_TEXAS, evtArgs);
			putBetsRes(putBetsEvt, false);
		//上轮结束，开始新的一轮
	    } else {
	    	//取得轮次的状态
	        BetRound betRound = ExtraAttrUtils.getBetRound(room.getTable());
	        switch (betRound) {
	          case PREFLOP:
		          // Deal the flop.
	        	  System.out.println("发公共牌FLOP...");
		          dealBoardPokersRes(event, true);
		          startBettingRound(event);
		          ExtraAttrUtils.setBetRound(room.getTable(), BetRound.FLOP);
		          break;
	          
	           case FLOP:
		           // Deal the turn.
	        	   System.out.println("发公共牌TURN...");
	        	   dealBoardPokersRes(event, false);
		           startBettingRound(event);
		           ExtraAttrUtils.setBetRound(room.getTable(), BetRound.TURN);
		           break;
	          
	           case TURN:
		           // Deal the river.
	        	   System.out.println("发公共牌RIVER...");
	        	   dealBoardPokersRes(event, false);
		           startBettingRound(event);
		           ExtraAttrUtils.setBetRound(room.getTable(), BetRound.RIVER);
		           break;
	          
	           case RIVER:
	        	   System.out.println("发牌结束SHOWDOWN...");
		           // Award winners by showdown.
	        	   //getWinnersByShowdown(room);
		           //awardWinners(room);
	        	   //分配奖励的筹码
		           distributePot(room.getTable());
		           //clearPlayersReadyStatus();
		           ExtraAttrUtils.setBetRound(room.getTable(), BetRound.HAND_DONE);
		           //发送子游戏结束的命令
		           subGameEndRes(event);
		           break;
	           default:
	        	  break;
	      }
	    }
	 }

	
	
	/**
	 * 发送投注的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult putBetsRes(Event event, boolean isRoundOver) throws Exception {
		FuncResult funcResult = null;
		System.out.println("开始下注...");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//如果不是本轮结束则需要设置定时
		if (!isRoundOver) {
			//设置一个投注超时的定时任务
			Map<String, Object> ptEvtArgs =  new HashMap<String, Object>(){};
			ptEvtArgs.put("tasktype", TimedTaskType.PUT_BETS_TIMEOUT);
			Event ptEvt = EventFactory.createTDEvent(0, event.getSession(), EventType.GAME_TEXAS_TIMED_TASK, ptEvtArgs, 
					System.currentTimeMillis() + Constants.PUT_BETS_TIMEOUT, 0, false);
			ExecutorEventDispatcher.getInstance().ScheduleEvent(ptEvt);
			//将定时任务加入table,以备删除
			room.getTable().setTimedEvent(ptEvt);
		}

		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//当前席位设置默认值
		int currentSeatNo = -1;
		//如果不是本轮结尾,则应该更新席位号为有效席位号
		if(!isRoundOver) {currentSeatNo = room.getTable().getCurrentSeatNo();}
		//构建叫地主的命令
		TexasDTO.PutBetsRes pbRes = new TexasDTO.PutBetsRes();
		pbRes.setCmd(CommandType.PUT_BETS_RES);
		pbRes.setCurrentSeatNo(currentSeatNo);
		pbRes.setMinBetAmount(ExtraAttrUtils.getBigBlindBet(room.getTable()));
		pbRes.setMinRaiseAmount(getUseMinRaiseAmount(room, userId));//设置改用户最下的加注额
		pbRes.setToCallAmount(getUserToCallAmount(room, userId));//设置该用户最小的跟注额
		pbRes.setAvailOpsTypes(getAavilOpsTypes(room, userId));
		pbRes.setLastChipsPutted(ExtraAttrUtils.getLastChipsPutted(room.getTable()));
		pbRes.setLastSeatPutted(ExtraAttrUtils.getLastSeatPutted(room.getTable()));
		pbRes.setLastSeatOps(ExtraAttrUtils.getLastSeatOps(room.getTable()).toString());
		Map<String, Object> args = objectMapper.convertValue(pbRes, Map.class);
		EventResponse pbResp = new EventResponse(0, EventType.GAME_TEXAS, true, args );
		room.sendBroadcastOnTable(pbResp.encode());
		return funcResult;
	}
	
	
	
	/**
	 * 处理投注超时操作
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult putBetsTimeout(Event event) throws Exception {
		FuncResult funcResult = null;
		System.out.println("下注超时...");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//可用的操作,超时默认的是CHECK
		OpsType oType = OpsType.CHECK;
		//如果CHECK不可用,则用FOLD
		List<String> availOpsTypes = getAavilOpsTypes(room, event.getSession().getUser().getId());
		if(!availOpsTypes.contains(OpsType.CHECK.toString())) {oType = OpsType.FOLD;}
		//构造一个虚拟的请求, 让牌
		Map<String, Object> pbEvtArgs =  new HashMap<String, Object>(){};
		pbEvtArgs.put("cmd", CommandType.PUT_BETS_REQ);
		pbEvtArgs.put("opsType", oType.toString());
		pbEvtArgs.put("chips", 0);
		event.setArgs(pbEvtArgs);
		processPutBetsRequest(event);
       	return funcResult;
	}
	
	/**
	 * 某个用户的私牌好公共牌构建一个最佳的手牌
	 * */
	public void constructBestHand(Table table, String userId) {
	    List<Poker> cards = new ArrayList<Poker>();
	    List<Poker> boardCards = ExtraAttrUtils.getBoardCards(table);
	    List<Poker> pocketCards = ExtraAttrUtils.getPocketCards(table, userId);
	    if(null != boardCards) {cards.addAll(boardCards);}
	    if(null != pocketCards) {cards.addAll(pocketCards);}
	    Hand bestHand = new HandPool(cards).getBestHand();
	    ExtraAttrUtils.setBestHand(table, userId, bestHand);
	}
	 /**
	   * @return the winning players determined by show down.
	   */
	private void getWinnersByShowdown(Room room) {
	    Hand bestHand = null;
	    List<User> winners = new ArrayList<User>();
	    List<User> allUsers = room.getTable().getUsers();
		for(User u: allUsers) {
	      if (!ExtraAttrUtils.isFolded(room.getTable(), u.getId())) {
	    	constructBestHand(room.getTable(), u.getId());
	    	Hand uBestHand = ExtraAttrUtils.getBestHand(room.getTable(), u.getId());
	        if (bestHand == null || bestHand.compare(uBestHand) < 0) {
	          bestHand = uBestHand;
	          winners.clear();
	          winners.add(u);
	        } else if (bestHand.compare(uBestHand) == 0) {
	          winners.add(u);
	        }
	      }
	    }
	    ExtraAttrUtils.setWinners(room.getTable(), winners);
	}
	
	/**
	 * Method for distributing the pot to the users at the end of a hand. Checks whether there is a showdown or not. If
	 * there is a showdown, the winners are determined and they receive the part of the pot that they deserve. If there
	 * is no showdown, the only remaining use receives the whole pot.
	 */
	private void distributePot(Table table)
	{	
		// check whether everyone but one player folded, if not we have to compute hand strengths
		List<User> usersInHand = getUsersInHand(table);
		//有可能在发手牌阶段,就出现所有用户弃牌,只剩一个用户
		// create a map of best hand of all the involved users
		HashMap<User, Hand> userBestHands = new HashMap<User, Hand>();
		for(User u: usersInHand) {
		    constructBestHand(table, u.getId());
		    Hand uBestHand = ExtraAttrUtils.getBestHand(table, u.getId());
		    userBestHands.put(u, uBestHand);
		}
		// retrieve the information about the main pot and side pots and the winning users per pot part
		Pot pots = ExtraAttrUtils.getPot(table);
		Pot.PayoutWinnerInfo winnerInfo = pots.payoutWinners(userBestHands);
		ArrayList<Integer> potParts = winnerInfo.getPots();
		ArrayList<ArrayList<User>> potPartWinners = winnerInfo.getWinnerPerPot();
		
		// divide each pot part among the users that win them
		for(int i = potParts.size() - 1; i >= 0; i--)
		{
			ArrayList<User> currentPotWinners = potPartWinners.get(i);
			int currentPotSize = potParts.get(i);
			int numberOfWinners = currentPotWinners.size();
			int amountPerWinner = currentPotSize / numberOfWinners;
			int restChips = currentPotSize - (numberOfWinners * amountPerWinner);
			for (int j = 0; j < currentPotWinners.size(); j++) {
				int chipsAwarded = amountPerWinner;
				if (restChips > 0) {
					chipsAwarded++;
			        restChips--;
				}
				//更新筹码
				ExtraAttrUtils.setChipsAwarded(table, currentPotWinners.get(j).getId(), chipsAwarded);
				System.out.println("Chips Awarded:" + currentPotWinners.get(j).getId() + "-" + chipsAwarded);
			}
		}
	}
	
	 /** 
	   * 简单的奖池计算,不包括边池
	   * Awards the winners of a hand the chips in the pot.
	   * There may be more than one winner if the best hand values are equal.
	   */
	private void awardWinners(Room room) {
	    // Determine the lowest pot contribution among the winners.
	    int lowestPotContribution = Integer.MAX_VALUE;
	    List<User> winners = ExtraAttrUtils.getWinners(room.getTable());
	    for (int i = 0; i < winners.size(); i++) {
	    	int winnerPC = ExtraAttrUtils.getPot(room.getTable()).getBet(winners.get(i));
	        if (winnerPC < lowestPotContribution) {
	        	lowestPotContribution = winnerPC;
	        }
	    }
	    
	    // Return any extra chips to players.
	    int mainPot = 0;
	    List<User> allUsers = room.getTable().getUsers();
	    for(User u: allUsers) {
	    	if(!ExtraAttrUtils.isBroken(room.getTable(), u.getId())) {
	    		int uPotConb = ExtraAttrUtils.getPot(room.getTable()).getBet(u);
	    		if (uPotConb > lowestPotContribution) {
	    			ExtraAttrUtils.setChipsAwarded(room.getTable(), u.getId(), uPotConb - lowestPotContribution);
	  	          	mainPot += lowestPotContribution;
	  	        } else {
	  	        	mainPot += uPotConb;
	  	        }
	    	}
	    }
	    
	    // Award chips in the main pot.
	    int chipsEach = mainPot / winners.size();
	    int remainder = mainPot - chipsEach * winners.size();
	    for (int i = 0; i < winners.size(); i++) {
	      int chipsAwarded = chipsEach;
	      if (remainder > 0) {
	        chipsAwarded++;
	        remainder--;
	      }
	      ExtraAttrUtils.setChipsAwarded(room.getTable(), winners.get(i).getId(), chipsAwarded);
	    }
	}
	  

	/**
	 * 校验当前用户是不是该出牌
	 * @param event
	 * @return
	 */
	public FuncResult userTurnValidation(Event event) {
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//如果最后一次出牌的不是自己,则需要进行比较
		int currentUserSeatNo = room.getTable().getSeatNoByUserId(event.getSession().getUser().getId());
		if (currentUserSeatNo != room.getTable().getCurrentSeatNo()){
			formattedCont = String.format("%s, 操作错误,还没有轮到您操作.", event.getSession().getUser().getName());
			funcResult = new FuncResult(false, formattedCont);
			return funcResult;
		}
		return funcResult;
	}
	
	
	/**
	 * 检查用户是否是赢家
	 */
	public boolean isWinner(int seatNo,int winnerSeatNo, int landlordSeatNo) {
		//检查该席位是否是赢家
		if(seatNo == winnerSeatNo) {return true;}
		//如果该席位不是赢家,则表明该席位是地主或者是农民
		else {
			//如果地主输了,则赢的肯定是农民
			if(winnerSeatNo != landlordSeatNo){return true;}
			//否则是农民，输了
			else {return false;}
		}
	}
	/**
	 * 游戏一局结束后更新用户的积分情况
	 */
	public List<UserScore> getUserScores(int winnerSeatNo, Table table) {
		List<UserScore> userScores = new ArrayList<>();
		List<User> users = table.getUsers();
		List<User> winners = new ArrayList<>();
		int toatalAvailScore = 0;
		
		return userScores;
	}
	
	/**
	 * 本局游戏结束的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult subGameEndRes(Event event) throws Exception {
		FuncResult funcResult = null;
		System.out.println("本局游戏结束.");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//更新用户的积分情况
		List<UserScore> userScores = getUserScores(event.getSession().getUser().getSeatNo(), room.getTable());
		//检查是否有破产的情况
		boolean isSomeOneBroken = false;
		for (UserScore uScore : userScores)
		{
		    if(uScore.isBankrupt()) {
		    	isSomeOneBroken = true;
		    	break;
		    }
		}
		//是否达到规定的局数
		boolean reachGameRounds = false;
		if(ExtraAttrUtils.getCurrentRoundNo(room.getTable()) >= room.getRounds()) {
			reachGameRounds = true;
		}
		//设置本局游戏结束命令
		TexasDTO.SubGameEndRes sgeRes = new TexasDTO.SubGameEndRes();
		sgeRes.setCmd(CommandType.SUB_GAME_END_RES);
		sgeRes.setWinnerSeatNo(event.getSession().getUser().getSeatNo());
		sgeRes.setUserScores(userScores);
		sgeRes.setSomeOneBroken(isSomeOneBroken);
		sgeRes.setReachGameRounds(reachGameRounds);
		Map<String, Object> sgeArgs = objectMapper.convertValue(sgeRes, Map.class);
		EventResponse finalSgeRes = new EventResponse(0, EventType.GAME_DDZ, true, sgeArgs );
		room.sendBroadcastOnTable(finalSgeRes.encode());
		return funcResult;
	}
	
	/**
	 * 再来一局的请求, 每个用户点击"再来一局"按钮后就会进入这个处理环节
	 * 如果全部用户都点击再来一局,则会产生新的一局
	 * @param event
	 * @return:FuncResult, null 表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processPlayAgainRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		//返回的命令
		System.out.println("再来一局请求");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//将Event的Args部分转成相应的类
		TexasDTO.PlayGameAgainReq playAgainReq = EventFactory.getClass(event,  TexasDTO.PlayGameAgainReq.class);
		//设置该用户再来一局的vote
		ExtraAttrUtils.setPlayAgain(room.getTable(), userId, playAgainReq.getPlayAgain());
		//检查是否全部都已经vote
		if(ExtraAttrUtils.isPlayAgainComplete(room.getTable())) {
			//检查投票结果
			if(ExtraAttrUtils.shouldPlayAgain(room.getTable())) {
				//重新开始新的一局
				playGameAgainRes(event);
				//休眠一段时间
				Thread.sleep(Constants.PLAY_AGAIN_SLEEP_IN_MS);
				//重新发牌
				pickDealerRes(event);
				//返回null,表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
				return funcResult;
			} else {
				//结束游戏
				gameEndRes(event);
			}
		}
		//否则,发送等待信息给请求者
		formattedCont = String.format("请等待其他玩家的投票结果...");
		funcResult = new FuncResult(true, formattedCont);
		return funcResult;
	}
	
	/**
	 * 再来一局的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult playGameAgainRes(Event event) throws Exception {
		FuncResult funcResult = null;
		System.out.println("再来一局.");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//为下一局增加一个属性变量
		ExtraAttrUtils.addSubGame(room.getTable());
		//设置再来一局的命令
		TexasDTO.PlayGameAgainRes pgaRes = new TexasDTO.PlayGameAgainRes();
		pgaRes.setCmd(CommandType.PLAY_GAME_AGAIN_RES);
		pgaRes.setRound(ExtraAttrUtils.getCurrentRoundNo(room.getTable()));
		Map<String, Object> pgaArgs = objectMapper.convertValue(pgaRes, Map.class);
		EventResponse finalPgaRes = new EventResponse(0, EventType.GAME_DDZ, true, pgaArgs );
		room.sendBroadcastOnTable(finalPgaRes.encode());
		//返回
		return funcResult;
	}

	/**
	 * 处理游戏结束的请求
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public FuncResult processGameEndRequest(Event event) throws Exception {
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		//返回的命令
		System.out.println("处理结束游戏的请求.");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//如果桌子正在游戏中,则要结束游戏
		if(room.getTable().getStatus().equals(Status.RUNNING)) {
			funcResult = gameEndRes(event);
		}
		return funcResult;
	}
		
	/**
	 * 全部游戏结束的命令
	 * @param event
	 * @return
	 * @throws RsException
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult gameEndRes(Event event) throws Exception {
		FuncResult funcResult = null;
		System.out.println("游戏全部结束.");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//改变桌面的状态
		room.getTable().setStatus(Status.CLOSED);
		//设置游戏全部结束命令
		TexasDTO.GameEndRes geRes = new TexasDTO.GameEndRes();
		geRes.setCmd(CommandType.GAME_END_RES);
		geRes.setTotalScore(0);
		Map<String, Object> geArgs = objectMapper.convertValue(geRes, Map.class);
		EventResponse finalGeRes = new EventResponse(0, EventType.GAME_DDZ, true, geArgs );
		room.sendBroadcastOnTable(finalGeRes.encode());
		return funcResult;
	}
	
}
