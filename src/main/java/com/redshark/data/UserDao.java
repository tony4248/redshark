package com.redshark.data;

import com.redshark.entity.User;

public class UserDao extends MongoDao<UserPojo, User>{

	public UserDao() 
	{
		super(UserPojo.class, User.class, "users");
	}

}
