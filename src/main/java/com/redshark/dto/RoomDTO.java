package com.redshark.dto;


import com.redshark.entity.Room;
import com.redshark.entity.RoomType;
import com.redshark.entity.Room.Status;
import com.redshark.entity.Room.Tier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户加入房间后,发送给其他人的信息
 * @author weswu
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
	private String id; //房间号
	private String game; //游戏名称
	private String name; //房间名称
	private Tier tier; //房间级别
	private int bottomScore; //房间的底分
	private RoomType type; //房间的类型
	private Status status; //房间的状态
	private String owner; //房间的创建者
	private int capacity; //房间的用户的容量
	private int rounds; //游戏的局数
	
	public static RoomDTO build(Room room) {
		return new RoomDTO(room.getId(), room.getGame(), room.getName(), room.getTier(),room.getBottomScore(),
				room.getType(), room.getStatus(), room.getOwner(), room.getCapacity(), 
				room.getRounds());
	}
}
