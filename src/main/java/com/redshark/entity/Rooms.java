package com.redshark.entity;

import java.util.Set;

/**
 * @author weswu
 * 在线的用户
 */
public class Rooms {
		
	 private static Rooms instance;
	 private RoomsCache rId2Rooms;

	 private Rooms() 
	 {
		 rId2Rooms = RoomsCache.getInstance(); 
	 }
	 
	 public static Rooms getInstance(){
	        if(instance == null){
	            synchronized (Rooms.class) {
	                if(instance == null){
	                    instance = new Rooms();
	                }
	            }
	        }
	        return instance;
	 }
	 
	 
	 
	 public void put(String id, Room room) throws Exception{
         rId2Rooms.put(id, room);
	 }
	 
     public void delete(String id) {
    	 rId2Rooms.delete(id);    	 
     }
     
     public Room get(String id) {
	     return rId2Rooms.get(id);
	 }
     
     public Set<Object> getKeys ()
     {
    	 return RoomsCache.getInstance().getCacheStore().keySet();
     }
}
