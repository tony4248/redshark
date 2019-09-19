package com.redshark.ddz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.ddz.DdzDTO.UserScore;
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
		case CommandType.DEAL_POKERS_END_REQ:
			funcResult = processDealPokersEndRequest(event);
			break;
		//玩家点击叫地主,或者叫分后发送的请求
		case CommandType.CALL_LANDLORD_REQ:
			funcResult = processCallLandlordRequest(event);
			break;
		//玩家点击底分加倍的请求
		case CommandType.ADD_SCORE_REQ:
			funcResult = processAddScoreRequest(event);
			break;
		//玩家点击出牌后发送的请求
		case CommandType.PLAY_POKERS_REQ:
			funcResult = processPlayPokersRequest(event);
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
			formattedCont = String.format("命令:%s不存在.", command);
			funcResult = new FuncResult(false, formattedCont);
			break;
		}
		return funcResult;
	}
	
	
	/**
	 * 给房间增加额外的属性值
	 * @param room
	 */
	public void AddAttributesToTable(Table table) {
		if(null == table.getAttribute(Constants.EXTRA_ATTRIBUTES_KEY)) {
			ExtraAttributes extraAttributes = new ExtraAttributes(table.getSeatsNum());
			table.setAttribute(Constants.EXTRA_ATTRIBUTES_KEY, extraAttributes);
		}
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
		AddAttributesToTable(room.getTable());
		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//如果已经准备了,直接返回,用于防止客户端双击,发送重复消息
		if(ExtraAttrUtils.isReady(room.getTable(), userId)) {return funcResult;}
		//设置该用户准备
		ExtraAttrUtils.setReady(room.getTable(), userId);
		//检查是否全部都已经准备了
		if(ExtraAttrUtils.isReadyComplete(room.getTable())) {
			//开始向用户发牌
			dealPokersRes(event);
			//返回null,表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
			return funcResult;
		}
		//否则,发送等待信息给请求者
		formattedCont = String.format("请等待其他玩家完成准备...");
		funcResult = new FuncResult(true, formattedCont);
		return funcResult;
	}
	
	/**
	 * 向用户发牌
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void dealPokersRes(Event event) throws Exception {
		//返回的命令
		DdzDTO.DealPokersRes dealPokersRes;
		System.out.println("发牌...");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		Map<Integer,List<Integer>> pokers = PokerUtils.getSplitPokerIds();
		//更新桌面的状态
		room.getTable().setStatus(Status.RUNNING);
		//保存到这局游戏的变量中
		ExtraAttrUtils.setPokers(room.getTable(), pokers);
		//发牌到每个席位,0位不用,从1开始
		for (int i = 1; i < room.getTable().getSeatsNum() + 1; i++ ) {
			dealPokersRes = new DdzDTO.DealPokersRes();
			dealPokersRes.setCmd(CommandType.DEAL_POKERS_RES);
			dealPokersRes.setPokers(pokers.get(i));
			UserSession usession = room.getTable().getUserBySeatNo(i);
			Map<String, Object> args = objectMapper.convertValue(dealPokersRes, Map.class);
			EventResponse dcResp = new EventResponse(0, EventType.GAME_DDZ, true, args );
			usession.send(dcResp.encode());
		}
	}
	
	/**
	 * 每个用户得到牌后,在UI端全部展现之后,应该发送一得到牌的请求,服务端会统计这个请求
	 * 当所有用户都确认得到牌之后，服务端会随机产生一个座位号,然后向用户发送叫地主的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processDealPokersEndRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		System.out.println("发牌结束.");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//更新用户的状态-发牌结束
		int dealCardsEndNum = ExtraAttrUtils.addDealPokersEndNum(room.getTable());
		//检查是否全部都已经发牌结束
		if(room.getTable().getSeatsNum() == dealCardsEndNum) {
			//延时一定时间
			Thread.sleep(500);
			//第一次可以叫地主的席位是个随机数
			Random random=new Random();
			int seatNo = 1 + random.nextInt(100) % room.getTable().getSeatsNum();
			room.getTable().setCurrentSeatNo(seatNo);
			UserSession usession = room.getTable().getCurrentUser();
			Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
			evtArgs.put("cmd", CommandType.CALL_LANDLORD_RES);
			Event callLandlordEvt = EventFactory.createRTEvent(0, usession, EventType.GAME_DDZ, evtArgs);
			callLandlordRes(callLandlordEvt, 0/* prevSeatNo */, false/* prevSeatAction */);
			//返回null,表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
			return funcResult;
		}
		//返回null
		return funcResult;
	}
	
	/**
	 * 构建call landlor的命令
	 * @param table
	 * @return
	 */
	public DdzDTO.CallLandlordRes buildCallLandlordRes(Table table, int prevSeatNo, boolean prevSeatAction){
		//构建叫地主命令的数据
		DdzDTO.CallLandlordRes clRes = new DdzDTO.CallLandlordRes();
		clRes.setCmd(CommandType.CALL_LANDLORD_RES);
		clRes.setCurrentSeatNo(table.getCurrentSeatNo());
		clRes.setLastCallLandlordSeatNo(ExtraAttrUtils.getLastCallLandlordSeatNo(table));
		clRes.setLastCallLandlordScore(ExtraAttrUtils.getLastCallLandlordScore(table));
		clRes.setPrevSeatNo(prevSeatNo);
		clRes.setPrevSeatAction(prevSeatAction);
		return clRes;
	}
	
	
	/**
	 * 叫地主，向全部用户发送叫地主命令，其中显示当前正在叫地主的席位号
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult callLandlordRes(Event event, int prevSeatNo, boolean prevSeatAction) throws Exception {
		FuncResult funcResult = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//检验是否全部叫过地主了,如果全部都叫过了则直接返回,用于防止客户端重复消息而导致的发出的重复命令
		if(ExtraAttrUtils.isCallLandlordComplete(room.getTable())) {return funcResult;}
		//设置一个叫地主超时的定时任务
		Map<String, Object> clEvtArgs =  new HashMap<String, Object>(){};
		clEvtArgs.put("tasktype", TimedTaskType.CALL_LANDLORD_TIMEOUT);
		Event clEvt = EventFactory.createTDEvent(0, event.getSession(), EventType.GAME_DDZ_TIMED_TASK, clEvtArgs, 
				System.currentTimeMillis() + Constants.CALL_LANDLORD_TIMEOUT, 0, false);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(clEvt);
		//将定时任务加入table,以备删除
		room.getTable().setTimedEvent(clEvt);
		//构建叫地主的命令
		DdzDTO.CallLandlordRes clRes = buildCallLandlordRes(room.getTable(), prevSeatNo, prevSeatAction);
		Map<String, Object> args = objectMapper.convertValue(clRes, Map.class);
		EventResponse clResp = new EventResponse(0, EventType.GAME_DDZ, true, args );
		room.sendBroadcastOnTable(clResp.encode());
		return funcResult;
	}
	
	
	
	/**
	 * 处理叫地主超时操作
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult callLandlordTimeout(Event event) throws Exception {
		FuncResult funcResult = null;
		//构造一个虚拟的请求, 叫零分,表示不叫分
		Map<String, Object> clEvtArgs =  new HashMap<String, Object>(){};
		clEvtArgs.put("cmd", CommandType.CALL_LANDLORD_REQ);
		clEvtArgs.put("score", 0);
		event.setArgs(clEvtArgs);
		processCallLandlordRequest(event);
       	return funcResult;
	}
	

	
	/**
	 * 将底牌和地主的手牌合并
	 * @param room
	 */
	public void mergeBottomPokerIntoLandlordHand(Room room, int landLordSeatNo) {
		//将底牌和手牌合并
		ExtraAttrUtils.addBottomToLandLord(room.getTable(), landLordSeatNo);
		//底牌置空
		ExtraAttrUtils.setBottomPokers(room.getTable(), null);
	}
		
	/**
	 * 处理叫分的请求
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processCallLandlordRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		System.out.println("处理叫地主的请求.");
		int prevSeatNo = 0;
		boolean prevSeatAction = false;
		//校验当前用户的是否是轮到该进行操作的用户
		funcResult = userTurnValidation(event);
		if(null != funcResult){ return  funcResult;}
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//将Event的Args部分转成相应的类
		DdzDTO.CallLandlordReq callLandlordReq = EventFactory.getClass(event,  DdzDTO.CallLandlordReq.class);
		//如果已经叫过地主了,则直接返回,用于防止客户端发送重复消息
		if(ExtraAttrUtils.isCallLandlord(room.getTable(), userId)) {return funcResult;}
		//更新叫地主的数据
		ExtraAttrUtils.setCallLandlord(room.getTable(), userId, callLandlordReq);
		prevSeatNo = event.getSession().getUser().getSeatNo();
		if(Constants.LANDLORD_INIT_SCORE == callLandlordReq.getScore()) {prevSeatAction = true;}
		//如果全部叫完了,且有地主产生, 则发送加分命令
		if(ExtraAttrUtils.isCallLandlordComplete(room.getTable()) 
				&& ExtraAttrUtils.isLandlordElected(room.getTable()))
		{	
			//地主产生,移动当前桌面指针到地主席位, 更新当前席位号和session信息
			Event theUserEvt  = moveToTheUser(event, CommandType.HAS_LANDLORD_RES, ExtraAttrUtils.getLandLordSeatNo(room.getTable()));
			//向全体用户广播地主产生的命令
			hasLandlordRes(theUserEvt, prevSeatNo, prevSeatAction);
			//在服务器端将底牌和地主的手牌合并
			mergeBottomPokerIntoLandlordHand(room, ExtraAttrUtils.getLandLordSeatNo(room.getTable()));
			//休眠一段时间
			Thread.sleep(Constants.HAS_LANDLORD_SLEEP_IN_MS);
			//向地主发出底分加倍的命令
			addScoreRes(theUserEvt);
			//返回
			return funcResult;
			
		}
		//全部都叫过了,但是没有地主,则荒牌
		if(ExtraAttrUtils.isCallLandlordComplete(room.getTable()) 
				&& !ExtraAttrUtils.isLandlordElected(room.getTable()))
		{
			//发送荒牌的命令
			noLandlordRes(event, prevSeatNo, prevSeatAction);
			//休眠一段时间
			Thread.sleep(Constants.NO_LANDLORD_SLEEP_IN_MS);
			//重新发牌
			dealPokersRes(event);
			//返回
			return funcResult;
		}

		//向下一个用户发送叫地主的命令
		Event nextUserEvt  = moveToNextUser(event, CommandType.CALL_LANDLORD_RES);
		callLandlordRes(nextUserEvt, prevSeatNo, prevSeatAction);
		//否则,发送等待信息给请求者
		formattedCont = String.format("您叫: %s分, 请等待其他玩家叫分...", callLandlordReq.getScore());
		funcResult = new FuncResult(true, formattedCont);
		return funcResult;
	}
	
	
	/**
	 * 产生地主的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public FuncResult hasLandlordRes(Event event, int prevSeatNo, boolean prevSeatAction) throws Exception {
		FuncResult funcResult = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//发送地主产生的命令
		DdzDTO.HasLandlordRes hlRes = new DdzDTO.HasLandlordRes();
		hlRes.setCmd(CommandType.HAS_LANDLORD_RES);
		hlRes.setLandLordSeatNo(ExtraAttrUtils.getLandLordSeatNo(room.getTable()));
		hlRes.setLandLordScore(ExtraAttrUtils.getLandLordScore(room.getTable()));
		hlRes.setBottomPokers(ExtraAttrUtils.getBottomPokers(room.getTable()));
		hlRes.setPrevSeatNo(prevSeatNo);
		hlRes.setPrevSeatAction(prevSeatAction);
		Map<String, Object> hlArgs = objectMapper.convertValue(hlRes, Map.class);
		EventResponse finalHlRes = new EventResponse(0, EventType.GAME_DDZ, true, hlArgs );
		room.sendBroadcastOnTable(finalHlRes.encode());
		return funcResult;
	}
	
	/**
	 * 发送没有产生地主的命令,荒局
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult noLandlordRes(Event event, int prevSeatNo, boolean prevSeatAction) throws Exception {
		FuncResult funcResult = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//重置本局相关的计算变量
		ExtraAttrUtils.resetCountValue(room.getTable());
		//发送无地主命令
		DdzDTO.NoLandlordRes nlRes = new DdzDTO.NoLandlordRes();
		nlRes.setCmd(CommandType.NO_LANDLORD_RES);
		nlRes.setPrevSeatNo(prevSeatNo);
		nlRes.setPrevSeatAction(prevSeatAction);
		Map<String, Object> nlArgs = objectMapper.convertValue(nlRes, Map.class);
		EventResponse finalNlRes = new EventResponse(0, EventType.GAME_DDZ, true, nlArgs );
		room.sendBroadcastOnTable(finalNlRes.encode());
		return funcResult;
	}
	
	/**
	 * 发送扣底牌的命令
	 * @param event
	 * @param landlordSeatNo
	 * @param landlordScore
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult addScoreRes(Event event) throws Exception {
		FuncResult funcResult = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//检验是否全部加过分了,如果全部都加过了则直接返回,用于防止客户端重复消息而导致的发出的重复命令
		if(ExtraAttrUtils.isAddScoreComplete(room.getTable())) {return funcResult;}
		//同时设置一个加分超时的定时任务
		Map<String, Object> asEvtArgs =  new HashMap<String, Object>(){};
		asEvtArgs.put("tasktype", TimedTaskType.ADD_SCORE_TIMEOUT);
		Event asEvt = EventFactory.createTDEvent(0, event.getSession(), EventType.GAME_DDZ_TIMED_TASK, asEvtArgs, 
				System.currentTimeMillis() + Constants.ADD_SCORE_TIMEOUT, 0, false);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(asEvt);
		//将定时任务加入table,以备删除
		room.getTable().setTimedEvent(asEvt);
		//向该用户发出加分的命令
		DdzDTO.AddScoreRes cbRes = new DdzDTO.AddScoreRes();
		cbRes.setCmd(CommandType.ADD_SCORE_RES);
		cbRes.setCurrentSeatNo(room.getTable().getCurrentSeatNo());
		cbRes.setLastAddScoreSeatNo(ExtraAttrUtils.getLastAddScoreSeatNo(room.getTable()));
		cbRes.setLastAddScore(ExtraAttrUtils.getLastAddScore(room.getTable()));
		Map<String, Object> cbArgs = objectMapper.convertValue(cbRes, Map.class);
		EventResponse finalCbRes = new EventResponse(0, EventType.GAME_DDZ, true, cbArgs );
		room.sendBroadcastOnTable(finalCbRes.encode());
		return funcResult;
	}
	
	/**
	 * 扣底牌超时的操作
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	public FuncResult addScoreTimeout(Event event) throws Exception {
		FuncResult funcResult = null;
		//构建加分的请求
		Map<String, Object> asEvtArgs =  new HashMap<String, Object>(){};
		asEvtArgs.put("cmd", CommandType.ADD_SCORE_REQ);
		asEvtArgs.put("score", 1);
		event.setArgs(asEvtArgs);
		processAddScoreRequest(event);
		return funcResult;
	}
	
	@SuppressWarnings({ "unchecked"})
	public FuncResult bottomScoreRes(Event event) throws Exception {
		FuncResult funcResult = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//向全部完成发送最终底分的情况
		DdzDTO.BottomScoreRes bsRes = new DdzDTO.BottomScoreRes();
		bsRes.setCmd(CommandType.BOTTOM_SCORE_RES);
		bsRes.setLandLordScore(ExtraAttrUtils.getLandLordScore(room.getTable()));
		bsRes.setLastAddScoreSeatNo(ExtraAttrUtils.getLastAddScoreSeatNo(room.getTable()));
		bsRes.setLastAddScore(ExtraAttrUtils.getLastAddScore(room.getTable()));
		Map<String, Object> cbArgs = objectMapper.convertValue(bsRes, Map.class);
		EventResponse finalCbRes = new EventResponse(0, EventType.GAME_DDZ, true, cbArgs );
		room.sendBroadcastOnTable(finalCbRes.encode());
		return funcResult;
	}
	
	
	/**
	 * 处理扣底牌的请求
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processAddScoreRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		String formattedCont =  null;
		System.out.println("加分请求");
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//取得用户Id
		String userId = event.getSession().getUser().getId();
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//将Event的Args部分转成相应的类
		DdzDTO.AddScoreReq addScoreReq = EventFactory.getClass(event,  DdzDTO.AddScoreReq.class);
		//如果已经加过分了,则直接返回,用于防止客户端发送重复消息
		if(ExtraAttrUtils.isAddScore(room.getTable(), userId)) {return funcResult;}
		//更新加分的数据,移动
		ExtraAttrUtils.setAddScore(room.getTable(), userId, addScoreReq.getScore());
		//如果还有人没加
		if(!ExtraAttrUtils.isAddScoreComplete(room.getTable())) {
			//向下一个用户发送叫地主的命令
			Event nextUserEvt  = moveToNextUser(event, CommandType.ADD_SCORE_RES);
			addScoreRes(nextUserEvt);
			//否则,发送等待信息给请求者
			formattedCont = String.format("您加的: %s倍, 请等待其他玩家加分...", addScoreReq.getScore());
			funcResult = new FuncResult(true, formattedCont);
			return funcResult;
		} 
		//都加过分了
		//发送最终底分的信息
		bottomScoreRes(event);
		//简化逻辑,直接休眠
		Thread.sleep(Constants.BOTTOM_SCORE_SLEEP_IN_MS);
		//地主产生,移动当前桌面指针到地主席位, 更新当前席位号和session信息
		Event theUserEvt  = moveToTheUser(event, CommandType.PLAY_POKERS_RES, ExtraAttrUtils.getLandLordSeatNo(room.getTable()));
		//初始化最后一手牌的席位号
		ExtraAttrUtils.setlastSeatPlayed(room.getTable(), room.getTable().getCurrentSeatNo());
		//发送出牌的命令
		playPokersRes(theUserEvt, 0 /* prevSeatNo */, false /* prevSeatAction */);
		//返回
		return funcResult;

	}
		

	/**
	 * 发送出牌的命令
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult playPokersRes(Event event, int prevSeatNo, boolean prevSeatAction) throws Exception {
		FuncResult funcResult = null;
		System.out.println("出牌命令.");
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//同时设置一个扣牌超时的定时任务
		Map<String, Object> cbEvtArgs =  new HashMap<String, Object>(){};
		cbEvtArgs.put("tasktype", TimedTaskType.PLAY_POKERS_TIMEOUT);
		Event clEvt = EventFactory.createTDEvent(0, event.getSession(), EventType.GAME_DDZ_TIMED_TASK, cbEvtArgs, 
				System.currentTimeMillis() + Constants.PLAY_POKERS_TIMEOUT, 0, false);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(clEvt);
		//将定时任务加入table,以备删除
		room.getTable().setTimedEvent(clEvt);
		//设置出牌的命令
		DdzDTO.PlayPokersRes ppRes = new DdzDTO.PlayPokersRes();
		ppRes.setCmd(CommandType.PLAY_POKERS_RES);
		ppRes.setCurrentSeatNo(room.getTable().getCurrentSeatNo());
		ppRes.setLastPokersPlayed(ExtraAttrUtils.getLastPokersPlayed(room.getTable()));
		ppRes.setLastSeatPlayed(ExtraAttrUtils.getlastSeatPlayed(room.getTable()));
		ppRes.setLastSeatPokersNum(ExtraAttrUtils.getLastSeatPokersNum(room.getTable()));
		ppRes.setLandLordScore(ExtraAttrUtils.getLandLordScore(room.getTable()));
		ppRes.setPrevSeatNo(prevSeatNo);
		ppRes.setPrevSeatAction(prevSeatAction);
		Map<String, Object> PpArgs = objectMapper.convertValue(ppRes, Map.class);
		EventResponse finalPpRes = new EventResponse(0, EventType.GAME_DDZ, true, PpArgs );
		room.sendBroadcastOnTable(finalPpRes.encode());
		return funcResult;
	}
	
	/**
	 * 出牌超时处理
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	public FuncResult playPokerTimeout(Event event) throws Exception {
		FuncResult funcResult = null;
		//构建不出牌的请求
		//TODO,智能出牌,或托管出牌
		Map<String, Object> ppEvtArgs =  new HashMap<String, Object>(){};
		ppEvtArgs.put("cmd", CommandType.PLAY_POKERS_RES);
		ppEvtArgs.put("pokers", null);
		event.setArgs(ppEvtArgs);
		processPlayPokersRequest(event);
		return funcResult;
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
	
	@SuppressWarnings("serial")
	public Event moveToNextUser(Event event, String commandType) {
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//移动桌面坐席,取得下一用户
		UserSession nextUserSession = room.getTable().getNextUser();
		Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
		evtArgs.put("cmd", commandType);
		Event nextUserEvt = EventFactory.createRTEvent(0, nextUserSession, EventType.GAME_DDZ, evtArgs);
		return nextUserEvt;
	}
	
	/**
	 * 移动到指定坐席的用户
	 * @param event
	 * @param commandType
	 * @param seatNo
	 * @return
	 */
	@SuppressWarnings("serial")
	public Event moveToTheUser(Event event, String commandType, int seatNo) {
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//设置当前坐席
		room.getTable().setCurrentSeatNo(seatNo);
		//取得当前用户的session
		UserSession theUserSession = room.getTable().getCurrentUser();
		Map<String, Object> evtArgs =  new HashMap<String, Object>(){};
		evtArgs.put("cmd", commandType);
		Event nextUserEvt = EventFactory.createRTEvent(0, theUserSession, EventType.GAME_DDZ, evtArgs);
		return nextUserEvt;
	}

	/**
	 * 处理出牌的请求
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public FuncResult processPlayPokersRequest(Event event) throws Exception
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		System.out.println("处理出牌请求.");
		int prevSeatNo = event.getSession().getUser().getSeatNo();
		boolean prevSeatAction = false;
		//校验当前用户的是否是轮到该进行操作的用户
		funcResult = userTurnValidation(event);
		if(null != funcResult){ return  funcResult;}
        //取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		//移除之前的定时事件
		Event timedEvt = room.getTable().getTimedEvent();
		if(null != timedEvt) {ExecutorEventDispatcher.getInstance().removeEvent(timedEvt);}
		//将Event的Args部分转成相应的类
		DdzDTO.PlayPokersReq playPokersReq = EventFactory.getClass(event,  DdzDTO.PlayPokersReq.class);
		//如果用户没出牌则向下一个用户发送出牌命令
		if(null == playPokersReq.getPokers()) 
		{
			//移动到下一个席位
			Event nextUserEvt =  moveToNextUser(event, CommandType.PLAY_POKERS_RES);
			playPokersRes(nextUserEvt, prevSeatNo, prevSeatAction);
			return funcResult;
		}
		//检查牌型是否合法
		PokerType pokerType = PokerTypeUtils.getType(playPokersReq.getPokers());
		if(null == pokerType) {
			//出牌错误,需要返回给发送者
			formattedCont = String.format("%s, 您出的牌错误,请重新出牌.", event.getSession().getUser().getName());
			funcResult = new FuncResult(false, formattedCont);
			return funcResult;
		}
		//如果最后一次出牌的不是自己,则需要进行比较
		int currentUserSeatNo = room.getTable().getSeatNoByUserId(event.getSession().getUser().getId());
		//如果最后一次出牌的不是自己,则需要进行比较
		int lastSeatPlayed = ExtraAttrUtils.getlastSeatPlayed(room.getTable());
		if(currentUserSeatNo != lastSeatPlayed & 0 != lastSeatPlayed) {
			//取得上一手牌,与上一手牌比较
			List<Integer> lastPokersPlayed = ExtraAttrUtils.getLastPokersPlayed(room.getTable());
			//如果上手牌不为空,且这手牌比上手牌小,则返回错误
			if( null != lastPokersPlayed && 
					!PokerCompareUtils.comparePokers(playPokersReq.getPokers(), lastPokersPlayed)) {
				//出牌错误,需要返回给发送者
				formattedCont = String.format("%s, 您出的牌错误,请重新出牌.", event.getSession().getUser().getName());
				funcResult = new FuncResult(false, formattedCont);
				return funcResult;
			}
		}
		//如果是炸弹,则需要更新炸弹的数量,更新最终的底分
		ExtraAttrUtils.updateBoomNum(room.getTable(), playPokersReq.getPokers());
		//减掉出去的牌
		ExtraAttrUtils.removePokersFromCurrentSeat(room.getTable(), playPokersReq.getPokers());
		//更新最后一手牌和席位,和有效出牌的步骤数
		ExtraAttrUtils.setLastPokersPlayed(room.getTable(), playPokersReq.getPokers());
		ExtraAttrUtils.setlastSeatPlayed(room.getTable(), room.getTable().getCurrentSeatNo());
		//发送成功的消息给对方,返回消息给客户端
		formattedCont = String.format("%s, 出牌成功.", event.getSession().getUser().getName());
		String resp = new EventResponse(event.getId(), EventType.GAME_DDZ, true, formattedCont).encode();
		event.getSession().send(resp);
		//简化逻辑,直接休眠
		Thread.sleep(500);
		//判断剩下的牌是否为零
		if(ExtraAttrUtils.isCurrentSeatWin(room.getTable())) {
			//设置赢家的席位号
			ExtraAttrUtils.setWinnerSeatNo(room.getTable(), event.getSession().getUser().getSeatNo());
			//计算得分,发送本局游戏结束命令
			subGameEndRes(event);
		} else {
			//设置最后一个席位出牌
			prevSeatAction = true;
			//否则,游戏继续,移动桌面坐席, 向下一个用户发送出牌的命令
			Event nextUserEvt = moveToNextUser(event, CommandType.PLAY_POKERS_RES);
			playPokersRes(nextUserEvt, prevSeatNo, prevSeatAction);
		}
		//返回null
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
		int landlordSeatNo = ExtraAttrUtils.getLandLordSeatNo(table);
		int landlordScore = ExtraAttrUtils.getLandLordScore(table);
		//先现处理输家,因为输家可能破产,积分有不够的情况
		for (User user : users) {
			DdzDTO.UserScore uScore = new DdzDTO.UserScore();
			//如果是输家
			if(!isWinner(user.getSeatNo(), winnerSeatNo, landlordSeatNo)) 
			{	
				int availScore = 0;
				boolean isLandlord = false;
				//如果地主是输家
				if(user.getSeatNo() == landlordSeatNo) {
					//计算可用的积分
					availScore = user.getScore() - 2 * landlordScore > 0 ? 2 * landlordScore : user.getScore();
					isLandlord = true;
				} else {
					//计算可用的积分
					availScore = user.getScore() - landlordScore > 0 ? landlordScore : user.getScore();
				}
				//更新用户的最终剩余积分
				user.setScore(user.getScore() - availScore );
				//累积扣掉的积分
				toatalAvailScore += availScore;
				//判断是否破产
				boolean isBankrupt = user.getScore() == 0 ? true : false;
				//更新值
				uScore.setLandlord(isLandlord);
				uScore.setBankrupt(isBankrupt);
				uScore.setName(user.getName());
				uScore.setSeatNo(user.getSeatNo());
				uScore.setScore(user.getScore());
				uScore.setLandlordScore(landlordScore);
				uScore.setFinalScore(-availScore);
				userScores.add(uScore);		
				
			} else {
				//将赢家加入队列稍后处理
				winners.add(user);
			}
			//处理赢家,将从输家扣掉的分加到输家上
			for (User winner : winners) {
				DdzDTO.UserScore winnerScore = new DdzDTO.UserScore();
				if(winners.size() == 1) {
					//更新用户的最终剩余积分
					user.setScore(winner.getScore() + toatalAvailScore);
					//更新结果
					winnerScore.setLandlord(true);
					winnerScore.setBankrupt(false);
					uScore.setName(winner.getName());
					uScore.setSeatNo(winner.getSeatNo());
					uScore.setScore(winner.getScore());
					uScore.setLandlordScore(landlordScore);
					uScore.setFinalScore(toatalAvailScore);
					userScores.add(winnerScore);
				} else {
					int score = 0;
					if(winner.getSeatNo() == winnerSeatNo) {
						score = (int) Math.ceil(toatalAvailScore/2);
					}else {
						score = (int) Math.floor(toatalAvailScore/2);
					}
					//更新用户的最终剩余积分
					user.setScore(user.getScore() + score);
					//更新结果
					winnerScore.setLandlord(false);
					winnerScore.setBankrupt(false);
					uScore.setName(winner.getName());
					uScore.setSeatNo(winner.getSeatNo());
					uScore.setScore(winner.getScore());
					uScore.setLandlordScore(landlordScore);
					uScore.setFinalScore(score);
					userScores.add(winnerScore);	
					
				}
			}
		}
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
		boolean isSomeOneBankrupt = false;
		for (UserScore uScore : userScores)
		{
		    if(uScore.isBankrupt()) {
		    	isSomeOneBankrupt = true;
		    	break;
		    }
		}
		//是否达到规定的局数
		boolean reachGameRounds = false;
		if(ExtraAttrUtils.getCurrentRoundNo(room.getTable()) >= room.getRounds()) {
			reachGameRounds = true;
		}
		//设置本局游戏结束命令
		DdzDTO.SubGameEndRes sgeRes = new DdzDTO.SubGameEndRes();
		sgeRes.setCmd(CommandType.SUB_GAME_END_RES);
		sgeRes.setWinnerSeatNo(event.getSession().getUser().getSeatNo());
		sgeRes.setUserScores(userScores);
		sgeRes.setSomeOneBankrupt(isSomeOneBankrupt);
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
		DdzDTO.PlayGameAgainReq playAgainReq = EventFactory.getClass(event,  DdzDTO.PlayGameAgainReq.class);
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
				dealPokersRes(event);
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
		DdzDTO.PlayGameAgainRes pgaRes = new DdzDTO.PlayGameAgainRes();
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
	 * @throws Exception
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
		DdzDTO.GameEndRes geRes = new DdzDTO.GameEndRes();
		geRes.setCmd(CommandType.SUB_GAME_END_RES);
		geRes.setTotalScore(0);
		Map<String, Object> geArgs = objectMapper.convertValue(geRes, Map.class);
		EventResponse finalGeRes = new EventResponse(0, EventType.GAME_DDZ, true, geArgs );
		room.sendBroadcastOnTable(finalGeRes.encode());
		return funcResult;
	}
	
}
