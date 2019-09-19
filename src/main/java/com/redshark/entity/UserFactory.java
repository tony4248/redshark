package com.redshark.entity;

import com.redshark.core.RsException;
import com.redshark.data.UserDao;
import com.redshark.util.StdRandom;

public class UserFactory {
	public static  UserDao userDao = new UserDao();
	public static User create(String userName, String password) throws Exception
	{
		/* 查找是否有同名用户 */
    	User existUser = userDao.findOneByName(userName);
    	if (null != existUser) {
    		String errorContent = String.format("用户名: %s已被占用.", userName);
    		throw new RsException(errorContent);
    	}
    	/* 产生Unique的用户Id */
    	String userId = null;
    	do
    	{
    		userId = String.valueOf(StdRandom.generate6BitInt());
    	}
    	while(null != userDao.findOneById(userId));
    	/* 保存用户 */
    	User newUser = new User();
    	newUser.setId(userId);
    	newUser.setName(userName);
    	newUser.setPassword(password);
    	userDao.save(newUser);
		return newUser;		
	}
}
