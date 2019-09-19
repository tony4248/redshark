package com.redshark.event;

import com.redshark.core.Constants;
import com.redshark.engine.EventScheduler;
import com.redshark.engine.ThreadPuddle;
import com.redshark.engine.ThreadPuddleFactory;
import com.redshark.event.Event;
import com.redshark.event.EventDispatcher;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author weswu
 *
 */
public class ExecutorEventDispatcher implements EventDispatcher
{
	protected static final Logger logger = LoggerFactory.getLogger(ExecutorEventDispatcher.class);
	private static ExecutorEventDispatcher instance;
	/* the event threads pool */
	private static ThreadPuddle eventHandlerThreadsPool;
	/* the factory of thread puddle class */
	private static ThreadPuddleFactory threadPuddleFactory;
	private static EventScheduler eventScheduler;
	
	private Map<Integer, List<EventHandler>> handlersByEventType;
	private List<EventHandler> genericHandlers;
	private boolean isShuttingDown;

	private ExecutorEventDispatcher()
	{
		this(new HashMap<Integer, List<EventHandler>>(2),
				new CopyOnWriteArrayList<EventHandler>());
		threadPuddleFactory = new ThreadPuddleFactory();
		threadPuddleFactory.setThreads(Constants.NUMBERS_OF_THREADS_IN_THREAD_POOL);
		threadPuddleFactory.setTaskLimit(Constants.NUMBERS_OF_THREADS_IN_THREAD_POOL * 100);
		threadPuddleFactory.setFifo(true);
		eventHandlerThreadsPool = threadPuddleFactory.build();
		/* 启动事件调度器 */
		eventScheduler = EventScheduler.getInstance(eventHandlerThreadsPool, handlersByEventType);
	}
	
	public static ExecutorEventDispatcher getInstance(){
        if(instance == null){
            synchronized (ExecutorEventDispatcher.class) {
                if(instance == null){
                    instance = new ExecutorEventDispatcher();
                }
            }
        }
        return instance;
    }

	public ExecutorEventDispatcher(
			Map<Integer, List<EventHandler>> handlersByEventType,
			List<EventHandler> genericHandlers)
	{
		this.handlersByEventType = handlersByEventType;
		this.genericHandlers = genericHandlers;
		this.isShuttingDown = false;
	}
	
	public ThreadPuddle getEventHandlerThreadsPool()
	{
		return eventHandlerThreadsPool;
	}

	@Override
	public void addHandler(EventHandler eventHandler)
	{
		int eventType = eventHandler.getEventType();
		synchronized (this)
		{
			if (eventType == EventType.ANY)
			{
				genericHandlers.add(eventHandler);
			}
			else
			{
				List<EventHandler> handlers = this.handlersByEventType
						.get(eventType);
				if (handlers == null)
				{
					handlers = new CopyOnWriteArrayList<EventHandler>();
					this.handlersByEventType.put(eventType, handlers);
				}

				handlers.add(eventHandler);
			}
		}
	}

	@Override
	public List<EventHandler> getHandlers(int eventType)
	{
		return handlersByEventType.get(eventType);
	}

	@Override
	public void removeHandler(EventHandler eventHandler)
	{
		int eventType = eventHandler.getEventType();
		synchronized (this)
		{
			if (eventType == EventType.ANY)
			{
				genericHandlers.remove(eventHandler);
			}
			else
			{
				List<EventHandler> handlers = this.handlersByEventType
						.get(eventType);
				if (null != handlers)
				{
					handlers.remove(eventHandler);
					// Remove the reference if there are no listeners left.
					if (handlers.size() == 0)
					{
						handlersByEventType.put(eventType, null);
					}
				}
			}
		}

	}

	@Override
	public void removeHandlersForEvent(int eventType)
	{
		synchronized (this)
		{
			List<EventHandler> handlers = this.handlersByEventType
					.get(eventType);
			if (null != handlers)
			{
				handlers.clear();
			}
		}
	}

	@Override
	public synchronized void clear()
	{
		if(null != handlersByEventType)
		{
			handlersByEventType.clear();
		}
		if(null != genericHandlers)
		{
			genericHandlers.clear();
		}
	}
	
	@Override
	public void fireEvent(final Event event)
	{
		boolean isShuttingDown = false;
		synchronized (this)
		{
			isShuttingDown = this.isShuttingDown;
		}
		if (!isShuttingDown)
		{
			eventHandlerThreadsPool.run(new Runnable()
			{

				@Override
				public void run()
				{
					for (EventHandler handler : genericHandlers)
					{
						handler.onEvent(event);
					}

					// retrieval is not thread safe, but since we are not
					// setting it to
					// null anywhere it should be fine.
					List<EventHandler> handlers = handlersByEventType
							.get(event.getType());
					// Iteration is thread safe since we use copy on write.
					if (null != handlers)
					{
						for (EventHandler handler : handlers)
						{
							handler.onEvent(event);
						}
					}

				}
			});

		}
		else
		{
			logger.error("Discarding event: " + event + " as dispatcher is shutting down");
		}

	}
	
	@Override
	public void ScheduleEvent(final Event event)
	{
		boolean isShuttingDown = false;
		synchronized (this)
		{
			isShuttingDown = this.isShuttingDown;
		}
		if (!isShuttingDown)
		{
			eventScheduler.addEvent(event);

		}
		else
		{
			System.err.println("Discarding event: " + event
					+ " as dispatcher is shutting down");
		}

	}
	
	/**
	 * 取消事件
	 * @param event
	 */
	public void removeEvent(final Event event)
	{
		eventScheduler.removeEvent(event);
	}
	
	@Override
	public void close()
	{
		synchronized (this)
		{
			isShuttingDown = true;
			genericHandlers.clear();
			handlersByEventType.clear();
		}

	}

}
