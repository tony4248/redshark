package com.redshark.handlers;

import com.redshark.entity.Sessions;
import com.redshark.event.Event;
import com.redshark.event.EventHandler;
import com.redshark.event.EventType;

import io.netty.channel.Channel;

public class LogOutEventHandler extends EventHandler {
	
	public LogOutEventHandler()
	{
		this.eventType = EventType.LOG_OUT;
	}
	
	@Override
	public void onEvent(Event event)
	{
		//格式化信息
		String formattedCont = null;
		try {
			//关闭channel
			Channel channel = event.getSession().getChannel();
			if (channel.isActive() || channel.isOpen()) {channel.close();}
			//从在线的session缓存中删掉该session
			//取得sessionId
			String sessionId = event.getSession().getId();
			Sessions.getInstance().delete(sessionId);
			//记录用户的退出事件
		} catch (Exception ex) {
			//发送欢迎信息
			formattedCont = String.format("用户:%s退出系统时发生错误:%s."
					, event.getSession().getUser().getName()
					, ex.getMessage()); 
			//记录日志
			logger.error(formattedCont);
			//返回消息并触发相应的事件
			OnComplete(event, 0, EventType.LOG_OUT, false, formattedCont);
		}
	}
}
