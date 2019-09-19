package com.redshark.proto;

public class CommonResp {
	
	public boolean result;
	public Object content;

	public CommonResp(Boolean isSuccess, Object object)
	{
		this.result = isSuccess;
		this.content = object;
	}
}
