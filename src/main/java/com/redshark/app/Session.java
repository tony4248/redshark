package com.redshark.app;


import com.redshark.event.Event;
import com.redshark.event.EventDispatcher;
import com.redshark.event.EventHandler;

import java.util.List;



public interface Session
{
	/**
	 * session status types
	 */
	enum Status
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}

	Object getId();

	void setId(Object id);

	void setAttribute(String key, Object value);

	Object getAttribute(String key);

	void removeAttribute(String key);

	void onEvent(Event event);

	EventDispatcher getEventDispatcher();


	boolean isWriteable();

	void setWriteable(boolean writeable);

	boolean isShuttingDown();
	
	long getCreationTime();

	long getLastReadWriteTime();

	void setStatus(Status status);

	Status getStatus();

	boolean isConnected();

	void addHandler(EventHandler eventHandler);
	
	void removeHandler(EventHandler eventHandler);
	
	List<EventHandler> getEventHandlers(int eventType);
	
	void close();
}


