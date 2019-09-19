package com.redshark.proto;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MsgFactory {
	
	public static String createCommonMsg(String mess) throws Exception
	{
		MsgProto commonProto = new MsgProto(MsgProto.MESS_PROTO, MsgBody.createCommonMsg(mess));
		return MsgProto.msgEncode(commonProto);
		
	}
	
	public static String createSystemMsg(String mess) throws Exception
	{
		MsgProto commonProto = new MsgProto(MsgProto.MESS_PROTO, MsgBody.createCommonMsg(mess));
		return MsgProto.msgEncode(commonProto);
	}
	
	public static String createErrorResp(int seqId, String content) throws Exception
	{
		MsgProto commonProto = new MsgProto(MsgProto.EROR_PROTO, MsgBody.crateErrorResp(seqId, content));
		return MsgProto.msgEncode(commonProto);
	}
	
	public static String createLogInReqMsg(LoginReq loginReq) throws Exception
	{
		MsgBody  bData = new MsgBody();
		bData.setCode(MsgCode.AUTH_CODE);
		bData.setData(loginReq);
		MsgProto msgProto = new MsgProto(MsgProto.AUTH_PROTO, bData);
		return MsgProto.msgEncode(msgProto);
	}
	
	public static String createLogInRespMsg(int seqId, boolean isSuccess, String content) throws JsonProcessingException
	{
		MsgBody  bData = new MsgBody(seqId);
		bData.setCode(MsgCode.AUTH_CODE);
		CommonResp commonResp = new CommonResp(isSuccess, content);
		bData.setData(commonResp);
		MsgProto msgProto = new MsgProto(MsgProto.AUTH_PROTO, bData);
		return MsgProto.msgEncode(msgProto);
	}
	

}
