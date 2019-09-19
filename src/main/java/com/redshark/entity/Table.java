package com.redshark.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redshark.event.Event;

public class Table {
    //设置座位
    private List<Seat> seats;
    //座位数量
    public int seatsNum;
    //当前用户的座位,从零开始
    public int currentSeatNo;
    //操作次数
    private int steps;
    //桌子的状态
    public Status status;
    //桌子的状态
    public enum Status
	{
		CREATED, FULL, RUNNING, CLOSED
	}
    //桌子关联的定时事件
    public Event timedEvent;
    //额外的属性
	private Map<String, Object> attributes; 
    
    public Table(int seatsNum) {
    	//席位从1开始,0位不用
        this.seats = Arrays.asList(new Seat[seatsNum + 1]);
        this.attributes = new HashMap<String, Object>();
        this.seatsNum = seatsNum;
        this.currentSeatNo = 1;
        this.steps = 0;
        this.status = Status.CREATED;
        //初始化座位
        buildSeats(this.seatsNum);
    }
    
    //根据座位数初始化Seat,全部为空,席位从1开始,0位不用
    private void buildSeats(int seatsNum){
		for (int i = 0; i < seatsNum + 1 ; i++){
			//使用add方法，增加相应个数的位置
			this.seats.set(i, null);
		}
    }
    
    //取得下一个空座位
    public int getNextEmptySeat(){
    	for (int i = 1; i < this.seats.size(); i++){
			 if(null == this.seats.get(i)){return i;}
		}
		return -1;
    }
    
    //取得全部的User对象
    public List<User> getUsers(){
    	List<User> users = new ArrayList<>();
    	for (int i = 1; i < this.seats.size(); i++){
			 if(null != this.seats.get(i)){
				 users.add(this.seats.get(i).getU().getUser());
			 }
		}
    	return users;
    }
    
    //取得全部坐席
    public List<Seat> getSeats(){
    	return this.seats;
    }
    
    //取得席位数
    public int getSeatsNum() {
		return seatsNum;
	}
    
    //设置席位数
	public void setSeatsNum(int seatsNum) {
		this.seatsNum = seatsNum;
	}

	//加入用户
    public boolean addUser(UserSession userSession){
    	//查看是否有空位
    	int seatNo = getNextEmptySeat();
    	//查看是否已经坐下了
    	int seatedNo = getSeatNoByUserId(userSession.getUser().getId());
    	if(seatNo != -1 && seatedNo == -1){
            addUserBySeatNo(seatNo, userSession);
            return true;
        }
        return false;
    }
    
    //加入用户
    public void addUserBySeatNo(int seatNo, UserSession userSession){
    	//使用list的set方法,替换元素
        this.seats.set(seatNo, new Seat(userSession));
        //重置状态数据
    	userSession.getUser().resetStateInRoom();
        //更新座位号
    	userSession.getUser().setSeatNo(seatNo);
    }

    //删除用户
    public void removeUserBySeatNo(int seatNo){
    	UserSession userSession = getUserBySeatNo(seatNo);
    	if(null != userSession) {
    		//重置状态数据
    		userSession.getUser().resetStateInRoom();
    		//置null表示删除
    		this.seats.set(seatNo, null);
    	}
    }
    
    //删除用户
    //删除用户后,没有改变指针,所以可能取得Null
    public boolean removeUser(UserSession userSession){
		//重置状态数据
    	userSession.getUser().resetStateInRoom();
    	for (int i = 1; i < this.seats.size(); i++) {
    		if(null == this.seats.get(i)) {continue;}
            UserSession tempU = this.seats.get(i).getU();
            if(tempU == userSession){ 
            	this.seats.set(i, null);
            	return true;
            }
		}
    	//删除
   		return false;
    }

    //判断是否满了
    public boolean isFull(){
    	int seatNo = getNextEmptySeat();
    	if(seatNo == -1){
        	this.status = Status.FULL;
            return true;
        }
        return false;
    }

    //根据座位号取得用户
    public UserSession getUserBySeatNo(int seatNo){
        return this.seats.get(seatNo).getU();
    }

