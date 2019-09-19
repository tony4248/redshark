package com.redshark.entity;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author weswu
 * 在线的用户
 */
public class Sessions {
		
	 private static Sessions instance;
	 private SessionsCache sId2Session;

	 private Sessions() 
	 {
		 sId2Session = SessionsCache.getInstance(); 
	 }
	 
	 public static Sessions getInstance(){
	        if(instance == null){
	            synchronized (Sessions.class) {
	                if(instance == null){
	                    instance = new Sessions();
	                }
	            }
	        }
	        return instance;
	 }
	 
	 public void put(String sId, Session session) throws Exception{
         sId2Session.put(sId, session);
	 }
	 
     public void delete(String sId) {
    	 sId2Session.delete(sId);    	 
     }
     
     public UserSession get(String sId) {
    	 UserSession uSession = sId2Session.get(sId);
    	 //更新session的time stamp
    	 if(null != uSession) {uSession.setActive(true);};
    	 return uSession;
	 }
     
     public Set<Object> getKeys ()
     {
    	 return SessionsCache.getInstance().getCacheStore().keySet();
     }
}
