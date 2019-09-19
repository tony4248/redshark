package com.redshark.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public abstract class EventHandler
{
	protected static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	protected static final ObjectMapper objectMapper = new ObjectMapper();
	public int eventType;
	public abstract void onEvent(Event event);
	
	/**
	 * 事件处理结束后的函数
	 * @param event: 已经处理的事件
	 * @param eventType: 触发分下一个目标事件
	 * @param result: 这个事件处理的是否成功
	 * @param respMsg: 这个事件返回给客户端的信息
	 */
	protected void OnComplete(Event event, int type, int eventType, Boolean result, String respMsg) 
	{
		//返回消息
		try {
			if(null != respMsg) {
				//返回消息给客户端
				String resp = new EventResponse(event.getId(), type, result, respMsg).encode();
				event.getSession().send(resp);
			}
			//如果没有事件,直接返回
			if(0 != eventType) {
				//触发事件
				Event evt = EventFactory.createRTEvent(0, event.getSession(), eventType, event.getArgs());
				ExecutorEventDispatcher.getInstance().fireEvent(evt);
			}
		} catch (Exception ex) {
			logger.error("返回用户{}的请求时发生错误:{}", event.getSession().getUser().getName(), ex.getMessage());
		}
	}
}
