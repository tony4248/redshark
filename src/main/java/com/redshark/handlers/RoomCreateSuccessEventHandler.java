package com.redshark.handlers;

import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class RoomCreateSuccessEventHandler extends EventHandler {
	
	public RoomCreateSuccessEventHandler()
	{
		this.eventType = EventType.ROOM_CREATE_SUCCESS;
	}
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		try {
			roomId = (String) event.getArg("roomId");
			Room room = Rooms.getInstance().get(roomId);
			
		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s创建房间成功后:%s,发生错误:%s."
					, event.getSession().getUser().getName()
					, roomId
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
		}
	}
}
