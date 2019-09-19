package com.redshark.entity;

import com.redshark.entity.Session.Status;
import com.redshark.util.CommonUtil;

import io.netty.channel.Channel;

public class SessionFactory {
	
	/**
	 * 创建session
	 * @param channel
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static Session create(Channel channel, User user) throws Exception
	{
		Session session = new Session();
		String sessionId = CommonUtil.sha256(user.getName() + user.getPassword());
  		session.setId(sessionId);
  		session.setChannel(channel);
  		session.setActive(true);
  		session.setAuth(true);
  		session.setStatus(Status.NOT_CONNECTED);
  		return session;
			
	}
}
