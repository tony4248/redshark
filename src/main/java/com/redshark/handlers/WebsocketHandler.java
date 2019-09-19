package com.redshark.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;


import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redshark.core.Constants;
import com.redshark.dto.UserDTO;
import com.redshark.entity.Session.Status;
import com.redshark.entity.FuncResult;
import com.redshark.entity.Sessions;
import com.redshark.entity.User;
import com.redshark.entity.UserSession;
import com.redshark.event.Event;
import com.redshark.event.EventFactory;
import com.redshark.event.EventResponse;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;
import com.redshark.proto.MsgCode;
import com.redshark.proto.MsgFactory;
import com.redshark.proto.MsgProto;
import com.redshark.service.UsersMgmtService;
import com.redshark.util.CommonUtil;
import com.redshark.util.NettyUtil;


/**
 * @author weswu
 *
 */
/**
 * @author weswu
 *
 */
public class WebsocketHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);
    //key: channel, value: sessionId
    private final Map<Object, String> sessionIdByChannel = new ConcurrentHashMap<>();
    private final boolean secure = false;

    private UserSession session; 
    private final int maxWebSocketFrameSize = 1024 * 1024;
    
    public WebsocketHandler() {}
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame  && isCurrentHandlerSession(ctx)) {
            handleWebSocket(ctx, (WebSocketFrame) msg);
            return;
        }
    }

    
	/**
	 * 从cookie或Url中取得sessionid
	 * @param request
	 * @return
	 */
	private String getSessionIdFromRequest(FullHttpRequest request) {
		try {
			/* get from cookie */
			for (String cookieHeader: request.headers().getAll(HttpHeaderNames.COOKIE)) 
			{
		        Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieHeader);
			        for (Cookie cookie : cookies) {
		            if (cookie.name().equals(Constants.SESSION_ID_KEY)) {
		                
		                	if(null != cookie.value() && !cookie.value().isEmpty()) 
		                	{ return cookie.value();}
		                
		            }
		        }
		    }
			/* get from request path */
	        return NettyUtil.extractParameter(new QueryStringDecoder(request.uri()), Constants.SESSION_ID_KEY);
		} catch (Exception ex) {
        	logger.warn("Can't get sessionId: {} " + ex.getMessage());
        }
		return null;
	}
	
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (evnet.state().equals(IdleState.READER_IDLE)) {
            	//取得sessionId
            	String sessionId = sessionIdByChannel.get(ctx.channel());
            	if(null == sessionId ) {return;}
            	//取得session
            	UserSession session = Sessions.getInstance().get(sessionId);
            	if(null == session ) {return;}
                //产生session idle事件
                Event sessionIdleEvt = EventFactory.createRTEvent(0, session, EventType.SESSION_IDLE, null);
                ExecutorEventDispatcher.getInstance().fireEvent(sessionIdleEvt);
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	//TODO Remove user from system
    	sessionIdByChannel.remove(ctx.channel());
    	super.channelInactive(ctx);
    }
    
    private boolean isCurrentHandlerSession(ChannelHandlerContext ctx) {
        return sessionIdByChannel.containsKey(ctx.channel());
    }
    
    /* 握手函数 */
    private void handshake(final ChannelHandlerContext ctx, final FullHttpRequest request, final UserSession session)
    {
        session.setStatus(Status.CONNECTING);
    	WebSocketServerHandshakerFactory wsFactory =
            new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true, maxWebSocketFrameSize);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker != null) {
          handshaker.handshake(ctx.channel(), request).addListener(
              new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (future.isSuccess()) {
                    connect(ctx, request, session);
                  } else {
                    logger.error("Can't handshake: {}", session.getId(), future.cause());
                  }
                }
              });
        } else {
          WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
          this.fireHandshakeFailed(ctx);
        }
    }
    
    /**建立连接
     * @param ctx
     * @param request
     * @param sessionId
     */
    private void connect(ChannelHandlerContext ctx, HttpRequest request, UserSession session){
    	String clientIp = null;
    	try
        {
        	sessionIdByChannel.put(ctx.channel(), session.getId());
        	//更新channel
        	session.setChannel(ctx.channel());
           	//更新活跃状态
        	session.setActive(true);
           	//更新认证状态
        	session.setAuth(true);
           	//更新IP地址
        	session.setIp(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
        	//计入日志
        	logger.info("Connection is established with user:{} via ip: {}", session.getUser().getName(), session.getIp());
            return;
        	
        }
        catch(Exception ex)
        {
        	String errorContent = String.format("Can't establish websocket connection：Ip: %s, %s.", clientIp, ex.getMessage());
        	logger.warn(errorContent);
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.FORBIDDEN);
            ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
            return;
        }
    }
    
   
	/**
	 * 处理Http请求
	 * @param ctx
	 * @param request
	 * @throws Exception 
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		FullHttpResponse res = null;
		String errorContent = null;
		String successContent = null;
		String userName = null;
		String password = null;
		try {

			// Handle a bad request.
			if (!request.decoderResult().isSuccess()) {
				res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
			    sendHttpResponse(ctx, request, res);
			    return;
			}

			// Allow only GET methods.
			if (request.method() != GET) {
				res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
			    sendHttpResponse(ctx, request, res);
			    return;
			}
			
			/* 解析HTTP请求 */
			final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
			Map<String, List<String>> parameters = queryStringDecoder.parameters();
			/**
			 * 处理注册的http请求
			 * GET /api/register?userName=tom&password=12345678 HTTP/1.1
			 * 客户端要处理空值检查
			 */
			if (request.uri().startsWith(Constants.HTTP_API_REGISTER) && parameters.size() != 0)
			{
				userName = parameters.get("userName").get(0);
				password = parameters.get("password").get(0);
				/* 空值检查 */
				if(!((null != userName  && !userName.isEmpty()) && (null != password  && !password.isEmpty()))) 
				{ 
					/* 需要返回json内容,因此返回码是:OK */
					res = createHttpJsonResponse(false, "用户名或密码不能为空.", BAD_REQUEST);
					sendHttpResponse(ctx, request, res);
			        return;
		        }
				FuncResult funcRes = UsersMgmtService.register(ctx.channel(), userName, password);
			    /* 发生错误 */
				if (!funcRes.isSuccess()){
					/* 需要返回json内容,因此返回码是:OK */
					res = createHttpJsonResponse(false, (String)funcRes.getData(), CONFLICT);
					sendHttpResponse(ctx, request, res);
			        return;
			    }
				res = createLoginAndRegisterSuccResp((UserSession)funcRes.getData());
				sendHttpResponse(ctx, request, res);
				return;
			}
			/**
			 * 处理登录的http请求
			 * GET /api/login HTTP/1.1
			 */
			if (request.uri().startsWith(Constants.HTTP_API_LOGIN) && parameters.size() != 0)
			{
				userName = parameters.get("userName").get(0);
				password = parameters.get("password").get(0);
				/* 空值检查 */
				if(!((null != userName  && !userName.isEmpty()) && (null != password  && !password.isEmpty()))) 
				{ 
					/* 需要返回json内容,因此返回码是:OK */
					res = createHttpJsonResponse(false, "用户名或密码不能为空.", BAD_REQUEST);
					sendHttpResponse(ctx, request, res);
			        return;
		        }
				FuncResult funcRes = UsersMgmtService.login(ctx.channel(), userName, password);
			    /* 发生错误 */
				if (!funcRes.isSuccess()){
					/* 需要返回json内容,因此返回码是:OK */
					res = createHttpJsonResponse(false, (String)funcRes.getData(), BAD_REQUEST);
					sendHttpResponse(ctx, request, res);
			        return;
			    }
				res = createLoginAndRegisterSuccResp((UserSession)funcRes.getData());
				sendHttpResponse(ctx, request, res);
				return;
			}
			/**
			 * websocket协议
			 *	当且仅当uri为指定path的时候,进行websocket通讯的升级
			 *	GET /websocket HTTP/1.1
			 *  Upgrade: websocket
			 *  Connection: Upgrade
			 *  Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
			 */
			if (request.uri().startsWith(Constants.WEBSOCKET_URL) &&
					(request.headers().get("Connection").equalsIgnoreCase("Upgrade") ||
							request.headers().get("Upgrade").equalsIgnoreCase("WebSocket"))
			        ) 
			{
				 // handshake处理
				 try
				 {
			        final String sessionId = getSessionIdFromRequest(request);
			        // 首次连接必须有sessionId,否则视为非法连接
			        if(null == sessionId || sessionId.isEmpty()) {throw new Exception("SessionId is null or empty.");}
			        // 在Session的查找该SessionId
			        UserSession uSession = Sessions.getInstance().get(sessionId);
			        errorContent = String.format("Invalid sessionId: %s, Ip: %s.", sessionId, NettyUtil.parseChannelRemoteAddr(ctx.channel()));
			        if(null == uSession) {throw new Exception(errorContent);}
			        this.session = uSession;
			        // 发送连接事件
			        Event connectEvt = EventFactory.createRTEvent(0, session, EventType.CONNECT, null);
			        ExecutorEventDispatcher.getInstance().fireEvent(connectEvt);
			        // 进行handshake
			        handshake(ctx, request, uSession);
			        // 发送链接成功事件
			        Event connectSuccessEvt = EventFactory.createRTEvent(0, session, EventType.CONNECT_SUCCESS, null);
			        ExecutorEventDispatcher.getInstance().fireEvent(connectSuccessEvt);

			     } catch (Exception ex) {
			        logger.error("Error occurred during {} handshake: {}", "websocket", ex.getMessage());
			        this.fireHandshakeFailed(ctx);
			     }
				 return;
			} 
			/* 处理Http的其它请求 */    
			sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
			return;
		} catch (Exception ex) {
			//错误消息
			errorContent = String.format("处理用户: %s的请求失败, %s", userName, ex.getMessage());
			res = createHttpJsonResponse(false, errorContent,  INTERNAL_SERVER_ERROR);
			sendHttpResponse(ctx, request, res);
	        return;
		}
    }

	/**
	 * @param result, 执行的结果 success or fail
	 * @param content, 返回的内容
	 * @param httpStatusCode, 返回http code
	 * @return
	 * @throws Exception
	 */
	private FullHttpResponse createHttpJsonResponse(boolean result, String content, HttpResponseStatus httpStatusCode) throws Exception
	{
		/* 暂时去掉httpStatusCode, 因为会加到返回的内容中 */
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, httpStatusCode);
		String resContent = new EventResponse(0, 0, result, content).encode();
		ByteBuf buffer = Unpooled.copiedBuffer(resContent, CharsetUtil.UTF_8);
		res.content().writeBytes(buffer);
		res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
		res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
		buffer.release();
		return res;
	}
	
	
	/**
	 * 注册或登录成功后返回websocket的Url和sessionId
	 * @param sessionId
	 * @param httpStatusCode
	 * @return
	 * @throws Exception
	 */
	private FullHttpResponse createLoginAndRegisterSuccResp(UserSession userSession) throws Exception
	{
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
		@SuppressWarnings("serial")
		Map<String, Object> succResp = new HashMap<String, Object>(){};
		succResp.put("url", Constants.WEBSOCKET_HOST + ":" + Constants.WEBSOCKET_PORT + Constants.WEBSOCKET_URL);
		succResp.put("sessionId", userSession.getId());
		UserDTO userDTO = UserDTO.build(userSession.getUser());
		succResp.put("user", userDTO);		
		String resContent = new EventResponse(0, 0, true, succResp).encode();
		ByteBuf buffer = Unpooled.copiedBuffer(resContent, CharsetUtil.UTF_8);
		res.content().writeBytes(buffer);
		res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
		res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
		buffer.release();
		return res;
	}
	
    /**
     * 处理websocket请求
     * @param ctx
     * @param frame
     * @throws Exception 
     */
    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    	int evtId = 0;
    	try
    	{
    		// 判断是否关闭链路命令
            if (frame instanceof CloseWebSocketFrame) {
            	disconnect(ctx, frame);
                return;
             
            } 
            // 判断是否Ping消息
            else if (frame instanceof PingWebSocketFrame)
            {
                //更新时间点
            	ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            // 判断是否文本消息
            else if (frame instanceof TextWebSocketFrame)
            {
            	//还原消息
                String message = ((TextWebSocketFrame) frame).text();
                /***
                 * {"id":1,"type":1001,"args":{"name":"tom","password":"P12345678"}}
                 */
                //解码成Event对象
                Event evt = EventFactory.decode(message);
                //更新Id
                evtId = evt.getId();
                //更新在线状态
                session.setActive(true);
                //更新session
                evt.setSession(session);
                //触发事件
                ExecutorEventDispatcher.getInstance().fireEvent(evt);
          	
            } 
            else 
            {
            	frame.release();
                logger.warn("{} frame type is not supported", frame.getClass().getName());
            }

    	}
    	catch (Exception ex)
    	{
    		if (ex instanceof JsonProcessingException){
    			String errorResp = new EventResponse(evtId, 0, false, ex.getMessage()).encode();
	        	logger.error(errorResp);
	        	ctx.channel().writeAndFlush(new TextWebSocketFrame(errorResp), ctx.channel().voidPromise());
            } else {
            	disconnect(ctx, frame);
            }
    	}
        
    }
    
    /**
     * 处理客户端disconnect
     * @param ctx
     * @param frame
     */
    private void disconnect(ChannelHandlerContext ctx, WebSocketFrame frame)
    {
    	//取得sessionId
    	String sessionId = sessionIdByChannel.get(ctx.channel());
    	//取得session
    	UserSession session = Sessions.getInstance().get(sessionId);
    	//产生Disconnect事件        
        //产生RoomLeave事件
        Event disconnectEvt = EventFactory.createRTEvent(0, session, EventType.DISCONNECT, null);
        ExecutorEventDispatcher.getInstance().fireEvent(disconnectEvt);
    	//删除本地缓存
    	sessionIdByChannel.remove(ctx.channel());
    	//关闭通道
    	if (ctx.channel().isActive() || ctx.channel().isOpen()) 
    	{
    		ChannelFuture f = ctx.writeAndFlush(frame);
    		f.addListener(ChannelFutureListener.CLOSE);
	    	// Closing the channel will trigger handshake failure.
	        ctx.channel().close();
	        return;
    	}
    }
    
    
    /**
     * 处理ping pong 和登录认证协议
     * @param ctx
     * @param frame
     * @return
     * @throws Exception
     */
    private boolean handPingPongAndAuthMessage(ChannelHandlerContext ctx, MsgProto msgProto) throws Exception
    {
        int type =  msgProto.getUri();
        int code = MsgProto.getMsgCode(msgProto);
        Channel channel = ctx.channel();
        /* 是客户自定义的ping pong 协议 */
        if(type == MsgProto.PING_PROTO || type == MsgProto.PONG_PROTO)
        {
        	if(code == MsgCode.PING_CODE || code == MsgCode.PONG_CODE)
        	{
        		UsersMgmtService.updateUserStatus(channel);
            	UsersMgmtService.sendPong(ctx.channel());
                return true;
        	}
        }
        /* 是客户认证协议 */
        if(type == MsgProto.AUTH_PROTO)
        {
        	/* 是客户认证的code */
        	if(code == MsgCode.AUTH_CODE)
        	{
        		//UsersMgmtService.login(channel, msgProto);
                return true;
        	}
        }
        return false;
    }
    

    /**
     * 处理系统消息和普通消息
     * @param ctx
     * @param frame
     * @return
     * @throws Exception
     */
    private boolean handSystmeAndCommonMessage(ChannelHandlerContext ctx, MsgProto msgProto) throws Exception
    {
        boolean res = validateUserToken(ctx, msgProto);
        if (!res){return false;}
        int type =  msgProto.getUri();
        int code = MsgProto.getMsgCode(msgProto);
        Channel channel = ctx.channel();
        /* 是客户自定义的ping pong 协议 */
        return true;
    }
    
    
    /**
     * 验证用户
     * @param ctx
     * @param msgProto
     * @return
     * @throws JsonProcessingException 
     */
    private boolean validateUserToken(ChannelHandlerContext ctx, MsgProto msgProto) throws Exception
    {
    	User user = null;
        if (user == null) 
        {
        	String errorContent = String.format("用户未登录,非法操作. ip: %s", NettyUtil.parseChannelRemoteAddr(ctx.channel()));
        	String invalidOpResp = MsgFactory.createErrorResp(msgProto.getBody().getSId(), errorContent);
        	UsersMgmtService.send(ctx.channel(), invalidOpResp);
        	return false;
        };
        //if(user.isAuth() && user.isActive()){return true;}
    	return false;
    }
    
    
    /**
     * 发送http响应
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res) {
        // Generate an error page if response status code is not OK (200).
    	//去掉，否则会破坏返回格式
        res.headers().add("Access-Control-Allow-Origin","*");
        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    private String getWebSocketLocation(HttpRequest req) {
        String protocol = secure ? "wss://" : "ws://";
        String webSocketLocation = protocol + req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return webSocketLocation;
    }
    
    private void fireHandshakeFailed(ChannelHandlerContext ctx) {
		if(null != session) {session.setStatus(Status.CLOSED);}
	    //创建connect event
	    Event connectEvt = EventFactory.createRTEvent(0, session, EventType.CONNECT_FAILED, null);
	    ExecutorEventDispatcher.getInstance().fireEvent(connectEvt);
        ctx.channel().close();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Network error, close the connection:", cause);
        if (!ctx.channel().isOpen()) {
        	// Channel didn't open, so we must fire handshake failure directly.
            this.fireHandshakeFailed(ctx);
            return;
        }
        if (cause instanceof IOException){
        	disconnect(ctx, null);
            return;
        }
        if (ctx.channel().isOpen()) {
            // Closing the channel will trigger handshake failure.
        	ctx.channel().close();
        	return;
        }
    }
}
