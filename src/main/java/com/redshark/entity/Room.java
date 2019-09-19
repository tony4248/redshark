package com.redshark.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.service.UsersMgmtService;
import com.redshark.util.BlankUtil;

import lombok.Data;

/**
 * @author weswu
 *
 */
@Data
public class Room {
	private static final Logger logger = LoggerFactory.getLogger(UsersMgmtService.class);
	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	/**
	 * room tier
	 */
	public enum Tier
	{
		STD,VIP
	}
	public enum Status
	{
		CREATED, READY, RUNNING, CLOSED
	}
	private String id; //房间号
	private String game; //游戏名称
	private String name; //房间名称
	private Tier tier; //房间级别
	private int bottomScore; //底分
	private RoomType type; //房间的类型
	private Status status; //房间的状态
	private String owner; //房间的创建者
	private int capacity; //房间的用户的容量
	private int rounds; //游戏的局数
	private long createDate; //创建时间
	private long updateDate; //更新时间
	private long closeDate; //关闭时间
	private List<UserSession> sessions; //活跃的用户列表
	private Table table; //桌面
	private Map<String, Object> attributes; //额外的属性

	public Room() 
	{
		this.status =  Status.CREATED;
		this.sessions = new ArrayList<UserSession>();
		this.createDate = System.currentTimeMillis();
		this.attributes = new HashMap<String, Object>();
	}
	
	
	public Object getAttribute(String key)
	{
		return attributes.get(key);
	}

	public void removeAttribute(String key)
	{
		attributes.remove(key);
	}

	public void setAttribute(String key, Object value)
	{
		attributes.put(key, value);
	}
   
    /**
     * 广播房间消息
     * @param msg
    */
    public void sendBroadcastInRoom(String msg)
    {
    	if (!BlankUtil.isBlank(msg)) {
            try {
                rwLock.readLock().lock();
                 for (UserSession usession : sessions)
                 {
                    usession.send(msg);
                 }
             } finally {
                rwLock.readLock().unlock();
             }
        }

    }
    
    /**
     * 广播桌面消息
     * @param msg
     */
    public void sendBroadcastOnTable(String msg)
    {
    	if (!BlankUtil.isBlank(msg)) {
            try {
                rwLock.readLock().lock();
                for (int i = 0; i < this.table.getSeats().size(); i++) {
            		if(null == this.table.getSeats().get(i)) {continue;}
            		this.table.getSeats().get(i).getU().send(msg);
        		}
             } finally {
                rwLock.readLock().unlock();
             }
        }

    }
    

    /**
     * 发送消息给其他人
     * @param session
     * @param msg
     */
    public void sendToOthersInRoom(Session session, String msg)
    {
    	if (!BlankUtil.isBlank(msg)) {
            try {
            	rwLock.readLock().lock();
	            for (UserSession usession : sessions )
	            {
	            	if (!session.getId().equals(usession.getId())) {
	            		usession.send(msg);
	            	}
	               
	            }
             } finally {
                rwLock.readLock().unlock();
             }
        }

    }
    
    
    /**
     * 发送消息给桌面上的用户
     * @param session
     * @param msg
     */
    public void sendToOthersOnTable(Session session, String msg)
    {
    	if (!BlankUtil.isBlank(msg)) {
            try {
            	rwLock.readLock().lock();
            	for (int i = 0; i < this.table.getSeats().size(); i++) {
            		if(null == this.table.getSeats().get(i)) {continue;}
            		if (!session.getId().equals(this.table.getSeats().get(i).getU().getId())) {
            			this.table.getSeats().get(i).getU().send(msg);
	            	}
        		}
             } finally {
                rwLock.readLock().unlock();
             }
        }

    }
  
    /**加入房间
     * @param userSession
     * @return
     */
    public boolean joinRoom(UserSession userSession)
    {
    	UserSession uSession = sessions.stream()
                .filter(e -> e.getId().equals(userSession.getId()))
                .findFirst()
                .orElse(null);
    	if(null == uSession)
    	{
    		this.sessions.add(userSession);
    		userSession.getUser().setRoom(id);
    		//加入桌面
    		this.joinTable(userSession);
    		return true;
    	}
    	return false;
    }
    
    /**
     * 加入桌面
     * @param userSession
     * @return
     */
    public boolean joinTable(UserSession userSession)
    {
		if(this.table.addUser(userSession)) {
			return true;
		}
    	return false;
    }
    
    
    /**
     * 离开桌面
     * @param userSession
     * @return
     */
    public boolean leaveTable(UserSession userSession)
    {
    	return this.table.removeUser(userSession);
    }
    
    /**
     * 离开房间
     * @param userSession
     * @return
     */
    public boolean leaveRoom(UserSession userSession)
    {
    	UserSession uSession = sessions.stream()
                .filter(e -> e.getId().equals(userSession.getId()))
                .findFirst()
                .get();
    	//先删除用户的当前房间
    	userSession.getUser().setRoom(null);
    	//在房间的session Set中删除该session
    	if(null != uSession){
    		//离开桌面
    		this.leaveTable(uSession);
    		//离开房间
       		this.sessions.removeIf(e -> e.equals(uSession));
    	}
       	return true;
    }
    
    /**
     * 关闭房间
     * @return
     */
    public boolean closeRoom(UserSession userSession)
    {
    	//关闭桌子
    	this.table.close();
    	//关闭房间
    	this.status = Status.CLOSED;
    	this.sessions = new ArrayList<UserSession>();
    	this.closeDate = System.currentTimeMillis();
    	//清空相关用户的房间属性值
    	userSession.getUser().setRoom(null);
    	Rooms.getInstance().delete(id);
    	return true;
    	
    }
    
}
