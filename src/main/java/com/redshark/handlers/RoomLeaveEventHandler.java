package com.redshark.handlers;

import java.util.HashMap;
import java.util.Map;

import com.redshark.ddz.CommandType;
import com.redshark.ddz.DdzDTO;
import com.redshark.ddz.GameLogic;
import com.redshark.ddz.DdzDTO.GameEndReq;
import com.redshark.dto.EventDTO;
import com.redshark.dto.UserDTO;
import com.redshark.entity.FuncResult;
import com.redshark.entity.Room;
import com.redshark.entity.RoomType;
import com.redshark.entity.Rooms;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventHandler;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;

public class RoomLeaveEventHandler extends EventHandler {
	
	public RoomLeaveEventHandler()
	{
		this.eventType = EventType.ROOM_LEAVE;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		String content = null;
		try {
			EventDTO.RoomLeaveReq roomLeaveReq = EventFactory.getClass(event,  EventDTO.RoomLeaveReq.class);
			roomId = roomLeaveReq.getId();
			content = roomLeaveReq.getContent();
			Room room = Rooms.getInstance().get(roomId);
			//检查房间是否存在或者已经关闭
			if(null == room || room.getStatus().equals(Room.Status.CLOSED))
			{
				//返回
				return;
			}
			//发送消息,通知其他人
			//发送用户的信息给其他同桌用户
			EventDTO.RoomLeaveRes roomLeaveRes = new EventDTO.RoomLeaveRes();
			roomLeaveRes.setUserId(event.getSession().getUser().getId());
			roomLeaveRes.setRoomId(roomId);
			roomLeaveRes.setContent(content);
			Map<String, Object> args = objectMapper.convertValue(roomLeaveRes, Map.class);
			EventResponse othersLRResp = new EventResponse(0, EventType.ROOM_LEAVE_OTHERS, true, args );
			room.sendToOthersInRoom(event.getSession(), othersLRResp.encode());
			
			//如果是房间拥有者退出,则触发关闭房间的事件
			if(room.getOwner().equals(event.getSession().getUser().getId()))
			{
				if(room.getStatus().equals(Room.Status.RUNNING)) {
					//扣除房卡
				}
				//
				//发送room close事件
				Map<String, Object> roomCloseArgs = new HashMap<String, Object>();					
				roomCloseArgs.put("id", room.getId().toString());
				Event roomCloseEvt = EventFactory.createRTEvent(0, event.getSession(), EventType.ROOM_CLOSE, roomCloseArgs);
				ExecutorEventDispatcher.getInstance().fireEvent(roomCloseEvt);
				//返回消息给请求者
				formattedCont = String.format("退出房间:%s.%s.", roomId, content);			
				OnComplete(event, 0, 0, true, formattedCont);
				return;
				
			}
			//如果不是房间的创建者
			//如果正在进行,算偷跑
			if(room.getStatus().equals(Room.Status.RUNNING)) {
				//扣除房卡
				//改变游戏状态
				room.setStatus(Room.Status.CREATED);
			}
			//关闭游戏
			sendGameEndReq(event);
			//删除session
			room.leaveRoom(event.getSession());
			//返回消息给请求者
			formattedCont = String.format("退出房间:%s.%s.", roomId, content);			
			OnComplete(event, 0, 0, true, formattedCont);
			

	
		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s退出房间:%s时发生错误:%s."
					, event.getSession().getUser().getName()
					, roomId
					, ex.getMessage());
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, 0, false, formattedCont);
		}
	}
	
	/**
	 * 游戏结束
	 * @param room
	 * @throws Exception 
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public FuncResult sendGameEndReq(Event event) throws Exception {
		FuncResult funcResult = null;
		//格式化信息
		String formattedCont = null;
		//取得房间,房间的null检查已经在前面处理了
		Room room = Rooms.getInstance().get(event.getSession().getUser().getRoom());
		RoomType roomType = room.getType();
		switch (roomType) {
		//斗地主的房间
		case DDZ: 
			GameEndReq geReq = new DdzDTO.GameEndReq();
			geReq.setCmd(CommandType.GAME_END_REQ);
			geReq.setOthersLeave(true);
			Map<String, Object> args = objectMapper.convertValue(geReq, Map.class);
			event.setArgs(args);
			funcResult = GameLogic.getInstance().processCmd(event);
			break;
		//扎金花的房间
		case ZJH:
			//funcResult = processDealPokersEndRequest(event);
			break;
		//牛牛的房间
		case NIUNIU:
			//funcResult = processDealPokersEndRequest(event);
			break;
		default:
			formattedCont = String.format("房间类型:%s不存在.", roomType);
			funcResult = new FuncResult(false, formattedCont);
			break;
		}
		return funcResult;
	}
}
