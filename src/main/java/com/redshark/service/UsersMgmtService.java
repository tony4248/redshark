package com.redshark.service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redshark.core.RsException;
import com.redshark.data.UserDao;
import com.redshark.entity.FuncResult;
import com.redshark.entity.Session;
import com.redshark.entity.SessionFactory;
import com.redshark.entity.Sessions;
import com.redshark.entity.User;
import com.redshark.entity.UserFactory;
import com.redshark.entity.UserSession;
import com.redshark.entity.UserSessionFactory;
import com.redshark.proto.LoginReq;
import com.redshark.proto.MsgBody;
import com.redshark.proto.MsgProto;
import com.redshark.util.BlankUtil;
import com.redshark.util.CommonUtil;
import com.redshark.util.NettyUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author weswu
 *
 */
public class UsersMgmtService {
	private static final Logger logger = LoggerFactory.getLogger(UsersMgmtService.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	private static UserDao userDao = new UserDao();
	
	/**
     * 用户注册
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    public static FuncResult register(Channel channel, String userName, String password) throws Exception
    {
    	String content = null;
    	User newUser = UserFactory.create(userName, password);
    	if (null == newUser) {
    		content = String.format("用户: %s注册失败.", userName);
    		return new FuncResult(false, content);
    	}
    	/* 更新用户数据 */
    	/* 新建UserSession对象 */
  		UserSession userSession = UserSessionFactory.create(channel, newUser);
  		/* 放入在线session缓存 */
  		Sessions.getInstance().put(userSession.getId(), userSession);
  		/* 返回sessionId */
    	return new FuncResult(true, userSession);
    }
    

    /**
     * 处理用户登录,如果成功加入更新活跃用户列表
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    public static FuncResult login(Channel channel, String userName, String password) throws Exception
    {
     	/* 查找用户是否存在 */
    	User existUser = userDao.findOneByName(userName);
    	if (null == existUser) {
    		String errorContent = String.format("用户: %s不存在,请先注册.", userName);
    		return new FuncResult(false, errorContent);
    	}
    	/* 检查密码 */
    	if(!password.equals(existUser.getPassword())){
    		String errorContent = String.format("用户名或密码不正确,请重试.");
    		return new FuncResult(false, errorContent);
    	}
    	/* 新建UserSession对象 */
  		UserSession userSession = UserSessionFactory.create(channel, existUser);
  		/* 放入在线session缓存 */
  		Sessions.getInstance().put(userSession.getId(), userSession);
  		/* 返回sessionId */
    	return new FuncResult(true, userSession);
    }
    
    public static boolean updateUserStatus(Channel channel)
    {
    	
//    	User user = activeUsers.get(channel);
//    	if (null != user){ user.setActive(true);}
    	return true;
    }
    
    
    
    
    /**
     * 广播普通消息
     *
     * @param message
     */
    public static void broadcastCommonMsg(String message) {
        if (!BlankUtil.isBlank(message)) {
            try {
                rwLock.readLock().lock();
//                Set<Object> keySet = ActiveUsers.channel2users.getCacheStore().keySet();
//                for (Object cacheKey : keySet) {
//                    Channel channel = (Channel)cacheKey;
//                    User user = activeUsers.get(channel);
//                    if (user == null || !user.isActive()) continue;
//                    channel.writeAndFlush(new TextWebSocketFrame(MsgFactory.createCommonMsg(message)));
//                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
    
    /**
     * 广播系统消息
     *
     * @param message
     */
    public static void broadcastSystemMsg(String message) {
        if (!BlankUtil.isBlank(message)) {
            try {
//                rwLock.readLock().lock();
//                Set<Object> keySet = ActiveUsers.channel2users.getCacheStore().keySet();
//                for (Object cacheKey : keySet) {
//                    Channel channel = (Channel)cacheKey;
//                    User user = activeUsers.get(channel);
//                    if (user == null || !user.isActive()) continue;
//                    channel.writeAndFlush(new TextWebSocketFrame(MsgFactory.createSystemMsg(message)));
//                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
   
    public static void broadCastPing() {
        try {
//            rwLock.readLock().lock();
//            Set<Object> keySet = ActiveUsers.channel2users.getCacheStore().keySet();
//            for (Object cacheKey : keySet) {
//                Channel channel = (Channel)cacheKey;
//                User user = activeUsers.get(channel);
//                if (user == null || !user.isActive()) continue;
//                channel.writeAndFlush(new TextWebSocketFrame(MsgProto.buildPingProto()));
//            }
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    /**
     * 发送系统消息
     *
     * @param code
     * @param mess
     */
    public static void send(Channel channel, String mess) {
        channel.writeAndFlush(new TextWebSocketFrame(mess));
    }

    
    /**
     * @param channel
     * @throws JsonProcessingException 
     */
    public static void sendPong(Channel channel) throws JsonProcessingException {
        channel.writeAndFlush(new TextWebSocketFrame(MsgProto.buildPongProto()));
    }
    

}
