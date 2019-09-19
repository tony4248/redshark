package com.redshark.entity;

import com.redshark.ddz.Constants;
import com.redshark.dto.EventDTO;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.util.StdRandom;

public class RoomFactory {
	
	public static Room create(Event event) throws Exception
	{
		EventDTO.RoomCreateReq roomCreateReq = EventFactory.getClass(event,  EventDTO.RoomCreateReq.class);
		Room.Tier tier = Room.Tier.valueOf(roomCreateReq.getTier());
		RoomType type = RoomType.valueOf(roomCreateReq.getType());
		Room room = new Room();
		room.setId(String.valueOf(StdRandom.generate6BitInt()));
		room.setTier(tier);
		room.setType(type);
		room.setCapacity(3);
		room.setOwner(event.getSession().getUser().getId());
		room.setTable(new Table(Constants.USER_NUM_PER_TABLE));
		//斗地主房间需要加入和额外属性
		//牛牛房间需要加入的额外属性
		//用户创建房间后自动加入
		//room.joinRoom(event.getSession());
		return room;		
	}
	
}
