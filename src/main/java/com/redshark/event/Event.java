package com.redshark.event;

import java.io.Serializable;
import java.util.Map;

import com.redshark.entity.UserSession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Serializable
{

	private static final long serialVersionUID = 8188757584720622237L;
	/* 消息，事件的序列号,用于匹配客户端请求-服务器端响应 */
	protected int id;
	/* 连接的channel */
	protected UserSession session;
	/* 消息的类型 */
	protected int type;
	/* 消息携带的数据 */
	protected Map<String, Object> args;
	
	/* 定时相关的属性 */
	protected long start;
	protected long interval;
    protected boolean isPeroid;
    protected int triggerCount;
    
    
    public Object getArg(String key)
	{
		return args.get(key);
	}

	public void removeArg(String key)
	{
		args.remove(key);
	}

	public void setArg(String key, Object value)
	{
		args.put(key, value);
	}
	
    public long getNextTriggerTime() {
        return start + interval * (triggerCount + 1);
    }
}
