package com.redshark.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author weswu
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id; //系统产生的UID
    private String name;
    private String nickName; //昵称
    private String password; //密码
    private String openId; //第三方登录ID
    private String sex; //性别  0:女生,1:男生,2:未知
    private String avatar; //头像地址
    private int level; //级别
    private String mobile; //手机号 
    private int cardNum; //房卡数量
    private Map<String, IScoreInRoom> scoreInRoom = new HashMap<>();
    private int score; //积分
    private String room; //所在房间的Id
    private int seatNo; //座位号
    private java.util.Date loginTime;
    private java.util.Date createTime;
    //private Session session;
    private Map<String, Object> attributes = new HashMap<String, Object>(); //额外的属性

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
	
	public IScoreInRoom getScoreInRoom() {
		return this.scoreInRoom.get(this.room);
	}
	
	public void setScoreInRoom(Map<String, IScoreInRoom> scoreInRoom) {
		this.scoreInRoom = scoreInRoom;
	}
	
	public void resetStateInRoom() {
		this.seatNo = -1;
	}

}
