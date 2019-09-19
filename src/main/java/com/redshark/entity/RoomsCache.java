package com.redshark.entity;

import java.util.Calendar;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.core.Constants;
import com.redshark.entity.Room.Status;

public class RoomsCache extends BaseCache {
	private static final Logger logger = LoggerFactory.getLogger(RoomsCache.class);
	private static RoomsCache instance;
	private static ExecutorService cacheAutoCleanupES;
	
	/* 构造函数 */
	private RoomsCache(){
		cacheStore = new ConcurrentHashMap<Object, CachedObject>(100, 0.9f, 1);
		capacity = Constants.ACTIVE_ROOM_CACHE_MAX_CAPACITY;
		expireTime = Constants.ACTIVE_ROOM_CACHE_TTL_MS;
		/* start the channel auto cleanup service */
		//startCacheAutoCleanupService();
	}
	
	public static RoomsCache getInstance(){
        if(instance == null){
            synchronized (RoomsCache.class) {
                if(instance == null){
                    instance = new RoomsCache();
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
		boolean result = false;
    	if (this.count() <= capacity || this.isEmpty()) {
            return true;
        }
    	try
    	{
			Set<Object> keySet = RoomsCache.getInstance().getCacheStore().keySet();
	        for (Object cacheKey : keySet) {
	        	Room room = (Room) RoomsCache.getInstance().get(cacheKey);
	            if (room == null) continue;
	            if (room.getStatus() == Status.CLOSED) {
	            	RoomsCache.getInstance().delete(cacheKey);
	            }
	        }
		}			
	    catch (Exception ex) {
	    	logger.error(ex.getMessage());
	    }
 
    	if (this.count() <= capacity){
    		result = true;    		
    	}
    	return result;	
    }
	
	@Override
	public Room get(Object cacheKey) {
		return (Room)super.get(cacheKey);
	}
	
	private class CacheAutoCleanupService implements Runnable{
		@Override
		public void run() {
			while (true){				
				/* start the auto cleanup process */
				logger.info("Start scanning closed room...");
				try {
					Set<Object> keySet = RoomsCache.getInstance().getCacheStore().keySet();
			        for (Object cacheKey : keySet) {
			        	Room room = (Room) RoomsCache.getInstance().get(cacheKey);
			            if (room == null) continue;
			            if (room.getStatus() == Status.CLOSED) {
			            	RoomsCache.getInstance().delete(cacheKey);
			            }
			        }
				}			
			    catch (Exception ex) {
			    	logger.error(ex.getMessage());
			    }
				logger.info("Scanning closed room ends.");
				/* 线程休眠 */
				try {Thread.sleep(Constants.CHANNEL_CLEARUP_REPEAT_IN_MS);
				} catch (InterruptedException e) {}
			}
					
		}
	
	}
	
}
