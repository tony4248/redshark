package com.redshark.dto;


import java.util.ArrayList;
import java.util.List;

import com.redshark.entity.User;

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
public class UserDTO {
	private String id; //系统产生的UID
	private String name;
	private String nickName; //昵称
	private String sex; //性别  0:女生,1:男生,2:未知
	private String avatar; //头像地址
	private int level; //级别
	private int cardNum; //房卡数量
	private int score; //积分
	private String room; //所在房间的Id
	private int seatNo; //座位号
	
	public static UserDTO build(User user) {
		return new UserDTO(user.getId(), user.getName(), user.getNickName(), user.getSex(),
				user.getAvatar(), user.getLevel(), user.getCardNum(), user.getScore(), 
				user.getRoom(), user.getSeatNo());
	}
	
	public static List<UserDTO> getUserDTOs(List<User> users){
		List<UserDTO> userDTOs = new ArrayList<>();
		for (User user : users) {
			userDTOs.add(build(user));
		}
		return userDTOs;
	}
}
