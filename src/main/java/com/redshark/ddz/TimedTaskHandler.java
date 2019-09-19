package com.redshark.ddz;

import com.redshark.entity.FuncResult;
import com.redshark.event.Event;

public class TimedTaskHandler {

	public static String getTaskType(Event event) 
	{
		String taskType = (String) event.getArgs().get("tasktype");
		return taskType;
	}
	
	public static FuncResult processTask(Event event) throws Exception 
	{
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		String taskType = getTaskType(event);
		switch (taskType) {
		//叫地主超时
		case TimedTaskType.CALL_LANDLORD_TIMEOUT:
			System.out.println("叫地主超时");
			funcResult = GameLogic.getInstance().callLandlordTimeout(event);
			break;
		//地主扣牌超时
		case TimedTaskType.ADD_SCORE_TIMEOUT:
			System.out.println("地主加分超时");
			funcResult = GameLogic.getInstance().addScoreTimeout(event);
			break;
		//玩家出牌超时
		case TimedTaskType.PLAY_POKERS_TIMEOUT:
			System.out.println("出牌超时");
			funcResult = GameLogic.getInstance().playPokerTimeout(event);
			break;
		default:
			formattedCont = String.format("定时任务:%s不存在.", taskType);
			funcResult = new FuncResult(false, formattedCont);
			break;
		}
		
		return funcResult;
	}
}
