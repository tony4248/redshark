package com.redshark.handlers;

import java.util.Map;

import com.redshark.dto.EventDTO;
import com.redshark.dto.UserDTO;
import com.redshark.dto.EventDTO.RoomJoinRes;
import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;

public class RoomJoinSuccessEventHandler extends EventHandler {
	
	public RoomJoinSuccessEventHandler()
	{
		this.eventType = EventType.ROOM_JOIN_SUCCESS;
	}
	
	@SuppressWarnings("unchecked")
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
				//记录日志
				logger.error(formattedCont);
				//返回
				return;
			}
		
			//发送用户的信息给其他同桌用户
			UserDTO userDTO = UserDTO.build(event.getSession().getUser());
			EventDTO.RoomJoinOthersRes roomJoinOthersRes = new EventDTO.RoomJoinOthersRes(userDTO);
			Map<String, Object> args = objectMapper.convertValue(roomJoinOthersRes, Map.class);
			EventResponse othersJRResp = new EventResponse(0, EventType.ROOM_JOIN_OTHERS, true, args );
			room.sendToOthersInRoom(event.getSession(), othersJRResp.encode());
			
			//检查Table是否已经满了
			if(room.getTable().isFull())
			{
				//发送信息给房间内的其它人
				formattedCont = String.format("房间:%s已满.", roomId);
				EventResponse evtResp = new EventResponse(0, EventType.TABLE_IS_READY, true, formattedCont);
				room.sendBroadcastOnTable(evtResp.encode());
				//返回
				return;
			}

		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s加入房间成功后:%s,发生错误:%s."
					, event.getSession().getUser().getName()
					, roomId
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
		}
	}
}
