package com.redshark.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.event.Event;
import com.redshark.event.EventHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Created by wesley on 2018/5/10.
 */
public class EventScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EventScheduler.class);
    /* the event threads pool */
	private ThreadPuddle eventHandlerThreadsPool;
	private Map<Integer, List<EventHandler>> handlersByEventType;
	private ExecutorService schedulerEs;
    private static EventScheduler instance;
    
    private EventScheduler(ThreadPuddle eventHandlerThreadsPool, 
    		Map<Integer, List<EventHandler>> handlersByEventType)
    {
    	this.eventHandlerThreadsPool = eventHandlerThreadsPool;
    	this.handlersByEventType = handlersByEventType;
    	/* start the lease auto renew service */
    	startEventScheduler();
    }

    public static EventScheduler getInstance(ThreadPuddle eventHandlerThreadsPool,
    		Map<Integer, List<EventHandler>> handlersByEventType) {
        if (instance == null) {
            instance = new EventScheduler(eventHandlerThreadsPool,handlersByEventType);
        }
        return instance;
    }

    private PriorityBlockingQueue<Event> queue = new PriorityBlockingQueue<>(100, new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            if (o1.getNextTriggerTime() > o2.getNextTriggerTime()) {
                return 1;
            }else if (o1.getNextTriggerTime() < o2.getNextTriggerTime()){
                return -1;
            }
            return 0;
        }
    });
    
	private final void startEventScheduler(){

		/* Make it a daemon */
		schedulerEs = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){
		    public Thread newThread(Runnable r) {
		        Thread t = new Thread(r);
		        t.setDaemon(true);
		        return t;
		    }        
		});
		schedulerEs.submit(new EventSchedulerService());

	}

	private class EventSchedulerService implements Runnable
	{

		@Override
		public void run() {
			SchedulingEvents();
		}
	}
	
    public void SchedulingEvents() {
        while (true){
            long nowTime = System.currentTimeMillis();
            final Event evt = queue.peek();
            if(evt != null && evt.getNextTriggerTime() <= nowTime){
                try {
                 	/* 删除该item */
                	queue.poll();
                    this.eventHandlerThreadsPool.run(new Runnable()
        			{

        				@Override
        				public void run()
        				{
        					// retrieval is not thread safe, but since we are not
        					// setting it to
        					// null anywhere it should be fine.
        					List<EventHandler> handlers = handlersByEventType
        							.get(evt.getType());
        					// Iteration is thread safe since we use copy on write.
        					if (null != handlers)
        					{
        						for (EventHandler handler : handlers)
        						{
        							handler.onEvent(evt);
        						}
        					}

        				}
        			});
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
                /* 重新加入队列 */
                if(evt.isPeroid()){
                	int triggerCount = evt.getTriggerCount() + 1 ;
                	evt.setTriggerCount(triggerCount);
                    queue.add(evt);
                }
            }else{
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                	logger.error(ex.getMessage());
                }
            }
        }
    }

    public void addEvent(Event evt) {
       queue.add(evt);
    }

    public void removeEvent(Event evt) {
        queue.remove(evt);
    }



}
