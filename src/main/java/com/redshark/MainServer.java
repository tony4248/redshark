package com.redshark;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.redshark.core.BaseServer;
import com.redshark.handlers.WebsocketHandler;

/**
 * @author weswu
 *
 */
public class MainServer extends BaseServer {
    private ScheduledExecutorService executorService;

    public MainServer(int port) {
        this.port = port;
        executorService = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void start() {
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defLoopGroup,
                        		//HttpServerCodec：将请求和应答消息解码为HTTP消息
                                new HttpServerCodec(),   //请求解码器
                                //HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
                                new HttpObjectAggregator(65536),//将多个消息转换成单一的消息对象
                                //ChunkedWriteHandler：向客户端发送HTML5文件
                                new ChunkedWriteHandler(),  //支持异步发送大的码流，一般用于发送文件流
                                //设置60秒没有读到数据，则触发一个READER_IDLE事件。
                                new IdleStateHandler(180, 0, 0), //检测链路是否读空闲
                                new WebsocketHandler() //处理握手和认证
                        );
                    }
                });

        try {
            cf = b.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) cf.channel().localAddress();
            logger.info("The server has started and is listening on port:{}", addr.getPort());

//            // 定时扫描所有的Channel，关闭失效的Channel
//            executorService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    logger.info("扫描不活跃端口 --------");
//                    UsersManager.scanNotActiveChannel();
//                }
//            }, 3, 60, TimeUnit.SECONDS);
//
//            //定时向所有客户端发送Ping消息
//            executorService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    UsersManager.broadCastPing();
//                }
//            }, 3, 50, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            logger.error("The server failed to start,{}", e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        super.shutdown();
    }
}
