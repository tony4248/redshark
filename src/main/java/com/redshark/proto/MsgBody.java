package com.redshark.proto;

import java.util.concurrent.atomic.AtomicInteger;

import com.redshark.core.Constants;

import lombok.Data;

@Data
public class MsgBody {
	private int code; //消息类型
	private int sId; //消息序列号
	private Object data;
	private String version;
	private static AtomicInteger seqId = new AtomicInteger(0);
	
	public MsgBody()
	{
		this.sId = seqId.getAndIncrement();
		this.version = Constants.MSG_VERSION;
	}
	
	public MsgBody(int seqId)
	{
		this.sId = seqId;
		this.version = Constants.MSG_VERSION;
	}
	
	public static MsgBody createCommonMsg(String content)
	{
		MsgBody msgBody = new MsgBody();
		msgBody.setCode(MsgCode.MESS_CODE);
		msgBody.setData(new CommonMsg(content));
		return msgBody;
	}
	
	public static MsgBody crateErrorResp(int sId, String content)
	{
		MsgBody msgBody = new MsgBody(sId);
		msgBody.setCode(MsgCode.ERROR_CODE);
		msgBody.setData(new CommonResp(false, content));
		return msgBody;
		
	}
	
	/**
	 * 创建通用的返回消息
	 * @param sId: 请求的顺序号
	 * @param isSuccess：请求是否成功
	 * @param content: 内容
	 * @return
	 */
	public static MsgBody createCommonResp(int sId, Boolean isSuccess, String content)
	{
		MsgBody msgBody = new MsgBody(sId);
		msgBody.setCode(MsgCode.MESS_CODE);
		msgBody.setData(new CommonResp(isSuccess, content));
		return msgBody;
	}
	
}
