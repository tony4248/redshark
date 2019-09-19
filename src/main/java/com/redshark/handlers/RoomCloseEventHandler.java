package com.redshark.handlers;

import com.redshark.data.RoomDao;
import com.redshark.dto.EventDTO;
import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class RoomCloseEventHandler extends EventHandler {
	RoomDao roomDao = new RoomDao();
	public RoomCloseEventHandler()
	{
		this.eventType = EventType.ROOM_CLOSE;
	}
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		try {
			EventDTO.RoomCloseReq roomCloseReq = EventFactory.getClass(event,  EventDTO.RoomCloseReq.class);
			roomId = roomCloseReq.getId();
			Room room = Rooms.getInstance().get(roomId);
			//检查房间是否存在
			if(null == room)
			{
				formattedCont = String.format("房间:%s不存在或已经关闭.", roomId);
				//返回消息并触发相应的事件
				OnComplete(event, 0, 0, true, formattedCont);
				//返回
				return;
			}
			//检查房间是否已经关闭
			if(room.getStatus().equals(Room.Status.CLOSED))
			{
				formattedCont = String.format("房间:%s已经关闭.", roomId); 
				//返回消息并触发相应的事件
				OnComplete(event, 0, 0, true, formattedCont);
				//返回
				return;
			}
			//检查提出请求者是否是房间拥有者
			if(!room.getOwner().equals(event.getSession().getUser().getId()))
			{
				formattedCont = String.format("只有房间的拥有者才能关闭房间:%s.", roomId); 
				//返回消息并触发相应的事件
				OnComplete(event, 0, 0, false, formattedCont);
				//返回
				return;
			}
			//发送信息给房间内所有人
			formattedCont = String.format("房间:%s即将关闭.", roomId); 
			room.sendBroadcastInRoom(formattedCont);
			//关闭房间, 删除在线缓存
			room.closeRoom(event.getSession());
			//更新数据库
			roomDao.updateById(room.getId(), room);
			//返回消息并触发相应的事件
			formattedCont = String.format("关闭房间:%s.", roomId); 
			OnComplete(event, 0, 0, true, formattedCont);

		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s关闭房间:%s时发生错误:%s."
					, event.getSession().getUser().getName()
					, roomId
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, 0, false, formattedCont);
		}
	}
}
