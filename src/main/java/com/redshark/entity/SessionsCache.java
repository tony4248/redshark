package com.redshark.entity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.core.Constants;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;

public class SessionsCache extends BaseCache {
	private static final Logger logger = LoggerFactory.getLogger(SessionsCache.class);
	private static SessionsCache instance;
	private static ExecutorService cacheAutoCleanupES;
	
	/* 构造函数 */
	private SessionsCache(){
		cacheStore = new ConcurrentHashMap<Object, CachedObject>(100, 0.9f, 1);
		capacity = Constants.ACTIVE_SESSION_CACHE_MAX_CAPACITY;
		expireTime = Constants.ACTIVE_SESSION_CACHE_TTL_MS;
		/* start the channel auto cleanup service */
		startCacheAutoCleanupService();
	}
	
	public static SessionsCache getInstance(){
        if(instance == null){
            synchronized (SessionsCache.class) {
                if(instance == null){
                    instance = new SessionsCache();
                }
            }
        }
        return instance;
    }
	
	private final void startCacheAutoCleanupService(){

			/* Make it a daemon */
			cacheAutoCleanupES = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){
			    public Thread newThread(Runnable r) {
			        Thread t = new Thread(r);
			        t.setDaemon(true);
			        return t;
			    }        
			});
			cacheAutoCleanupES.submit(new CacheAutoCleanupService());

	}
	
	public void finalize() {
		cacheAutoCleanupES.shutdown();
		cacheStore = null;
	}
	
	@Override
	public boolean trimToCapcity(){
    	return true;    	
    }
	
	@Override
	public UserSession get(Object cacheKey) {
		return (UserSession)super.get(cacheKey);
	}
	
	private class CacheAutoCleanupService implements Runnable{
		@Override
		public void run() {
			while (true){				
				/* start the auto cleanup process */
				logger.info("Start scanning inactive channel...");
				try {
					Set<Object> keySet = SessionsCache.getInstance().getCacheStore().keySet();
			        for (Object cacheKey : keySet) {
			        	UserSession uSession = (UserSession) SessionsCache.getInstance().get(cacheKey);
			            if (uSession == null) continue;
			            if (!uSession.isActive()) {
			            	Event sessionTimeoutEvt = EventFactory.createRTEvent(0, uSession, EventType.SESSION_TIMEOUT, null);
							ExecutorEventDispatcher.getInstance().fireEvent(sessionTimeoutEvt);
			            	//logger.info(String.format("Close and clear up the inactive channel:{}", uSession.getIp()));
			            }
			        }
					
				}			
			    catch (Exception ex) {
			    	logger.error(ex.getMessage());
			    }
				logger.info("Scanning inactive channel ends.");
				/* 线程休眠 */
				try {Thread.sleep(Constants.CHANNEL_CLEARUP_REPEAT_IN_MS);
				} catch (InterruptedException e) {}
			}
					
		}
	
	}
	
}
