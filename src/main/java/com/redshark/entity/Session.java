package com.redshark.entity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.redshark.core.Constants;
import com.redshark.event.ExecutorEventDispatcher;
import com.redshark.util.DateTimeUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

	/**
	 * session status types
	 */
	public enum Status
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}
    //考虑rejoin的情况, sId没变,Channel变化
    public String id;
	private Status status;		
	private volatile Channel channel;    
    private long timePoint;
    private boolean isAuth; //是否已经验证
    private String ip; //登录Ip:Port
    
    public Session(Channel channel) {
    	this.id = UUID.randomUUID().toString();
        this.channel = channel;
        
    }
    
    public void setActive(boolean isActive) {
		this.timePoint = System.currentTimeMillis();
		if (isAuth)
		{
			this.status = Status.CONNECTED;
		}
	}
      
    /**
     * 用户在线时间需要客户端定期更新
     * @return
     */
    
    public boolean isActive() {
        if( channel != null && channel.isActive())
        {
        	if(System.currentTimeMillis() - this.timePoint < Constants.ACTIVE_SESSION_TTL_MS)
        	{return true;}
        }
        return false;
    }

    public void send(String msg) {
        send(msg, null);
    }

    public void send(String message, ChannelFutureListener listener) {
        if (null == channel || !channel.isActive()) {
            return;
        }
        //异步发送消息
        ExecutorEventDispatcher.getInstance().getEventHandlerThreadsPool().run(new Runnable()
		{
			@Override
			public void run()
			{
				if (channel.isWritable()) {
		            if (listener == null) {
		                channel.writeAndFlush(new TextWebSocketFrame(message), channel.voidPromise());
		            } else {
		                channel.writeAndFlush(new TextWebSocketFrame(message)).addListener(listener);
		            }
		        } else {
		            channel.eventLoop().schedule(() -> {
		            	send(message, listener);
		            }, 1L, TimeUnit.SECONDS);
		        }
			}
		});
       
    }
    
    public void close() {
        if (channel != null) {
            if (channel.isActive())
                channel.close();
            channel = null;
        }
    }
}