package com.redshark.handlers;

import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

public class ConnectSuccessEventHandler extends EventHandler {
	
	public ConnectSuccessEventHandler()
	{
		this.eventType = EventType.CONNECT_SUCCESS;
	}
	@Override
	public void onEvent(Event event)
	{
		//如果发现有session在cache 则可能时超时后重新连接
		//需要算下session里面的channel等数据
		//这里加上重新连接后,如果断线前有房间，则需要加入进入房间的逻辑
		System.out.println("连接成功！");
	}
}
