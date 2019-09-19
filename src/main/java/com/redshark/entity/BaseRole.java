package com.redshark.entity;

import lombok.Data;

/**
 * @author weswu
 * 用每次进入房间都会产生一个role,进行对局的管理,每种游戏的role都会不同
 */

@Data
public class BaseRole {
	public	User user; //role 关联的用户
	
	public BaseRole(User user)
	{
		this.user = user;
	}
}
