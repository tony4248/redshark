package com.redshark.proto;

import com.redshark.core.RsException;

public class LoginReq{
	
	public String name;
	public String password;
	
	public LoginReq(String name, String password) 
	{
		this.name = name;
		this.password = password;
	}
	
	public void validateProperties() throws RsException
	{
		if(this.name == null || this.name .isEmpty())
		{
			throw new RsException("用户名不能为空！");
		}
		if(this.password == null || this.password .isEmpty())
		{
			throw new RsException("密码不能为空！");
		}
	}
	

}
