package com.redshark.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

public class EventDTO {

	@Data
	public static class RoomCreateReq
	{
		private String type;
		private String tier;
	}
	
	@Data
	@AllArgsConstructor
	public static class RoomCreateRes
	{
		private RoomDTO room;
		private UserDTO user;
	}
	
	@Data
	public static class RoomJoinReq
	{
		private String id;
	}
	
	@Data
	@AllArgsConstructor
	public static class RoomJoinRes
	{
		private RoomDTO room;
		private List<UserDTO> users;
	}
	
	@Data
	@AllArgsConstructor
	public static class RoomJoinOthersRes
	{
		private UserDTO user;
	}
	
	@Data
	public static class RoomLeaveReq
	{
		private String id;
		private String content;
	}
	
	//用户离开房间，发给其他人的响应
	@Data
	public static class RoomLeaveRes
	{
		private String userId;
		private String roomId;
		private String content;
	}
	
	@Data
	public static class RoomCloseReq
	{
		private String id;
	}

}
