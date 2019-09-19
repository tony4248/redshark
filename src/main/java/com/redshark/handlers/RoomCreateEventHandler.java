package com.redshark.handlers;

import java.util.List;
import java.util.Map;

import com.redshark.data.RoomDao;
import com.redshark.dto.EventDTO;
import com.redshark.dto.RoomDTO;
import com.redshark.dto.UserDTO;
import com.redshark.dto.EventDTO.RoomCreateRes;
import com.redshark.dto.EventDTO.RoomJoinRes;
import com.redshark.entity.Room;
import com.redshark.entity.RoomFactory;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventHandler;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;
import com.redshark.util.StdRandom;

public class RoomCreateEventHandler extends EventHandler {
	RoomDao roomDao = new RoomDao();
	
	public RoomCreateEventHandler()
	{
		this.eventType = EventType.ROOM_CREATE;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		try 
		{
			//取得房卡数量
			int cardNum = event.getSession().getUser().getCardNum();
			synchronized (RoomCreateEventHandler.class) 
			{
				if ((cardNum -= 1) < 0) 
				{
					//放回消息并触发相应事件
					formattedCont = String.format("房卡数量:%d,不足,房间创建失败.",event.getSession().getUser().getCardNum());
					OnComplete(event, 0, EventType.ROOM_CREATE_FAILED, false, formattedCont);
					return;
				}else {
					event.getSession().getUser().setCardNum(cardNum);
				}
				
			}
			Room room = RoomFactory.create(event);
			room.setOwner(event.getSession().getUser().getId());
			//加入在线缓存
			Rooms.getInstance().put(room.getId(), room);
			//存入库
			roomDao.save(room);
			//更新用户的当前房间设置
			event.getSession().getUser().setRoom(room.getId());
			//加入房间
			room.joinRoom(event.getSession());
			//加入参数,后续调用
			event.setArg("roomId", room.getId());
			//发送房间信息和已有的用户信息给请求者
			RoomDTO roomDTO = RoomDTO.build(room);
			UserDTO userDTO = UserDTO.build(event.getSession().getUser());
			EventDTO.RoomCreateRes roomCreateRes = new RoomCreateRes (roomDTO, userDTO);
			Map<String, Object> args = objectMapper.convertValue(roomCreateRes, Map.class);
			EventResponse roomCreateResp = new EventResponse(event.getId(), EventType.ROOM_CREATE, true, args );
			event.getSession().send(roomCreateResp.encode());
			//触发成功事件
			OnComplete(event, 0, EventType.ROOM_CREATE_SUCCESS, true, null);
			
		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户%s创建房间时发生错误:%s."
					,event.getSession().getUser().getName()
					,ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, EventType.ROOM_CREATE_FAILED, false, formattedCont);
		}
	}
}
