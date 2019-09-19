package com.redshark.proto;

import lombok.Data;

@Data
public class CommonMsg {
	
	String content;
	
	public CommonMsg(String msg)
	{
		content = msg;
	}

}
