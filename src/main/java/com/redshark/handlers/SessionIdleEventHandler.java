package com.redshark.handlers;

import com.redshark.data.RoomDao;
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

public class SessionIdleEventHandler extends EventHandler {
	RoomDao roomDao = new RoomDao();
	
	public SessionIdleEventHandler()
	{
		this.eventType = EventType.SESSION_IDLE;
	}
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		try 
		{
			System.out.println(event);
			
		} catch (Exception ex) {
	
		}
	}
}
