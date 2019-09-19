package com.redshark.event;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.entity.UserSession;

public class EventFactory {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/* 创建实时任务: RealTimeEvent  */
	public static Event createRTEvent (int id, UserSession session, int type, Map<String, Object> args)
	{
		return  new Event(id, session, type, args, 0, 0, false, 0);
	}
	
	/* 创建定时任务: TimedEvent */
	/**
	 * @param id,event Id
	 * @param session, 关联的User session
	 * @param type, event的类型
	 * @param args, event的参数
	 * @param start, event的开始时间
	 * @param interval, event重复执行的间隔
	 * @param isPeroid,是否是重复事件
	 * @return
	 */
	public static Event createTDEvent (int id, UserSession session, int type, Map<String, Object> args
			,long start, long interval, boolean isPeroid)
	{
		return  new Event(id, session, type, args, start, interval, isPeroid, 0);
	}
	
	/* string转object */
	public static Event decode(String JsonObjetStr) throws Exception
	{
		Event evt = objectMapper.readValue(JsonObjetStr,  Event.class);
    	return evt;
	}
	
	/* object转string */
	public static String encode(Event event) throws Exception
	{
    	return objectMapper.writeValueAsString(event);
	}
	
	/* 将Event的args部分转成相应的class */
	public static <T> T getClass(Event event, Class<T> clazz) throws Exception
	{
		T tgtClass = objectMapper.readValue(objectMapper.writeValueAsString(event.getArgs()),  clazz);
		return tgtClass;
	}
}
