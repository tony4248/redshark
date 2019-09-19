package com.redshark.entity;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.redshark.core.RsException;

public abstract class BaseCache {
	protected  ConcurrentHashMap<Object, CachedObject> cacheStore = null;
	protected  int capacity;
	//过期时间,如果过期时间为0,则表示不过期
	protected  int expireTime;
   
	public ConcurrentHashMap<Object, CachedObject> getCacheStore()
	{
		return cacheStore;
	}
	
	protected boolean put(Object cacheKey, Object cached) throws RsException{
    	if (!trimToCapcity()){
    		String errMessage = "Cache is full when puting the key: " + cacheKey.toString() + ".";
    		throw new RsException(errMessage);
    	}
    	Calendar timeout = null;
    	//如果过期时间为0,则表示不过期
    	if(0 != expireTime) {
	    	timeout = Calendar.getInstance();
	        timeout.setTimeInMillis(timeout.getTimeInMillis() + expireTime);
    	}
        CachedObject cacheObject = new CachedObject(cached, timeout);
        cacheStore.put(cacheKey, cacheObject);
        return true;
    }
		
	protected Object get(Object cacheKey){
        if(this.has(cacheKey)){
            CachedObject cachedObject = cacheStore.get(cacheKey);
            if(cachedObject.expire != null){
                if(Calendar.getInstance().before(cachedObject.expire)){
                    return cachedObject.cachedObject;
                }else{
                	//删除过期条目
                	this.delete(cacheKey);
                    return null;
                }
            }
            return cachedObject.cachedObject;
        }
        return null;
    }
	
	protected boolean trimToCapcity(){
    	boolean result = false;
    	if (this.count() <= capacity || this.isEmpty()) {
            return true;
        }
    	for (Entry<Object, CachedObject> toRemove : cacheStore.entrySet()) {
	        CachedObject cachedObject = toRemove.getValue();
    		if(cachedObject.expire != null){
                if(Calendar.getInstance().after(cachedObject.expire)){
        	        this.delete(toRemove.getKey());
        	    }
            }
    	}
    	if (this.count() <= capacity){
    		result = true;    		
    	}
    	return result;
    	
    }
    
	protected boolean has(Object cacheKey){
        return cacheStore.containsKey(cacheKey);
    }
	
	protected boolean isEmpty(){
        return cacheStore.isEmpty();
    }
    
	protected void delete(Object cacheKey){
        cacheStore.remove(cacheKey);
    }
	
	protected int count(){
        return cacheStore.size();
    }
    
	protected void clear(){
        cacheStore.clear();
    }
}
/* the object that will be cached */
class CachedObject{
    Calendar expire;
    Object cachedObject;
    public CachedObject(Object cachedObject){
        this.cachedObject = cachedObject;
    }
    public CachedObject(){
    }
    public CachedObject(Object cachedObject, Calendar expire){
        this.cachedObject = cachedObject;
        this.expire = expire;
    }
}
