package com.redshark.data;


import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import com.redshark.entity.User;
import com.redshark.util.CommonUtil;


public class UserDaoBackup {
	

	private static Jongo Jongo;
	private static MongoCollection users;
	
	public UserDaoBackup()
	{
		synchronized(UserDaoBackup.class)
        {  
			Jongo = DbClient.getInstance().getJongo();
			users = Jongo.getCollection("users");
		}  
		
	}
	
	public MongoCollection getUsers() 
	{
		return users;
	}
	
	public UserPojo createPoJoFromUser(User userEntity) throws Exception
	{
		UserPojo userPojo = new UserPojo();
		CommonUtil.copyFrom(userPojo, userEntity);
		return userPojo;		
	}
	
	public User createUserFromPojo(UserPojo userPojo) throws Exception
	{
		User user = new User();
		CommonUtil.copyFrom(user, userPojo);
		return user;
	}
	
	public void save(User userEntity) throws Exception
	{
		UserPojo userPojo = createPoJoFromUser(userEntity);
		users.save(userPojo);
	}
	
	public void updateById(User userEntity) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", userEntity.getId());
		UserPojo userPojo = createPoJoFromUser(userEntity);
		users.update(kvPair).with(userPojo);
	}
	
	public void deleteById(String id) throws Exception 
	{
		String kvPair = String.format("{_id: '%s'}", id);
		users.remove(kvPair);
	}
	
	public void delete(String key, String value) throws Exception 
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		users.remove(kvPair);
	}
	
	public void deleteByName(String name) throws Exception 
	{
		String kvPair = String.format("{name: '%s'}", name);
		users.remove(kvPair);
	}
	
	public User findOneById(String id) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", id);
		UserPojo userPojo = users.findOne(kvPair).as(UserPojo.class);
		return createUserFromPojo(userPojo);
	}
	
	public User findOneByName(String name) throws Exception
	{
		String kvPair = String.format("{name: '%s'}", name);
		UserPojo userPojo = users.findOne(kvPair).as(UserPojo.class);
		return createUserFromPojo(userPojo);
	}
	
	public User findOne(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		UserPojo userPojo = users.findOne(kvPair).as(UserPojo.class);
		return createUserFromPojo(userPojo);
	}
	
	public List<User> findAll(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		MongoCursor<UserPojo> all = users.find(kvPair).as(UserPojo.class);
		List<User> users = new ArrayList<User>();
		while (all.hasNext()) {
			users.add(createUserFromPojo(all.next()));
        }
		return users;
	}
	
	public List<User> findAll() throws Exception
	{
		MongoCursor<UserPojo> all = users.find().as(UserPojo.class);
		List<User> users = new ArrayList<User>();
		while (all.hasNext()) {
			users.add(createUserFromPojo(all.next()));
        }
		return users;
	}
	
}
