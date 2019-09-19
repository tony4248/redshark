package com.redshark.texas;

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
		case TimedTaskType.PUT_BETS_TIMEOUT:
			System.out.println("下注超时");
			funcResult = GameLogic.getInstance().putBetsTimeout(event);
			break;
		default:
			formattedCont = String.format("定时任务:%s不存在.", taskType);
			funcResult = new FuncResult(false, formattedCont);
			break;
		}
		
		return funcResult;
	}
}
