package com.redshark.proto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.core.RsException;

/**
 * 协议
 * | head | body
 *   4
 */
public class MsgProto {
    public static final int PING_PROTO = 1 << 8 | 220; //ping消息
    public static final int PONG_PROTO = 2 << 8 | 220; //pong消息
    public static final int SYST_PROTO = 3 << 8 | 220; //系统消息
    public static final int EROR_PROTO = 4 << 8 | 220; //错误消息
    public static final int AUTH_PROTO = 5 << 8 | 220; //认证消息
    public static final int MESS_PROTO = 6 << 8 | 220; //普通消息

    private int uri; //消息头
    private MsgBody body; //消息payload

    public MsgProto(int head, MsgBody body) {
        this.uri = head;
        this.body = body;
    }
    /* 空参构造函数 */
    public MsgProto(){}
    
    public static String buildPingProto() throws JsonProcessingException {
        return buildProto(PING_PROTO, null);
    }

    /* 序列化消息 */
    public static String msgEncode(MsgProto msgProto) throws JsonProcessingException
    {
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	return objectMapper.writeValueAsString(msgProto);
    	
    }
    
    /* 反序列化消息 */
    public static MsgProto msgDecode(String msgString) throws Exception
    {
    	try
    	{
    		ObjectMapper objectMapper = new ObjectMapper();
    		MsgProto msgProto = objectMapper.readValue(msgString,  MsgProto.class);
        	return msgProto;
    	}
    	catch(Exception ex)
    	{
    		throw new RsException("无效数据包!");
    	}
    	
    }
    
    /* 取得消息的类型码 */
    public static int getMsgCode(MsgProto msgProto) throws RsException
    {
    	try
    	{
        	return msgProto.getBody().getCode();
    	}
    	catch(Exception ex)
    	{
    		throw new RsException("无效数据类型");
    	}
    	
    	
    }
    
    public static String buildPongProto() throws JsonProcessingException {
        return buildProto(PONG_PROTO, null);
    }

    public static String buildSystProto(int code, Object mess) throws JsonProcessingException {
        MsgProto gameProto = new MsgProto(SYST_PROTO, null);
        return msgEncode(gameProto);
    }

    public static String buildAuthProto(boolean isSuccess) throws JsonProcessingException {
        MsgProto gameProto = new MsgProto(AUTH_PROTO, null);
        return msgEncode(gameProto);
    }

    public static String buildErorProto(int code,String mess) throws JsonProcessingException {
        MsgProto gameProto = new MsgProto(EROR_PROTO, null);
        return msgEncode(gameProto);
    }

    public static String buildMessProto(String mess) throws JsonProcessingException {
        MsgProto gameProto = new MsgProto(MESS_PROTO, MsgBody.createCommonMsg(mess));
        return msgEncode(gameProto);
    }

    public static String buildProto(int head, MsgBody body) throws JsonProcessingException {
        MsgProto gameProto = new MsgProto(head, body);
        return msgEncode(gameProto);
    }

    public int getUri() {
        return uri;
    }

    public void setUri(int uri) {
        this.uri = uri;
    }

    public MsgBody getBody() {
        return body;
    }

    public void setBody(MsgBody body) {
        this.body = body;
    }
}
