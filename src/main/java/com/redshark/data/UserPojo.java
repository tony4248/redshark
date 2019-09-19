package com.redshark.data;

import org.jongo.marshall.jackson.oid.MongoId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPojo {
	@MongoId
	private String id; //系统产生的UID
	private String name;
    private String password; //密码
    private String openId; //第三方登录ID
    private String sex; //性别  0:女生,1:男生,2:未知
    private String avatar; //头像地址
    private int level; //级别
    private String mobile; //手机号
    private int score; //目前的积分
    private int cardNum; //房卡数量
    private java.util.Date loginTime;
    private java.util.Date createTime;
}
