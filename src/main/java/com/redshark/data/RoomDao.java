package com.redshark.data;

import com.redshark.entity.Room;

public class RoomDao extends MongoDao<RoomPojo, Room>{

	public RoomDao() 
	{
		super(RoomPojo.class, Room.class, "rooms");
	}

}