    //根据User Id取得座位号
    public int getSeatNoByUserId(String userId) {
    	for (int i = 1; i < this.seats.size(); i++) {
    		if(null == this.seats.get(i)) {continue;}
            UserSession userSession = this.seats.get(i).getU();
            if(userSession.getUser().getId() == userId){ return i;}
		}
        return -1;
    }
    
    //根据User Id取得User
	public User getUserByUserId(String userId) {
		for (int i = 1; i < this.seats.size(); i++) {
			if(null == this.seats.get(i)) {continue;}
	        UserSession userSession = this.seats.get(i).getU();
	        if(userSession.getUser().getId() == userId){ return userSession.getUser();}
		}
	    return null;
	}
	
	//根据User Id取得UserSession
	public UserSession getUserSessionByUserId(String userId) {
		for (int i = 1; i < this.seats.size(); i++) {
			if(null == this.seats.get(i)) {continue;}
	        UserSession userSession = this.seats.get(i).getU();
	        if(userSession.getUser().getId() == userId){ return userSession;}
		}
	    return null;
	}
    
   //取得当前用户
    public UserSession getCurrentUser(){
        return this.seats.get(this.currentSeatNo).getU();
    }
    
    //取得上家的座位号,不移动指针
    public int peekPreviousSeatNo() {
    	if(this.seatsNum == 1) { return -1;}
        int seatNo = (this.currentSeatNo - 1) >= 1 ? this.currentSeatNo - 1 : this.seats.size() -1;
        return seatNo;
    }
    
    //取得上家的座位号,移动指针
    public int getPreviousSeatNo() {
        int seatNo = peekPreviousSeatNo();
        if(-1 == seatNo) { return -1;}
        this.currentSeatNo = seatNo;
        this.steps ++;
        return seatNo;
    }

    //取得当前用户的上家,移动指针
    public UserSession getPreviousUser(){
        int seatNo = getPreviousSeatNo();
        if(seatNo == -1 || null == this.seats.get(seatNo)){return null;}
        return this.seats.get(seatNo).getU();
    }
    
    //取得下家的座位号,不移动指针
    public int peekNextSeatNo() {
    	if(this.seatsNum == 1) { return -1;}
    	int seatNo = (this.currentSeatNo + 1) >=  this.seats.size() ? 1 : this.currentSeatNo + 1;
        return seatNo;
    }
    
    //取得下家的座位号,移动指针
    public int getNextSeatNo() {
    	int seatNo = peekNextSeatNo();
        if(-1 == seatNo) { return -1;}
        this.currentSeatNo = seatNo;
        this.steps ++;
        return seatNo;
    }

    //取得当前用户的下家,移动指针
    public UserSession getNextUser(){
        int seatNo = getNextSeatNo();
        if(seatNo == -1 || null == this.seats.get(seatNo)){return null;}
        return this.seats.get(seatNo).getU();
    }
    
    //取得属性的值
    public Object getAttribute(String key)
	{
		return attributes.get(key);
	}
    
    //删除属性
	public void removeAttribute(String key)
	{
		attributes.remove(key);
	}

	//设置属性
	public void setAttribute(String key, Object value)
	{
		attributes.put(key, value);
	}
	
	//取得当前位置
    public int getCurrentSeatNo() {
		return currentSeatNo;
	}
    
    //设置当前位置
	public void setCurrentSeatNo(int currentSeatNo) {
		this.currentSeatNo = currentSeatNo;
		this.steps = 0;
	}
	
	//是否是一圈结束,当前席位号为最后一个
	public boolean isRoundOver() {
		if(this.steps != 0 && ((this.steps + 1) % this.seatsNum) == 0) {
			return true;
		}
		return false;
	}
	
	//是否是一圈重新开始,当前席位号为最前一个
	public boolean isRoundAgain() {
		if(this.steps != 0 && ((this.steps) % this.seatsNum) == 0) {
			return true;
		}
		return false;
	}

	public Event getTimedEvent() {
		return timedEvent;
	}

	public void setTimedEvent(Event timedEvent) {
		this.timedEvent = timedEvent;
	}

	//关闭
    public void close() {
    	this.seats.clear();
    	this.status =  Status.CLOSED;
    }

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
    
}
