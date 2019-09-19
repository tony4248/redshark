package com.redshark.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.util.DateTimeUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse implements Serializable
{

	private static final long serialVersionUID = 8188757584720655876L;
	private static final ObjectMapper objectMapper = new ObjectMapper();
	/* 消息，事件的序列号,用于匹配客户端请求-服务器端响应 */
	protected int id;
	/* 事件的类型 */
	protected int type;
	/* 返回的结果:true, false */
	boolean status;
	/* 消息携带的数据 */
	protected Map<String, Object> args;
	
	public EventResponse(int id, int type, boolean status, String content) 
	{
		Map<String, Object> args = new HashMap<>();
		String finalContent = String.format("%s %s", DateTimeUtil.getCurrentTime(),content);
		args.put("content", finalContent);
		this.id = id;
		this.type = type;
		this.status = status;
		this.args = args;
		
	}

	public String encode() throws Exception
	{
    	return objectMapper.writeValueAsString(this);
	}
}
