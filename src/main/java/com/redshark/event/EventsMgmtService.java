package com.redshark.event;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.handlers.ConnectEventHandler;
import com.redshark.handlers.ConnectFailedEventHandler;
import com.redshark.handlers.ConnectSuccessEventHandler;
import com.redshark.handlers.DisconnectEventHandler;
import com.redshark.handlers.GameDdzEventHandler;
import com.redshark.handlers.GameDdzTimedTaskEventHandler;
import com.redshark.handlers.GameTexasEventHandler;
import com.redshark.handlers.GameTexasTimedTaskEventHandler;
import com.redshark.handlers.LogOutEventHandler;
import com.redshark.handlers.RoomCloseEventHandler;
import com.redshark.handlers.RoomCreateEventHandler;
import com.redshark.handlers.RoomCreateFailedEventHandler;
import com.redshark.handlers.RoomCreateSuccessEventHandler;
import com.redshark.handlers.RoomJoinEventHandler;
import com.redshark.handlers.RoomJoinFailedEventHandler;
import com.redshark.handlers.RoomJoinSuccessEventHandler;
import com.redshark.handlers.RoomLeaveEventHandler;
import com.redshark.handlers.SessionIdleEventHandler;
import com.redshark.handlers.SessionTimeoutEventHandler;

/**
 * @author weswu
 *
 */
public class EventsMgmtService {
	public EventsMgmtService(){}
	
	public void registerEventHandlers()
	{
		ExecutorEventDispatcher.getInstance().addHandler(new ConnectEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new ConnectSuccessEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new ConnectFailedEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new DisconnectEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new SessionIdleEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new SessionTimeoutEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new LogOutEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomCreateEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomCreateSuccessEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomCreateFailedEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomJoinEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomJoinSuccessEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomJoinFailedEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomLeaveEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new RoomCloseEventHandler());
		/* 斗地主的handler */
		ExecutorEventDispatcher.getInstance().addHandler(new GameDdzEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new GameDdzTimedTaskEventHandler());
		/* 德州扑克的handler */
		ExecutorEventDispatcher.getInstance().addHandler(new GameTexasEventHandler());
		ExecutorEventDispatcher.getInstance().addHandler(new GameTexasTimedTaskEventHandler());

	}
    

}
