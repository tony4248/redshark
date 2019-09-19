package com.redshark.data;

import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import com.redshark.entity.Room;
import com.redshark.util.CommonUtil;

public class RoomDaoBackup {

	private static Jongo Jongo;
	private static MongoCollection rooms;
	
	public RoomDaoBackup()
	{
		synchronized(RoomDaoBackup.class)
        {  
			Jongo = DbClient.getInstance().getJongo();
			rooms = Jongo.getCollection("rooms");
		}  
		
	}
	
	public MongoCollection getRooms() 
	{
		return rooms;
	}
	
	public RoomPojo createPoJoFromRoom(Room roomEntity) throws Exception
	{
		RoomPojo roomPojo = new RoomPojo();
		CommonUtil.copyFrom(roomPojo, roomEntity);
		if(null != roomEntity.getSessions()) 
		{
			ArrayList<String> userIds =  new ArrayList<String>();
			roomEntity.getSessions().stream()
				.forEach(s -> userIds.add(s.getUser().getId()));
			roomPojo.setUsers(userIds);
		}
		return roomPojo;		
	}
	
	public Room createRoomFromPojo(RoomPojo roomPojo) throws Exception
	{
		Room room = new Room();
		CommonUtil.copyFrom(room, roomPojo);
		return room;
	}
	
	public void save(Room roomEntity) throws Exception
	{
		RoomPojo roomPojo = createPoJoFromRoom(roomEntity);
		rooms.save(roomPojo);
	}
	
	public void updateById(Room roomEntity) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", roomEntity.getId());
		RoomPojo roomPojo = createPoJoFromRoom(roomEntity);
		rooms.update(kvPair).with(roomPojo);
	}
	
	public void deleteById(String id) throws Exception 
	{
		String kvPair = String.format("{_id: '%s'}", id);
		rooms.remove(kvPair);
	}
	
	public void delete(String key, String value) throws Exception 
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		rooms.remove(kvPair);
	}
	
	public Room findOneById(String id) throws Exception
	{
		String kvPair = String.format("{_id: '%s'}", id);
		RoomPojo roomPojo = rooms.findOne(kvPair).as(RoomPojo.class);
		return createRoomFromPojo(roomPojo);
	}
	
	public Room findOne(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		RoomPojo roomPojo = rooms.findOne(kvPair).as(RoomPojo.class);
		return createRoomFromPojo(roomPojo);
	}
	
	public List<Room> findAll(String key, String value) throws Exception
	{
		String kvPair = String.format("{%s: '%s'}", key, value);
		MongoCursor<RoomPojo> all = rooms.find(kvPair).as(RoomPojo.class);
		List<Room> rooms = new ArrayList<Room>();
		while (all.hasNext()) {
			rooms.add(createRoomFromPojo(all.next()));
        }
		return rooms;
	}
	
	public List<Room> findAll() throws Exception
	{
		MongoCursor<RoomPojo> all = rooms.find().as(RoomPojo.class);
		List<Room> rooms = new ArrayList<Room>();
		while (all.hasNext()) {
			rooms.add(createRoomFromPojo(all.next()));
        }
		return rooms;
	}
}
