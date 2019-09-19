package com.redshark.entity;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Seat {
	private UserSession u;
	private Map<String, Object> Attributes; //额外的属性
	
	public Seat(UserSession userSession) {
		this.u = userSession;
	}
	
	public Object getAttribute(String key)
	{
		return Attributes.get(key);
	}

	public void removeAttribute(String key)
	{
		Attributes.remove(key);
	}

	public void setAttribute(String key, Object value)
	{
		Attributes.put(key, value);
	}
	
}
