package com.redshark.handlers;

import java.util.HashMap;
import java.util.Map;

import com.redshark.entity.Room;
import com.redshark.entity.Rooms;
import com.redshark.entity.Sessions;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;

import io.netty.channel.Channel;

public class DisconnectEventHandler extends EventHandler {

	public DisconnectEventHandler()
	{
		this.eventType = EventType.DISCONNECT;
	}
	
	@Override
	public void onEvent(Event event)
	{
		//2018.05.23,主动disconnect表示用户的强制退出,和超时不同
		//重新连接必须重新登录
		//格式化信息
		String formattedCont = null;
		String roomId = null;
		try {
			//检查room中是否含有该session,如果有删掉,发送room leave事件
			roomId = event.getSession().getUser().getRoom();
			if(null != roomId)
			{
				//查找room
				Room room = Rooms.getInstance().get(roomId);
				//如果找该room,并且room没有关闭
				if(null != room && !room.getStatus().equals(Room.Status.CLOSED)) 
				{
					//触发room leave事件
					Map<String, Object> roomLeaveArgs = new HashMap<String, Object>();					
					roomLeaveArgs.put("id", room.getId().toString());
					Event roomLeaveEvt = EventFactory.createRTEvent(0, event.getSession(), EventType.ROOM_LEAVE, roomLeaveArgs);
					ExecutorEventDispatcher.getInstance().fireEvent(roomLeaveEvt);
				}
			}
			//关闭channel
			Channel channel = event.getSession().getChannel();
			if (channel.isActive() || channel.isOpen()) {channel.close();}
			//从在线的session缓存中删掉该session
			//取得sessionId
			//String sessionId = event.getSession().getId();
			//Sessions.getInstance().delete(sessionId);
		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s断开连接时发生错误:%s."
					, event.getSession().getUser().getName()
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, EventType.ROOM_JOIN_FAILED, false, formattedCont);
		}
	}
}
