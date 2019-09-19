package com.redshark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.core.Constants;
import com.redshark.event.EventsMgmtService;

public class RedShark {
    private static final Logger logger = LoggerFactory.getLogger(RedShark.class);

    public static void main(String[] args) {
    	Test test = new Test();
    	//test.texas();
    	//test.poker();
    	test.testCode();
    	//test.testEventScheduler();
    	//test.testBeanCopy();
    	new EventsMgmtService().registerEventHandlers();
        final MainServer server = new MainServer(Constants.WEBSOCKET_PORT);
        server.init();
        server.start();
        // 注册进程钩子，在JVM进程关闭前释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                server.shutdown();
                logger.warn(">>>>>>>>>> jvm shutdown");
                System.exit(0);
            }
        });
    }
}
