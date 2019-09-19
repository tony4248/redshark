package com.redshark.handlers;

import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class ConnectFailedEventHandler extends EventHandler {
	
	public ConnectFailedEventHandler()
	{
		this.eventType = EventType.CONNECT_FAILED;
	}
	
	@Override
	public void onEvent(Event event)
	{
		System.out.println("连接失败！");
	}
}
