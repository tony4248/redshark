package com.redshark.data;


import org.jongo.Jongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.redshark.core.Constants;


public class DbClient {
	private static final Logger logger = LoggerFactory.getLogger(DbClient.class);
	private static DbClient instance;
	private static MongoClient mongoClient; 
	private static Jongo jongo;
	private static DB db;
	
	@SuppressWarnings("deprecation")
	private DbClient()
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
		// 取得数据库实例和Jongo实例
		try 
		{
			db = mongoClient.getDB(Constants.DATABASE_NAME);
			jongo = new Jongo(db);
		}catch (Exception ex) {
			String errorContent = String.format("Can get the database instance :%s, %s", Constants.DATABASE_NAME,ex.getMessage());
			logger.error(errorContent);
		}
		

	}
	public static DbClient getInstance(){
	        if(instance == null){
	            synchronized (DbClient.class) {
	                if(instance == null){
	                    instance = new DbClient();
	                }
	            }
	        }
	        return instance;
	 }
	
	 public Jongo getJongo() 
	 {
		 return this.jongo;
	 }
	
	 
		
	
	 
	
	
}
