package com.redshark.handlers;

import com.redshark.texas.GameLogic;
import com.redshark.entity.FuncResult;
import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class GameTexasEventHandler extends EventHandler {
	public GameTexasEventHandler()
	{
		this.eventType = EventType.GAME_TEXAS;
	}
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		try {
			roomId = event.getSession().getUser().getRoom();
			Room room = Rooms.getInstance().get(roomId);
			//检查房间是否存在
			if(null == room)
			{
				formattedCont = String.format("房间:%s不存在或已经解散.", roomId);
				//返回消息并触发相应的事件
				OnComplete(event, 0, 0, false, formattedCont);
				//返回
				return;
			}
			FuncResult funcResult = GameLogic.getInstance().processCmd(event);
			//返回null,表示信息已经发给客户端了,后续不需要在发信息给请求的客户端
			if(null == funcResult) {return;}
			OnComplete(event, 0, 0, funcResult.isSuccess(), funcResult.getData().toString());

		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("处理用户:%s:的请求%s时发生错误:%s."
					, event.getSession().getUser().getName()
					, event.getArgs().toString()
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, 0, false, formattedCont);
		}
	}
}
