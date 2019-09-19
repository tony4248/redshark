package com.redshark.data;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.redshark.core.Constants;

public class MongoDb {
	private static final Logger logger = LoggerFactory.getLogger(MongoDb.class);
	private static MongoClient instance;
	private static MongoClient mongoClient; 
	private static MongoDatabase db;
	
	private MongoDb()
	{
		// 建立客户端
		try 
		{
			mongoClient = new MongoClient(Constants.DATABASE_IP, Constants.DATABASE_PORT);
		}catch (Exception ex) {
			String errorContent = String.format("Can connect the database on %s:%d, %s", Constants.DATABASE_IP,
					Constants.DATABASE_PORT,ex.getMessage());
			logger.error(errorContent);
		}
		// 取得数据库实例
		try 
		{
			db = mongoClient.getDatabase(Constants.DATABASE_NAME);
		}catch (Exception ex) {
			String errorContent = String.format("Can get the database instance :%s, %s", Constants.DATABASE_NAME,ex.getMessage());
			logger.error(errorContent);
		}
		

	}
	public static MongoClient getInstance(){
	        if(instance == null){
	            synchronized (MongoClient.class) {
	                if(instance == null){
	                    instance = new MongoClient();
	                }
	            }
	        }
	        return instance;
	 }
	
	 public MongoDatabase getDatabase() 
	 {
		 return this.db;
	 }
	
	 
		
	
	 
	
	
}
