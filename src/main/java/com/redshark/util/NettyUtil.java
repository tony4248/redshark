package com.redshark.util;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyUtil {

	private static final Logger logger = LoggerFactory.getLogger(NettyUtil.class);

    /**
     * 获取Channel的远程IP地址
     * @param channel
     * @return
     */
    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }
    
    public static String getSessionId(final String requestPath) {
        String[] parts = requestPath.split("[/]");

        if (parts.length > 4 && !parts[4].isEmpty()) {
          String[] idsplit = parts[4].split("[?]");

          if (idsplit[0] != null && idsplit[0].length() > 0) {
            return idsplit[0];
          }

          return parts[4];
        }

        return null;
    }
    

    public static String getOrigin(final HttpRequest req) {
        return req.headers().get(HttpHeaderNames.ORIGIN);
    }

    public static String extractParameter(QueryStringDecoder queryDecoder, String key) {
        final Map<String, List<String>> params = queryDecoder.parameters();
        List<String> paramsByKey = params.get(key);
        return (paramsByKey != null) ? paramsByKey.get(0) : null;
    }
      
    
    public static SocketAddress resolveClientIpByRemoteAddressHeader(HttpMessage message, String headerName) {
        SocketAddress clientIp = null;
        if (headerName != null && !headerName.trim().isEmpty()) {
          String ip = null;
          try {
            ip = message.headers().get(headerName);
            if (ip != null) {
              ip = ip.split(",")[0]; // to handle multiple proxies case (e.g. X-Forwarded-For: client, proxy1, proxy2)
              clientIp = new InetSocketAddress(InetAddress.getByName(ip), 0);
            }
          } catch (Exception e) {
        	  logger.warn("无法从Http Header: {} 中解析IP地址: {} ", headerName, ip );
          }
        }
        return clientIp;
    }
    
   
}

