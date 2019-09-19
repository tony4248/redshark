package com.redshark.data;


import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import com.redshark.util.CommonUtil;


public class MongoDao<T, E>{
	private static Jongo Jongo;
	private static MongoCollection mgCollection;
	private Class<T> pojo;
	private Class<E> entity;
	
	public MongoDao(Class<T> pojo, Class<E> entity, String collectionName)
	{
        this.pojo = pojo;
    	this.entity =  entity;
		Jongo = DbClient.getInstance().getJongo();
		mgCollection = Jongo.getCollection(collectionName);
	}
	
	public T newPojo() throws Exception {
	      return this.pojo.newInstance();
	}
	
	public E newEntity() throws Exception {
	      return this.entity.newInstance();
	}
	
	public MongoCollection getCollection() 
	{
		return mgCollection;
	}
	
	public T createPoJoFromEntity(E entity) throws Exception
	{
		T pojo = this.newPojo();
		CommonUtil.copyFrom(pojo, entity);
		return pojo;		
	}
	
	public E createEntityFromPojo(T pojo) throws Exception
	{
		E entity = this.newEntity();
		CommonUtil.copyFrom(entity, pojo);
		return entity;
	}
	
	public void save(E entity) throws Exception
	{
		T pojo = createPoJoFromEntity(entity);
		mgCollection.save(pojo);
	}
	
	public void updateById(String Id, E entity) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", Id);
		T pojo = createPoJoFromEntity(entity);
		mgCollection.update(kvPair).with(pojo);
	}
	
	public void deleteById(String id) throws Exception 
	{
		String kvPair = String.format("{_id: '%s'}", id);
		mgCollection.remove(kvPair);
	}
	
	public void delete(String key, String value) throws Exception 
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		mgCollection.remove(kvPair);
	}
	
	public void deleteByName(String name) throws Exception 
	{
		String kvPair = String.format("{name: '%s'}", name);
		mgCollection.remove(kvPair);
	}
	
	public E findOneById(String id) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", id);
		T pojo = (T) mgCollection.findOne(kvPair).as(this.pojo);
		return (null == pojo)? null: createEntityFromPojo(pojo);
	}

	public E findOneByName(String name) throws Exception
	{
		String kvPair = String.format("{name: '%s'}", name);
		T pojo = (T) mgCollection.findOne(kvPair).as(this.pojo);
		return (null == pojo)? null: createEntityFromPojo(pojo);
	}
	
	public E findOne(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		T pojo = (T) mgCollection.findOne(kvPair).as(this.pojo);
		return (null == pojo)? null: createEntityFromPojo(pojo);
	}
	
	public List<E> findAll(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		MongoCursor<T> all = (MongoCursor<T>) mgCollection.find(kvPair).as(this.pojo);
		List<E> entities = new ArrayList<E>();
		while (all.hasNext()) {
			entities.add(createEntityFromPojo(all.next()));
        }
		return entities;
	}
	
	public List<E> findAll() throws Exception
	{
		MongoCursor<T> all = (MongoCursor<T>) mgCollection.find().as(this.pojo);
		List<E> entities = new ArrayList<E>();
		while (all.hasNext()) {
			entities.add(createEntityFromPojo(all.next()));
        }
		return entities;
	}
	
}
