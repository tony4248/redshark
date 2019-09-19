package com.redshark.handlers;

import java.util.List;
import java.util.Map;

import com.redshark.dto.EventDTO;
import com.redshark.dto.EventDTO.RoomJoinRes;
import com.redshark.dto.RoomDTO;
import com.redshark.dto.UserDTO;
import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.entity.User;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventHandler;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;

public class RoomJoinEventHandler extends EventHandler {
	
	public RoomJoinEventHandler()
	{
		this.eventType = EventType.ROOM_JOIN;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		try {
			EventDTO.RoomJoinReq roomJoinReq = EventFactory.getClass(event,  EventDTO.RoomJoinReq.class);
			roomId = roomJoinReq.getId();
			Room room = Rooms.getInstance().get(roomId);
			//检查房间是否存在
			if(null == room)
			{
				formattedCont = String.format("房间:%s不存在或已经解散.", roomId);
				//返回消息并触发相应的事件
				OnComplete(event, 0, EventType.ROOM_JOIN_FAILED, false, formattedCont);
				//返回
				return;
			}
			//检查人数是否已经达到房间上限
			if(room.getSessions().size() >= room.getCapacity())
			{
				formattedCont = String.format("房间:%s已满,无法加入.", roomId); 
				//返回消息并触发相应的事件
				OnComplete(event, 0, EventType.ROOM_JOIN_FAILED, false, formattedCont);
				//返回
				return;
			}
			//加入房间
			if (!room.joinRoom(event.getSession()))
			{
				formattedCont = String.format("已经加入房间:%s,请勿重复加入.", roomId);
				//返回消息并触发相应的事件
				OnComplete(event, 0, EventType.ROOM_JOIN_FAILED, false, formattedCont);
				//返回
				return;
			}
			//更新用户的当前房间设置
			event.getSession().getUser().setRoom(roomId);
			//发送房间信息和已有的用户信息给请求者
			RoomDTO roomDTO = RoomDTO.build(room);
			List<UserDTO> userDTOs = UserDTO.getUserDTOs(room.getTable().getUsers());
			EventDTO.RoomJoinRes roomJoinRes = new RoomJoinRes(roomDTO, userDTOs);
			Map<String, Object> args = objectMapper.convertValue(roomJoinRes, Map.class);
			EventResponse roomJoinResp = new EventResponse(event.getId(), EventType.ROOM_JOIN, true, args );
			event.getSession().send(roomJoinResp.encode());
			//返回消息并触发相应的事件,返回加入者的信息给其他人
			OnComplete(event, 0, EventType.ROOM_JOIN_SUCCESS, true, formattedCont);

		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s加入房间:%s时发生错误:%s."
					, event.getSession().getUser().getName()
					, roomId
					, ex.getMessage());
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, EventType.ROOM_JOIN_FAILED, false, formattedCont);
		}
	}
}
