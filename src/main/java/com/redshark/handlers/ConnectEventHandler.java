package com.redshark.handlers;

import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class ConnectEventHandler extends EventHandler {
	
	public ConnectEventHandler()
	{
		this.eventType = EventType.CONNECT;
	}
	@Override
	public void onEvent(Event event)
	{
		System.out.println("开始连接！");
	}
}
