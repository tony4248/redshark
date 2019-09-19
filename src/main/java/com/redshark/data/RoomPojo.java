package com.redshark.data;

import java.util.List;

import org.jongo.marshall.jackson.oid.MongoId;

import com.redshark.entity.Room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPojo {
	@MongoId
	private String id; //房间号
	private String game; //游戏名称
	private Room.Tier type; //房间类型
	private Room.Status status; //房间的状态
	private String owner; //房间的创建者
	private long createDate; //创建时间
	private long updateDate; //更新时间
	private long closeDate; //关闭时间
	private List<String> users; //活跃的用户列表
}
